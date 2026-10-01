package com.instagramfocus.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.instagramfocus.app.FocusApplication
import com.instagramfocus.app.domain.classifier.InstagramAdapter
import com.instagramfocus.app.domain.classifier.NodeSnapshot
import com.instagramfocus.app.domain.model.BypassState
import com.instagramfocus.app.domain.model.FocusSettings
import com.instagramfocus.app.domain.model.RestrictionDecision
import com.instagramfocus.app.domain.model.ScreenType
import com.instagramfocus.app.ui.intervention.InterventionActivity
import com.instagramfocus.app.util.Debouncer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FocusAccessibilityService : AccessibilityService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private lateinit var debouncer: Debouncer

    private var currentActivityName: String? = null
    private var lastInterventionTimestamp: Long = 0L
    private val INTERVENTION_COOLDOWN_MS = 1200L

    private var lastClassifiedScreen: ScreenType? = null
    private var lastSessionRecordTime: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        debouncer = Debouncer(delayMs = 120L, scope = serviceScope)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val pkgName = event.packageName?.toString() ?: ""
        if (pkgName != InstagramAdapter.PACKAGE_NAME) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val className = event.className?.toString()
            if (className != null && !className.startsWith("android.widget.") && !className.startsWith("android.view.")) {
                currentActivityName = className
            }
        }

        debouncer.debounce {
            processInstagramScreen()
        }
    }

    private suspend fun processInstagramScreen() {
        val container = FocusApplication.instance.container

        // 1. Get current settings and bypass state
        val settings: FocusSettings = container.settingsRepository.settingsFlow.first()
        val bypassState: BypassState = container.settingsRepository.bypassFlow.first()

        // Fast exit if Focus Mode is disabled or bypass is active
        if (!settings.isFocusModeEnabled || (bypassState.isActive && !bypassState.isExpired)) {
            return
        }

        // 2. Safely capture the node snapshot with bounded depth to preserve battery and avoid ANRs
        val rootNode = rootInActiveWindow ?: return
        val snapshot = try {
            buildNodeSnapshot(rootNode, maxDepth = 6, maxChildren = 12)
        } catch (_: Exception) {
            null
        }

        // 3. Classify screen
        val classification = container.classifyScreenUseCase(snapshot, currentActivityName)

        // 4. Evaluate restriction
        val decision = container.evaluateRestrictionUseCase(classification, settings, bypassState)

        // 5. Handle decision
        when (decision) {
            is RestrictionDecision.Block -> {
                val now = SystemClock.uptimeMillis()
                if (now - lastInterventionTimestamp > INTERVENTION_COOLDOWN_MS) {
                    lastInterventionTimestamp = now

                    // Record block event locally
                    container.recordBlockEventUseCase(
                        screenType = decision.screenType,
                        reason = decision.reason,
                        ruleMatched = decision.ruleMatched
                    )

                    // Execute Calm Intervention
                    launchIntervention(decision.screenType, decision.reason)
                }
            }

            is RestrictionDecision.Allow -> {
                // Record DM session metric once per 5 minutes of DM activity
                if (classification.screenType == ScreenType.DM_INBOX || classification.screenType == ScreenType.DM_CONVERSATION) {
                    val now = System.currentTimeMillis()
                    if (now - lastSessionRecordTime > 300_000L) {
                        lastSessionRecordTime = now
                        container.statisticsRepository.recordSession("DM")
                    }
                }
            }

            is RestrictionDecision.Warn -> {
                // Handled gracefully without blocking
            }
        }

        lastClassifiedScreen = classification.screenType
    }

    private fun launchIntervention(screenType: ScreenType, reason: String) {
        // Send BACK action to dismiss the distraction screen in Instagram
        performGlobalAction(GLOBAL_ACTION_BACK)

        // Present Calm Intervention UI
        val intent = Intent(this, InterventionActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(InterventionActivity.EXTRA_SCREEN_TYPE, screenType.name)
            putExtra(InterventionActivity.EXTRA_REASON, reason)
        }
        startActivity(intent)
    }

    /**
     * Bounded tree traversal creating an immutable snapshot.
     * Prevents infinite loops and node recycling exceptions.
     */
    private fun buildNodeSnapshot(node: AccessibilityNodeInfo, currentDepth: Int = 0, maxDepth: Int = 6, maxChildren: Int = 12): NodeSnapshot {
        val text = node.text?.toString()
        val desc = node.contentDescription?.toString()
        val viewId = node.viewIdResourceName
        val className = node.className?.toString()
        val pkg = node.packageName?.toString()
        val isScrollable = node.isScrollable
        val isClickable = node.isClickable
        val childCount = node.childCount

        val children = mutableListOf<NodeSnapshot>()
        if (currentDepth < maxDepth) {
            val limit = minOf(childCount, maxChildren)
            for (i in 0 until limit) {
                val child = try {
                    node.getChild(i)
                } catch (_: Exception) {
                    null
                }
                if (child != null) {
                    children.add(buildNodeSnapshot(child, currentDepth + 1, maxDepth, maxChildren))
                }
            }
        }

        return NodeSnapshot(
            text = text,
            contentDescription = desc,
            viewIdResourceName = viewId,
            className = className,
            packageName = pkg,
            isScrollable = isScrollable,
            isClickable = isClickable,
            childCount = childCount,
            children = children
        )
    }

    override fun onInterrupt() {
        // Accessibility service interrupted by system
    }

    override fun onDestroy() {
        super.onDestroy()
        debouncer.cancel()
        serviceScope.cancel()
    }
}
