package com.instagramfocus.app.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.instagramfocus.app.domain.model.BypassState
import com.instagramfocus.app.domain.model.FocusSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

interface FocusPreferences {
    val settingsFlow: Flow<FocusSettings>
    val bypassFlow: Flow<BypassState>

    suspend fun updateFocusMode(enabled: Boolean)
    suspend fun updateBlockReels(block: Boolean)
    suspend fun updateBlockExplore(block: Boolean)
    suspend fun updateBlockFeed(block: Boolean)
    suspend fun updateBlockSuggestedContent(block: Boolean)
    suspend fun updateAllowDms(allow: Boolean)
    suspend fun updateAllowStories(allow: Boolean)
    suspend fun updateAllowFollowRequests(allow: Boolean)
    suspend fun updateAllowProfiles(allow: Boolean)
    suspend fun updateAllowNotifications(allow: Boolean)
    suspend fun updateStrictMode(enabled: Boolean)
    suspend fun updateFrictionSurvey(enabled: Boolean)
    suspend fun updateBypassDuration(minutes: Int)
    suspend fun setOnboardingCompleted(completed: Boolean)

    suspend fun startBypass(durationMinutes: Int)
    suspend fun cancelBypass()
}

class FocusPreferencesImpl(private val context: Context) : FocusPreferences {

    private val prefs: SharedPreferences = context.getSharedPreferences("instagram_focus_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(readSettings())
    override val settingsFlow: Flow<FocusSettings> = _settingsFlow.asStateFlow()

    private val _bypassFlow = MutableStateFlow(readBypass())
    override val bypassFlow: Flow<BypassState> = _bypassFlow.asStateFlow()

    private fun readSettings(): FocusSettings {
        return FocusSettings(
            isFocusModeEnabled = prefs.getBoolean("focus_mode_enabled", true),
            blockReels = prefs.getBoolean("block_reels", true),
            blockExplore = prefs.getBoolean("block_explore", true),
            blockFeed = prefs.getBoolean("block_feed", true),
            blockSuggestedContent = prefs.getBoolean("block_suggested", true),
            allowDms = prefs.getBoolean("allow_dms", true),
            allowStories = prefs.getBoolean("allow_stories", true),
            allowFollowRequests = prefs.getBoolean("allow_follow_requests", true),
            allowProfiles = prefs.getBoolean("allow_profiles", true),
            allowNotifications = prefs.getBoolean("allow_notifications", true),
            isStrictModeEnabled = prefs.getBoolean("strict_mode", false),
            enableFrictionSurvey = prefs.getBoolean("friction_survey", true),
            bypassDurationMinutes = prefs.getInt("bypass_duration", 10),
            hasCompletedOnboarding = prefs.getBoolean("onboarding_completed", false)
        )
    }

    private fun readBypass(): BypassState {
        val active = prefs.getBoolean("bypass_active", false)
        val expiresAt = prefs.getLong("bypass_expires_at", 0L)
        val totalMinutes = prefs.getInt("bypass_total_minutes", 0)
        return BypassState(
            isActive = active && System.currentTimeMillis() < expiresAt,
            expiresAtTimestamp = expiresAt,
            totalDurationMinutes = totalMinutes
        )
    }

    private fun persistSettings(newSettings: FocusSettings) {
        prefs.edit().apply {
            putBoolean("focus_mode_enabled", newSettings.isFocusModeEnabled)
            putBoolean("block_reels", newSettings.blockReels)
            putBoolean("block_explore", newSettings.blockExplore)
            putBoolean("block_feed", newSettings.blockFeed)
            putBoolean("block_suggested", newSettings.blockSuggestedContent)
            putBoolean("allow_dms", newSettings.allowDms)
            putBoolean("allow_stories", newSettings.allowStories)
            putBoolean("allow_follow_requests", newSettings.allowFollowRequests)
            putBoolean("allow_profiles", newSettings.allowProfiles)
            putBoolean("allow_notifications", newSettings.allowNotifications)
            putBoolean("strict_mode", newSettings.isStrictModeEnabled)
            putBoolean("friction_survey", newSettings.enableFrictionSurvey)
            putInt("bypass_duration", newSettings.bypassDurationMinutes)
            putBoolean("onboarding_completed", newSettings.hasCompletedOnboarding)
            apply()
        }
        _settingsFlow.value = newSettings
    }

    override suspend fun updateFocusMode(enabled: Boolean) {
        persistSettings(_settingsFlow.value.copy(isFocusModeEnabled = enabled))
    }

    override suspend fun updateBlockReels(block: Boolean) {
        persistSettings(_settingsFlow.value.copy(blockReels = block))
    }

    override suspend fun updateBlockExplore(block: Boolean) {
        persistSettings(_settingsFlow.value.copy(blockExplore = block))
    }

    override suspend fun updateBlockFeed(block: Boolean) {
        persistSettings(_settingsFlow.value.copy(blockFeed = block))
    }

    override suspend fun updateBlockSuggestedContent(block: Boolean) {
        persistSettings(_settingsFlow.value.copy(blockSuggestedContent = block))
    }

    override suspend fun updateAllowDms(allow: Boolean) {
        persistSettings(_settingsFlow.value.copy(allowDms = allow))
    }

    override suspend fun updateAllowStories(allow: Boolean) {
        persistSettings(_settingsFlow.value.copy(allowStories = allow))
    }

    override suspend fun updateAllowFollowRequests(allow: Boolean) {
        persistSettings(_settingsFlow.value.copy(allowFollowRequests = allow))
    }

    override suspend fun updateAllowProfiles(allow: Boolean) {
        persistSettings(_settingsFlow.value.copy(allowProfiles = allow))
    }

    override suspend fun updateAllowNotifications(allow: Boolean) {
        persistSettings(_settingsFlow.value.copy(allowNotifications = allow))
    }

    override suspend fun updateStrictMode(enabled: Boolean) {
        persistSettings(_settingsFlow.value.copy(isStrictModeEnabled = enabled))
    }

    override suspend fun updateFrictionSurvey(enabled: Boolean) {
        persistSettings(_settingsFlow.value.copy(enableFrictionSurvey = enabled))
    }

    override suspend fun updateBypassDuration(minutes: Int) {
        persistSettings(_settingsFlow.value.copy(bypassDurationMinutes = minutes))
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        persistSettings(_settingsFlow.value.copy(hasCompletedOnboarding = completed))
    }

    override suspend fun startBypass(durationMinutes: Int) {
        val expiresAt = System.currentTimeMillis() + (durationMinutes * 60 * 1000L)
        prefs.edit().apply {
            putBoolean("bypass_active", true)
            putLong("bypass_expires_at", expiresAt)
            putInt("bypass_total_minutes", durationMinutes)
            apply()
        }
        _bypassFlow.value = BypassState(
            isActive = true,
            expiresAtTimestamp = expiresAt,
            totalDurationMinutes = durationMinutes
        )
    }

    override suspend fun cancelBypass() {
        prefs.edit().apply {
            putBoolean("bypass_active", false)
            putLong("bypass_expires_at", 0L)
            apply()
        }
        _bypassFlow.value = BypassState(
            isActive = false,
            expiresAtTimestamp = 0L,
            totalDurationMinutes = 0
        )
    }
}
