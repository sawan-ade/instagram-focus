package com.instagramfocus.app.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import com.instagramfocus.app.FocusApplication
import com.instagramfocus.app.domain.classifier.ClassificationResult
import com.instagramfocus.app.domain.classifier.NodeSnapshot
import com.instagramfocus.app.domain.model.BypassState
import com.instagramfocus.app.domain.model.FocusSettings
import com.instagramfocus.app.domain.model.RestrictionDecision
import com.instagramfocus.app.domain.model.ScreenType
import com.instagramfocus.app.domain.model.StatisticsSummary
import com.instagramfocus.app.util.InstagramIntents
import com.instagramfocus.app.util.PermissionUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineDispatcher
import android.util.Log

class MainActivity : Activity() {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val mainDispatcher = object : CoroutineDispatcher() {
        override fun dispatch(context: CoroutineContext, block: Runnable) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                block.run()
            } else {
                mainHandler.post(block)
            }
        }
    }
    private val activityScope = CoroutineScope(mainDispatcher + Job())
    private var tickerRunnable: Runnable? = null

    private lateinit var rootContainer: LinearLayout

    // Current screen view state
    private enum class CurrentView {
        ONBOARDING, DASHBOARD, SETTINGS, STATISTICS, DEBUG
    }
    private var currentView = CurrentView.DASHBOARD
    private var onboardingStep = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Set status and navigation bar color
            window.statusBarColor = Color.parseColor("#090D16")
            window.navigationBarColor = Color.parseColor("#090D16")

            rootContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setBackgroundColor(Color.parseColor("#090D16"))
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            setContentView(rootContainer)

            checkInitialState()
            startTicker()
        } catch (t: Throwable) {
            Log.e("InstagramFocus", "FATAL in onCreate", t)
            showEmergencyErrorView(t)
        }
    }

    override fun onResume() {
        super.onResume()
        refreshCurrentView()
    }

    override fun onDestroy() {
        super.onDestroy()
        tickerRunnable?.let { mainHandler.removeCallbacks(it) }
    }

    private fun startTicker() {
        tickerRunnable = object : Runnable {
            override fun run() {
                val container = FocusApplication.instance.container
                activityScope.launch {
                    val bypass = container.settingsRepository.bypassFlow.first()
                    if (bypass.isActive && currentView == CurrentView.DASHBOARD) {
                        refreshCurrentView()
                    }
                }
                mainHandler.postDelayed(this, 1000L)
            }
        }
        mainHandler.post(tickerRunnable!!)
    }

    private fun checkInitialState() {
        val container = FocusApplication.instance.container
        activityScope.launch {
            val settings = container.settingsRepository.settingsFlow.first()
            if (!settings.hasCompletedOnboarding) {
                currentView = CurrentView.ONBOARDING
                onboardingStep = 0
            } else {
                currentView = CurrentView.DASHBOARD
            }
            refreshCurrentView()
        }
    }

    private fun refreshCurrentView() {
        rootContainer.removeAllViews()
        when (currentView) {
            CurrentView.ONBOARDING -> renderOnboardingView()
            CurrentView.DASHBOARD -> renderDashboardView()
            CurrentView.SETTINGS -> renderSettingsView()
            CurrentView.STATISTICS -> renderStatisticsView()
            CurrentView.DEBUG -> renderDebugView()
        }
    }

    // ==========================================
    // 1. DASHBOARD VIEW
    // ==========================================
    private fun renderDashboardView() {
        val container = FocusApplication.instance.container

        activityScope.launch {
            val settings = container.settingsRepository.settingsFlow.first()
            val stats = container.statisticsRepository.summaryFlow.value
            val bypass = container.settingsRepository.bypassFlow.first()
            val isAccessEnabled = PermissionUtils.isAccessibilityServiceEnabled(this@MainActivity)

            val scroll = ScrollView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            val content = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(24), dp(20), dp(24))
            }

            // Top Bar
            val headerRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                gravity = Gravity.CENTER_VERTICAL
            }
            val titleCol = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            titleCol.addView(TextView(this@MainActivity).apply {
                text = "INSTAGRAM FOCUS"
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#3B82F6"))
                letterSpacing = 0.1f
            })
            titleCol.addView(TextView(this@MainActivity).apply {
                text = "Protection Hub"
                textSize = 24f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#F8FAFC"))
            })
            headerRow.addView(titleCol)

            // Top Navigation Action Buttons
            val actionsRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            actionsRow.addView(createSmallIconButton("Stats") {
                currentView = CurrentView.STATISTICS
                refreshCurrentView()
            })
            actionsRow.addView(createSmallIconButton("Settings") {
                currentView = CurrentView.SETTINGS
                refreshCurrentView()
            })
            actionsRow.addView(createSmallIconButton("Debug") {
                currentView = CurrentView.DEBUG
                refreshCurrentView()
            })
            headerRow.addView(actionsRow)
            content.addView(headerRow)

            content.addView(createSpacer(16))

            // Accessibility Warning Banner if Disabled
            if (!isAccessEnabled) {
                val warnCard = createCardView(Color.parseColor("#2D1B05"), Color.parseColor("#F59E0B"))
                warnCard.addView(TextView(this@MainActivity).apply {
                    text = "⚠️ Focus protection is currently disabled"
                    textSize = 16f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.parseColor("#F59E0B"))
                })
                warnCard.addView(createSpacer(4))
                warnCard.addView(TextView(this@MainActivity).apply {
                    text = "Enable Accessibility Service so Instagram Focus can block Reels and Explore pages."
                    textSize = 13f
                    setTextColor(Color.parseColor("#CBD5E1"))
                })
                warnCard.addView(createSpacer(10))
                warnCard.addView(createPrimaryButton("Enable Accessibility Service", Color.parseColor("#F59E0B"), Color.BLACK) {
                    PermissionUtils.openAccessibilitySettings(this@MainActivity)
                })
                content.addView(warnCard)
                content.addView(createSpacer(14))
            }

            // Master Switch Focus Card
            val masterCard = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
            val switchRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                gravity = Gravity.CENTER_VERTICAL
            }
            val switchCol = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            switchCol.addView(TextView(this@MainActivity).apply {
                text = "Focus Mode"
                textSize = 18f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#F8FAFC"))
            })
            switchCol.addView(TextView(this@MainActivity).apply {
                text = if (bypass.isActive) "Bypass active: ${bypass.remainingSeconds}s left"
                else if (settings.isFocusModeEnabled) "Distractions are restricted"
                else "Instagram is unrestricted"
                textSize = 13f
                setTextColor(if (bypass.isActive) Color.parseColor("#F59E0B") else Color.parseColor("#94A3B8"))
            })
            switchRow.addView(switchCol)

            val toggleSwitch = Switch(this@MainActivity).apply {
                isChecked = settings.isFocusModeEnabled
                setOnCheckedChangeListener { _, isChecked ->
                    if (!isChecked && settings.enableFrictionSurvey) {
                        showFrictionSurveyDialog()
                    } else {
                        activityScope.launch { container.settingsRepository.setFocusMode(isChecked) }
                    }
                }
            }
            switchRow.addView(toggleSwitch)
            masterCard.addView(switchRow)
            content.addView(masterCard)

            content.addView(createSpacer(14))

            // Allowed vs Blocked Overview Row
            val overviewRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            // Allowed Card
            val allowedCard = createCardView(Color.parseColor("#06241B"), Color.parseColor("#10B981")).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            allowedCard.addView(TextView(this@MainActivity).apply {
                text = "Allowed"
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#10B981"))
            })
            allowedCard.addView(createSpacer(6))
            listOf("✓ DMs", "✓ Stories", "✓ Follows", "✓ Alerts").forEach {
                allowedCard.addView(TextView(this@MainActivity).apply {
                    text = it
                    textSize = 13f
                    setTextColor(Color.parseColor("#F8FAFC"))
                })
            }
            overviewRow.addView(allowedCard)

            overviewRow.addView(createSpacerHorizontal(10))

            // Blocked Card
            val blockedCard = createCardView(Color.parseColor("#260808"), Color.parseColor("#EF4444")).apply {
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            blockedCard.addView(TextView(this@MainActivity).apply {
                text = "Blocked"
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#EF4444"))
            })
            blockedCard.addView(createSpacer(6))
            listOf("✗ Reels", "✗ Explore", "✗ Feed", "✗ Suggestions").forEach {
                blockedCard.addView(TextView(this@MainActivity).apply {
                    text = it
                    textSize = 13f
                    setTextColor(Color.parseColor("#F8FAFC"))
                })
            }
            overviewRow.addView(blockedCard)
            content.addView(overviewRow)

            content.addView(createSpacer(14))

            // Today's Distraction Protection Card
            val statsCard = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
            statsCard.addView(TextView(this@MainActivity).apply {
                text = "Today's Distraction Protection"
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#F8FAFC"))
            })
            statsCard.addView(createSpacer(10))
            statsCard.addView(createMetricRow("Reels blocked", "${stats.reelsBlockedToday} attempts", Color.parseColor("#EF4444")))
            statsCard.addView(createMetricRow("Explore blocked", "${stats.exploreBlockedToday} attempts", Color.parseColor("#F59E0B")))
            statsCard.addView(createMetricRow("Feed scrolling blocked", "${stats.feedBlockedToday} attempts", Color.parseColor("#3B82F6")))
            statsCard.addView(createMetricRow("DM sessions protected", "${stats.dmSessionsCount} intentional", Color.parseColor("#10B981")))
            content.addView(statsCard)

            content.addView(createSpacer(14))

            // Temporary Bypass Section
            val bypassCard = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
            bypassCard.addView(TextView(this@MainActivity).apply {
                text = "Temporary Bypass"
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#F8FAFC"))
            })
            bypassCard.addView(createSpacer(4))
            bypassCard.addView(TextView(this@MainActivity).apply {
                text = if (bypass.isActive) "Active: restrictions return in ${bypass.remainingSeconds}s"
                else "Temporarily pause restrictions for quick tasks"
                textSize = 13f
                setTextColor(Color.parseColor("#94A3B8"))
            })
            bypassCard.addView(createSpacer(10))

            if (bypass.isActive) {
                bypassCard.addView(createPrimaryButton("Resume Focus Protection Now", Color.parseColor("#3B82F6"), Color.WHITE) {
                    activityScope.launch { container.settingsRepository.deactivateBypass() }
                })
            } else {
                val bypassRow = LinearLayout(this@MainActivity).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                }
                listOf(5, 10, 30).forEach { mins ->
                    val btn = createSecondaryButton("$mins min") {
                        activityScope.launch { container.settingsRepository.activateBypass(mins) }
                    }.apply {
                        layoutParams = LinearLayout.LayoutParams(0, dp(42), 1f)
                    }
                    bypassRow.addView(btn)
                    if (mins != 30) bypassRow.addView(createSpacerHorizontal(8))
                }
                bypassCard.addView(bypassRow)
            }
            content.addView(bypassCard)

            content.addView(createSpacer(20))

            // Action Buttons
            content.addView(createPrimaryButton("Open Direct Messages Directly", Color.parseColor("#3B82F6"), Color.WHITE) {
                InstagramIntents.openDirectMessages(this@MainActivity)
            })
            content.addView(createSpacer(10))
            content.addView(createSecondaryButton("Open Instagram") {
                InstagramIntents.openInstagram(this@MainActivity)
            })

            scroll.addView(content)
            rootContainer.addView(scroll)
        }
    }

    private fun showFrictionSurveyDialog() {
        val container = FocusApplication.instance.container
        val options = arrayOf(
            "I need something specific on Instagram",
            "I'm done with focused work for today",
            "Just a quick check (I'll take 10m pause)",
            "Other reason"
        )
        var selectedIdx = 0

        AlertDialog.Builder(this)
            .setTitle("Pause Distraction Protection?")
            .setMessage("Focus Mode is protecting your attention. Why are you turning it off?")
            .setSingleChoiceItems(options, 0) { _, which -> selectedIdx = which }
            .setPositiveButton("Turn Off Completely") { _, _ ->
                activityScope.launch { container.settingsRepository.setFocusMode(false) }
            }
            .setNeutralButton("Take 10m Pause Instead") { _, _ ->
                activityScope.launch { container.settingsRepository.activateBypass(10) }
            }
            .setNegativeButton("Keep Focus ON", null)
            .show()
    }

    // ==========================================
    // 2. ONBOARDING VIEW
    // ==========================================
    private fun renderOnboardingView() {
        val container = FocusApplication.instance.container
        val isAccessEnabled = PermissionUtils.isAccessibilityServiceEnabled(this)
        val isNotifEnabled = PermissionUtils.isNotificationListenerEnabled(this)

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(32), dp(24), dp(32))
            gravity = Gravity.CENTER_HORIZONTAL
        }

        when (onboardingStep) {
            0 -> {
                content.addView(TextView(this).apply {
                    text = "🛡️"
                    textSize = 48f
                    gravity = Gravity.CENTER
                })
                content.addView(createSpacer(16))
                content.addView(TextView(this).apply {
                    text = "Instagram, without the rabbit hole."
                    textSize = 26f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.parseColor("#F8FAFC"))
                    gravity = Gravity.CENTER
                })
                content.addView(createSpacer(12))
                content.addView(TextView(this).apply {
                    text = "Keep Instagram for the people you care about.\nRemove the endless scrolling."
                    textSize = 16f
                    setTextColor(Color.parseColor("#94A3B8"))
                    gravity = Gravity.CENTER
                })
                content.addView(createSpacer(24))
                val philosophyCard = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
                philosophyCard.addView(TextView(this).apply {
                    text = "The Philosophy"
                    textSize = 15f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.parseColor("#3B82F6"))
                })
                philosophyCard.addView(createSpacer(4))
                philosophyCard.addView(TextView(this).apply {
                    text = "\"Instagram as a communication tool, NOT an entertainment app.\""
                    textSize = 14f
                    setTextColor(Color.parseColor("#F8FAFC"))
                })
                content.addView(philosophyCard)
            }
            1 -> {
                content.addView(TextView(this).apply {
                    text = "Allowed Features"
                    textSize = 24f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.parseColor("#F8FAFC"))
                })
                content.addView(createSpacer(4))
                content.addView(TextView(this).apply {
                    text = "All genuine personal and social communication is protected:"
                    textSize = 14f
                    setTextColor(Color.parseColor("#94A3B8"))
                })
                content.addView(createSpacer(16))
                listOf(
                    "Direct Messages (DMs)" to "Chat, send photos, voice notes with friends",
                    "Instagram Stories" to "View updates from friends without video feeds",
                    "Follow Requests" to "Approve or decline friend requests",
                    "Social Notifications" to "Alerts for direct messages and mentions",
                    "User Profiles" to "View accounts and profiles when searching"
                ).forEach { (title, desc) ->
                    val card = createCardView(Color.parseColor("#06241B"), Color.parseColor("#10B981"))
                    card.addView(TextView(this).apply {
                        text = "✓ $title"
                        textSize = 15f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(Color.parseColor("#10B981"))
                    })
                    card.addView(TextView(this).apply {
                        text = desc
                        textSize = 13f
                        setTextColor(Color.parseColor("#CBD5E1"))
                    })
                    content.addView(card)
                    content.addView(createSpacer(8))
                }
            }
            2 -> {
                content.addView(TextView(this).apply {
                    text = "Blocked Distractions"
                    textSize = 24f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.parseColor("#F8FAFC"))
                })
                content.addView(createSpacer(4))
                content.addView(TextView(this).apply {
                    text = "Addictive algorithmic loops designed for infinite scroll:"
                    textSize = 14f
                    setTextColor(Color.parseColor("#94A3B8"))
                })
                content.addView(createSpacer(16))
                listOf(
                    "Instagram Reels" to "Infinite vertical short-form video feed",
                    "Explore Grid" to "Endless discovery algorithm optimized to hook attention",
                    "Endless Home Feed" to "Algorithmic posts below 'You're all caught up'",
                    "Suggested Content" to "Recommendation rabbit holes from unknown accounts"
                ).forEach { (title, desc) ->
                    val card = createCardView(Color.parseColor("#260808"), Color.parseColor("#EF4444"))
                    card.addView(TextView(this).apply {
                        text = "✗ $title"
                        textSize = 15f
                        typeface = Typeface.DEFAULT_BOLD
                        setTextColor(Color.parseColor("#EF4444"))
                    })
                    card.addView(TextView(this).apply {
                        text = desc
                        textSize = 13f
                        setTextColor(Color.parseColor("#CBD5E1"))
                    })
                    content.addView(card)
                    content.addView(createSpacer(8))
                }
            }
            3 -> {
                content.addView(TextView(this).apply {
                    text = "Required Permissions"
                    textSize = 24f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(Color.parseColor("#F8FAFC"))
                })
                content.addView(createSpacer(4))
                content.addView(TextView(this).apply {
                    text = "100% on-device. No data ever leaves your phone."
                    textSize = 14f
                    setTextColor(Color.parseColor("#94A3B8"))
                })
                content.addView(createSpacer(16))

                // 1. Accessibility Service
                val p1 = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
                p1.addView(TextView(this).apply {
                    text = "1. Accessibility Service: " + if (isAccessEnabled) "✓ GRANTED" else "MISSING"
                    textSize = 15f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(if (isAccessEnabled) Color.parseColor("#10B981") else Color.parseColor("#EF4444"))
                })
                p1.addView(createSpacer(2))
                p1.addView(TextView(this).apply {
                    text = "Enables detecting Reels vs DMs so the app can protect your attention."
                    textSize = 13f
                    setTextColor(Color.parseColor("#94A3B8"))
                })
                if (!isAccessEnabled) {
                    p1.addView(createSpacer(6))
                    p1.addView(createSecondaryButton("Enable Accessibility") { PermissionUtils.openAccessibilitySettings(this) })
                }
                content.addView(p1)
                content.addView(createSpacer(8))

                // 2. Notification Filter
                val p2 = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
                p2.addView(TextView(this).apply {
                    text = "2. Notification Filter: " + if (isNotifEnabled) "✓ GRANTED" else "OPTIONAL"
                    textSize = 15f
                    typeface = Typeface.DEFAULT_BOLD
                    setTextColor(if (isNotifEnabled) Color.parseColor("#10B981") else Color.parseColor("#3B82F6"))
                })
                p2.addView(createSpacer(2))
                p2.addView(TextView(this).apply {
                    text = "Silently filters promotional Reels notifications while keeping message alerts."
                    textSize = 13f
                    setTextColor(Color.parseColor("#94A3B8"))
                })
                if (!isNotifEnabled) {
                    p2.addView(createSpacer(6))
                    p2.addView(createSecondaryButton("Enable Notification Filter") { PermissionUtils.openNotificationListenerSettings(this) })
                }
                content.addView(p2)
            }
        }

        content.addView(createSpacer(24))

        val navRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
        if (onboardingStep > 0) {
            navRow.addView(createSecondaryButton("Back") {
                onboardingStep--
                refreshCurrentView()
            }.apply {
                layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f)
            })
            navRow.addView(createSpacerHorizontal(10))
        }
        navRow.addView(createPrimaryButton(if (onboardingStep == 3) "Enter Focus Mode" else "Continue", Color.parseColor("#3B82F6"), Color.WHITE) {
            if (onboardingStep == 3) {
                activityScope.launch {
                    container.settingsRepository.completeOnboarding()
                    currentView = CurrentView.DASHBOARD
                    refreshCurrentView()
                }
            } else {
                onboardingStep++
                refreshCurrentView()
            }
        }.apply {
            layoutParams = LinearLayout.LayoutParams(0, dp(48), 1f)
        })
        content.addView(navRow)

        scroll.addView(content)
        rootContainer.addView(scroll)
    }

    // ==========================================
    // 3. SETTINGS VIEW
    // ==========================================
    private fun renderSettingsView() {
        val container = FocusApplication.instance.container
        activityScope.launch {
            val settings = container.settingsRepository.settingsFlow.first()

            val scroll = ScrollView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            val content = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(24), dp(20), dp(24))
            }

            content.addView(createHeaderWithBack("Focus Settings") {
                currentView = CurrentView.DASHBOARD
                refreshCurrentView()
            })
            content.addView(createSpacer(16))

            content.addView(createSectionHeader("BLOCKED SURFACES", Color.parseColor("#EF4444")))
            content.addView(createSpacer(8))
            val blockedCard = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
            blockedCard.addView(createSettingSwitch("Block Reels", "Prevents opening the vertical short-video feed", settings.blockReels) {
                activityScope.launch { container.settingsRepository.setBlockReels(it) }
            })
            blockedCard.addView(createDivider())
            blockedCard.addView(createSettingSwitch("Block Explore Grid", "Blocks discovery algorithm grid", settings.blockExplore) {
                activityScope.launch { container.settingsRepository.setBlockExplore(it) }
            })
            blockedCard.addView(createDivider())
            blockedCard.addView(createSettingSwitch("Block Endless Feed", "Restricts timeline scrolling past updates", settings.blockFeed) {
                activityScope.launch { container.settingsRepository.setBlockFeed(it) }
            })
            blockedCard.addView(createDivider())
            blockedCard.addView(createSettingSwitch("Block Suggested Content", "Blocks accounts you do not follow", settings.blockSuggestedContent) {
                activityScope.launch { container.settingsRepository.setBlockSuggestedContent(it) }
            })
            content.addView(blockedCard)

            content.addView(createSpacer(20))

            content.addView(createSectionHeader("ALLOWED COMMUNICATION", Color.parseColor("#10B981")))
            content.addView(createSpacer(8))
            val allowedCard = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
            allowedCard.addView(createSettingSwitch("Allow Direct Messages (DMs)", "Chat, photos, voice notes remain active", settings.allowDms) {
                activityScope.launch { container.settingsRepository.setAllowDms(it) }
            })
            allowedCard.addView(createDivider())
            allowedCard.addView(createSettingSwitch("Allow Stories", "Permits viewing updates from contacts", settings.allowStories) {
                activityScope.launch { container.settingsRepository.setAllowStories(it) }
            })
            allowedCard.addView(createDivider())
            allowedCard.addView(createSettingSwitch("Allow Follow Requests", "Permits approving connection requests", settings.allowFollowRequests) {
                activityScope.launch { container.settingsRepository.setAllowFollowRequests(it) }
            })
            allowedCard.addView(createDivider())
            allowedCard.addView(createSettingSwitch("Allow Profiles", "Permits opening profile pages intentionally", settings.allowProfiles) {
                activityScope.launch { container.settingsRepository.setAllowProfiles(it) }
            })
            content.addView(allowedCard)

            content.addView(createSpacer(20))

            content.addView(createSectionHeader("STRICTNESS & FRICTION", Color.parseColor("#3B82F6")))
            content.addView(createSpacer(8))
            val strictCard = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
            strictCard.addView(createSettingSwitch("Strict Mode", "Actively monitors unrecognized screens (otherwise safe fallback allows them)", settings.isStrictModeEnabled) {
                activityScope.launch { container.settingsRepository.setStrictMode(it) }
            })
            strictCard.addView(createDivider())
            strictCard.addView(createSettingSwitch("Anti-Bypass Friction Survey", "Prompts for intention when turning Focus Mode off", settings.enableFrictionSurvey) {
                activityScope.launch { container.settingsRepository.setFrictionSurvey(it) }
            })
            content.addView(strictCard)

            scroll.addView(content)
            rootContainer.addView(scroll)
        }
    }

    // ==========================================
    // 4. STATISTICS VIEW
    // ==========================================
    private fun renderStatisticsView() {
        val container = FocusApplication.instance.container
        val stats = container.statisticsRepository.summaryFlow.value

        val scroll = ScrollView(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(24))
        }

        content.addView(createHeaderWithBack("Protection Statistics") {
            currentView = CurrentView.DASHBOARD
            refreshCurrentView()
        })
        content.addView(createSpacer(16))

        // Weekly Highlight Card
        val weekCard = createCardView(Color.parseColor("#0C2042"), Color.parseColor("#3B82F6"))
        weekCard.addView(TextView(this).apply {
            text = "WEEKLY PROTECTION IMPACT"
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#3B82F6"))
        })
        weekCard.addView(createSpacer(6))
        weekCard.addView(TextView(this).apply {
            text = "${stats.weeklyBlockedTotal}"
            textSize = 36f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#F8FAFC"))
        })
        weekCard.addView(TextView(this).apply {
            text = "Distraction attempts prevented over the last 7 days"
            textSize = 13f
            setTextColor(Color.parseColor("#94A3B8"))
        })
        content.addView(weekCard)

        content.addView(createSpacer(16))

        content.addView(createSectionHeader("TODAY'S BREAKDOWN", Color.parseColor("#94A3B8")))
        content.addView(createSpacer(8))
        val todayCard = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
        todayCard.addView(createMetricRow("Reels blocked", "${stats.reelsBlockedToday}", Color.parseColor("#EF4444")))
        todayCard.addView(createDivider())
        todayCard.addView(createMetricRow("Explore blocked", "${stats.exploreBlockedToday}", Color.parseColor("#F59E0B")))
        todayCard.addView(createDivider())
        todayCard.addView(createMetricRow("Feed scrolling blocked", "${stats.feedBlockedToday}", Color.parseColor("#3B82F6")))
        todayCard.addView(createDivider())
        todayCard.addView(createMetricRow("Total distractions prevented today", "${stats.totalBlockedToday}", Color.parseColor("#F8FAFC")))
        content.addView(todayCard)

        content.addView(createSpacer(16))

        content.addView(createSectionHeader("COMMUNICATION SESSIONS", Color.parseColor("#10B981")))
        content.addView(createSpacer(8))
        val sessionCard = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
        sessionCard.addView(createMetricRow("Direct Message sessions", "${stats.dmSessionsCount}", Color.parseColor("#10B981")))
        sessionCard.addView(createDivider())
        sessionCard.addView(createMetricRow("Instagram launches", "${stats.instagramSessionsCount}", Color.parseColor("#F8FAFC")))
        content.addView(sessionCard)

        content.addView(createSpacer(20))

        content.addView(createSecondaryButton("Clear Statistics") {
            activityScope.launch {
                container.statisticsRepository.clearAll()
                refreshCurrentView()
            }
        })

        scroll.addView(content)
        rootContainer.addView(scroll)
    }

    // ==========================================
    // 5. DEBUG / RULE INSPECTOR VIEW
    // ==========================================
    private var simulatedScreenKey = "REELS"

    private fun renderDebugView() {
        val container = FocusApplication.instance.container

        activityScope.launch {
            val settings = container.settingsRepository.settingsFlow.first()
            val bypass = container.settingsRepository.bypassFlow.first()

            val snapshot = when (simulatedScreenKey) {
                "REELS" -> NodeSnapshot(
                    text = "Reels",
                    contentDescription = "Reel by creator",
                    viewIdResourceName = "com.instagram.android:id/clips_viewer_view_pager",
                    children = listOf(
                        NodeSnapshot(text = "Original audio", contentDescription = "Audio used in this reel"),
                        NodeSnapshot(contentDescription = "Like reel")
                    )
                )
                "FEED" -> NodeSnapshot(
                    text = "Home",
                    viewIdResourceName = "com.instagram.android:id/feed_tab",
                    children = listOf(
                        NodeSnapshot(text = "Suggested for you"),
                        NodeSnapshot(text = "You're all caught up")
                    )
                )
                "EXPLORE" -> NodeSnapshot(
                    contentDescription = "Search and explore",
                    viewIdResourceName = "com.instagram.android:id/explore_grid",
                    children = listOf(NodeSnapshot(text = "Search instagram"))
                )
                "DM_INBOX" -> NodeSnapshot(
                    text = "Messages",
                    viewIdResourceName = "com.instagram.android:id/direct_inbox",
                    children = listOf(NodeSnapshot(text = "Primary"), NodeSnapshot(text = "General"))
                )
                "DM_CHAT" -> NodeSnapshot(
                    text = "Message...",
                    viewIdResourceName = "com.instagram.android:id/row_thread_composer_edittext",
                    children = listOf(NodeSnapshot(contentDescription = "Send message"))
                )
                "STORY" -> NodeSnapshot(
                    text = "Reply to user...",
                    viewIdResourceName = "com.instagram.android:id/reel_viewer_progress_bar",
                    children = listOf(NodeSnapshot(contentDescription = "Like story"))
                )
                "PROFILE" -> NodeSnapshot(
                    viewIdResourceName = "com.instagram.android:id/profile_tab",
                    children = listOf(NodeSnapshot(text = "Posts"), NodeSnapshot(text = "Followers"))
                )
                else -> NodeSnapshot(text = "Unknown screen", children = emptyList())
            }

            val classification = container.classifier.classify(snapshot, null)
            val decision = container.restrictionEngine.evaluate(classification, settings, bypass)

            val scroll = ScrollView(this@MainActivity).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            }
            val content = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(20), dp(24), dp(20), dp(24))
            }

            content.addView(createHeaderWithBack("Classifier Live Inspector") {
                currentView = CurrentView.DASHBOARD
                refreshCurrentView()
            })
            content.addView(createSpacer(14))

            content.addView(createSectionHeader("SELECT SIMULATION SCENARIO", Color.parseColor("#3B82F6")))
            content.addView(createSpacer(8))

            val hScroll = HorizontalScrollView(this@MainActivity)
            val pillRow = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.HORIZONTAL
            }
            listOf(
                "REELS" to "Reels",
                "FEED" to "Home Feed",
                "EXPLORE" to "Explore",
                "DM_INBOX" to "DM Inbox",
                "DM_CHAT" to "DM Chat",
                "STORY" to "Story",
                "PROFILE" to "Profile",
                "UNKNOWN" to "Unknown"
            ).forEach { (key, label) ->
                val isSelected = simulatedScreenKey == key
                val btn = Button(this@MainActivity).apply {
                    text = label
                    textSize = 12f
                    setPadding(dp(12), dp(6), dp(12), dp(6))
                    val bg = GradientDrawable().apply {
                        cornerRadius = dp(16).toFloat()
                        setColor(if (isSelected) Color.parseColor("#3B82F6") else Color.parseColor("#1E293B"))
                    }
                    background = bg
                    setTextColor(if (isSelected) Color.WHITE else Color.parseColor("#CBD5E1"))
                    setOnClickListener {
                        simulatedScreenKey = key
                        refreshCurrentView()
                    }
                }
                pillRow.addView(btn)
                pillRow.addView(createSpacerHorizontal(6))
            }
            hScroll.addView(pillRow)
            content.addView(hScroll)

            content.addView(createSpacer(16))

            content.addView(createSectionHeader("CLASSIFICATION ENGINE OUTPUT", Color.parseColor("#94A3B8")))
            content.addView(createSpacer(8))
            val outputCard = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
            outputCard.addView(createMetricRow("Target Package", "com.instagram.android", Color.parseColor("#F8FAFC")))
            outputCard.addView(createDivider())
            outputCard.addView(createMetricRow(
                "Detected Screen",
                classification.screenType.name,
                if (classification.screenType.isDistractionSurface) Color.parseColor("#EF4444") else Color.parseColor("#10B981")
            ))
            outputCard.addView(createDivider())
            outputCard.addView(createMetricRow("Confidence Score", String.format("%.2f", classification.confidence), Color.parseColor("#3B82F6")))
            outputCard.addView(createDivider())

            val (decisionText, decisionColor) = when (decision) {
                is RestrictionDecision.Block -> "BLOCK" to Color.parseColor("#EF4444")
                is RestrictionDecision.Allow -> "ALLOW" to Color.parseColor("#10B981")
                is RestrictionDecision.Warn -> "WARN" to Color.parseColor("#F59E0B")
            }
            outputCard.addView(createMetricRow("Restriction Decision", decisionText, decisionColor))
            content.addView(outputCard)

            content.addView(createSpacer(16))

            content.addView(createSectionHeader("MATCHED RULES", Color.parseColor("#94A3B8")))
            content.addView(createSpacer(8))
            val rulesCard = createCardView(Color.parseColor("#131B2E"), Color.parseColor("#334155"))
            classification.matchedRules.forEach { rule ->
                rulesCard.addView(TextView(this@MainActivity).apply {
                    text = "• $rule"
                    textSize = 13f
                    typeface = Typeface.MONOSPACE
                    setTextColor(Color.parseColor("#CBD5E1"))
                })
            }
            content.addView(rulesCard)

            scroll.addView(content)
            rootContainer.addView(scroll)
        }
    }

    // ==========================================
    // UI HELPER METHODS
    // ==========================================
    private fun createCardView(bgColor: Int, borderColor: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            val bg = GradientDrawable().apply {
                setColor(bgColor)
                setStroke(dp(1), borderColor)
                cornerRadius = dp(14).toFloat()
            }
            background = bg
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun createPrimaryButton(text: String, bgColor: Int, textColor: Int, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(textColor)
            val bg = GradientDrawable().apply {
                setColor(bgColor)
                cornerRadius = dp(10).toFloat()
            }
            background = bg
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48))
            setOnClickListener { onClick() }
        }
    }

    private fun createSecondaryButton(text: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.parseColor("#CBD5E1"))
            val bg = GradientDrawable().apply {
                setColor(Color.parseColor("#1E293B"))
                setStroke(dp(1), Color.parseColor("#475569"))
                cornerRadius = dp(10).toFloat()
            }
            background = bg
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48))
            setOnClickListener { onClick() }
        }
    }

    private fun createSmallIconButton(label: String, onClick: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            setTextColor(Color.parseColor("#94A3B8"))
            setBackgroundColor(Color.TRANSPARENT)
            setPadding(dp(8), dp(4), dp(8), dp(4))
            setOnClickListener { onClick() }
        }
    }

    private fun createMetricRow(label: String, value: String, valueColor: Int): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(4), 0, dp(4))

            addView(TextView(this@MainActivity).apply {
                text = label
                textSize = 14f
                setTextColor(Color.parseColor("#94A3B8"))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(this@MainActivity).apply {
                text = value
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(valueColor)
            })
        }
    }

    private fun createSettingSwitch(title: String, subtitle: String, checked: Boolean, onToggle: (Boolean) -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(6), 0, dp(6))

            val col = LinearLayout(this@MainActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            }
            col.addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 15f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#F8FAFC"))
            })
            col.addView(TextView(this@MainActivity).apply {
                text = subtitle
                textSize = 12f
                setTextColor(Color.parseColor("#94A3B8"))
            })
            addView(col)

            val sw = Switch(this@MainActivity).apply {
                isChecked = checked
                setOnCheckedChangeListener { _, isChecked -> onToggle(isChecked) }
            }
            addView(sw)
        }
    }

    private fun createHeaderWithBack(title: String, onBack: () -> Unit): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            gravity = Gravity.CENTER_VERTICAL

            addView(Button(this@MainActivity).apply {
                text = "← Back"
                textSize = 14f
                setTextColor(Color.parseColor("#3B82F6"))
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener { onBack() }
            })
            addView(createSpacerHorizontal(10))
            addView(TextView(this@MainActivity).apply {
                text = title
                textSize = 20f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(Color.parseColor("#F8FAFC"))
            })
        }
    }

    private fun createSectionHeader(title: String, color: Int): TextView {
        return TextView(this).apply {
            text = title
            textSize = 12f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(color)
            letterSpacing = 0.1f
        }
    }

    private fun createDivider(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(1))
            setBackgroundColor(Color.parseColor("#1E293B"))
            setPadding(0, dp(6), 0, dp(6))
        }
    }

    private fun createSpacer(dp: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(dp))
        }
    }

    private fun createSpacerHorizontal(dp: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(dp(dp), ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    private fun dp(value: Int): Int {
        return (value * resources.displayMetrics.density).toInt()
    }

    private fun showEmergencyErrorView(t: Throwable) {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#090D16"))
            setPadding(dp(20), dp(40), dp(20), dp(20))
        }
        root.addView(TextView(this).apply {
            text = "⚠️ Launch Error"
            textSize = 22f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#EF4444"))
        })
        root.addView(createSpacer(12))
        root.addView(TextView(this).apply {
            text = "${t.javaClass.simpleName}: ${t.message}\n\n${Log.getStackTraceString(t)}"
            textSize = 12f
            typeface = Typeface.MONOSPACE
            setTextColor(Color.parseColor("#CBD5E1"))
        })
        setContentView(root)
    }
}
