package com.instagramfocus.app.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.instagramfocus.app.FocusApplication
import com.instagramfocus.app.domain.classifier.InstagramAdapter
import com.instagramfocus.app.domain.model.FocusSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Filters out distracting promotional and Reels notifications from Instagram
 * while keeping Direct Messages, Story mentions, and Follow Requests intact.
 *
 * Privacy Guarantees:
 * - 100% on-device analysis
 * - Message contents are NEVER stored, logged, or transmitted
 * - Zero network calls
 */
class FocusNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        if (sbn == null) return

        // Only process notifications from Instagram
        if (sbn.packageName != InstagramAdapter.PACKAGE_NAME) return

        serviceScope.launch {
            try {
                val container = FocusApplication.instance.container
                val settings: FocusSettings = container.settingsRepository.settingsFlow.first()

                // If Focus Mode is OFF or notifications aren't filtered, ignore
                if (!settings.isFocusModeEnabled || !settings.allowNotifications) return@launch

                val extras = sbn.notification.extras
                val title = extras.getCharSequence("android.title")?.toString()?.lowercase() ?: ""
                val text = extras.getCharSequence("android.text")?.toString()?.lowercase() ?: ""

                // 1. Check if it's a protected communication notification (DO NOT CANCEL)
                val isDm = title.contains("sent you a message") || 
                           text.contains("sent you a message") || 
                           title.contains("message request") || 
                           text.contains("sent a photo") ||
                           text.contains("sent a video") ||
                           text.contains("voice message")

                val isFollowRequest = title.contains("requested to follow") || 
                                     text.contains("requested to follow") ||
                                     title.contains("follow request")

                val isMention = title.contains("mentioned you") || 
                                text.contains("mentioned you")

                if (isDm || isFollowRequest || isMention) {
                    // Safe communication notification: keep visible
                    return@launch
                }

                // 2. Check if it's an entertainment/distraction notification
                val isDistraction = title.contains("reel") ||
                                    text.contains("reel") ||
                                    title.contains("suggested") ||
                                    text.contains("suggested") ||
                                    text.contains("watch their video") ||
                                    text.contains("haven't seen posts from") ||
                                    text.contains("popular on instagram")

                if (isDistraction && settings.blockReels) {
                    // Suppress distraction notification to protect user attention
                    cancelNotification(sbn.key)
                }
            } catch (_: Exception) {
                // Safe failure: do not crash
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}
