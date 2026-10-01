package com.instagramfocus.app.data.repository

import com.instagramfocus.app.data.preferences.FocusPreferences
import com.instagramfocus.app.domain.model.BypassState
import com.instagramfocus.app.domain.model.FocusSettings
import kotlinx.coroutines.flow.Flow

interface FocusSettingsRepository {
    val settingsFlow: Flow<FocusSettings>
    val bypassFlow: Flow<BypassState>

    suspend fun setFocusMode(enabled: Boolean)
    suspend fun setBlockReels(block: Boolean)
    suspend fun setBlockExplore(block: Boolean)
    suspend fun setBlockFeed(block: Boolean)
    suspend fun setBlockSuggestedContent(block: Boolean)
    suspend fun setAllowDms(allow: Boolean)
    suspend fun setAllowStories(allow: Boolean)
    suspend fun setAllowFollowRequests(allow: Boolean)
    suspend fun setAllowProfiles(allow: Boolean)
    suspend fun setAllowNotifications(allow: Boolean)
    suspend fun setStrictMode(enabled: Boolean)
    suspend fun setFrictionSurvey(enabled: Boolean)
    suspend fun setBypassDuration(minutes: Int)
    suspend fun completeOnboarding()

    suspend fun activateBypass(durationMinutes: Int)
    suspend fun deactivateBypass()
}

class FocusSettingsRepositoryImpl(
    private val preferences: FocusPreferences
) : FocusSettingsRepository {

    override val settingsFlow: Flow<FocusSettings> = preferences.settingsFlow
    override val bypassFlow: Flow<BypassState> = preferences.bypassFlow

    override suspend fun setFocusMode(enabled: Boolean) = preferences.updateFocusMode(enabled)
    override suspend fun setBlockReels(block: Boolean) = preferences.updateBlockReels(block)
    override suspend fun setBlockExplore(block: Boolean) = preferences.updateBlockExplore(block)
    override suspend fun setBlockFeed(block: Boolean) = preferences.updateBlockFeed(block)
    override suspend fun setBlockSuggestedContent(block: Boolean) = preferences.updateBlockSuggestedContent(block)
    override suspend fun setAllowDms(allow: Boolean) = preferences.updateAllowDms(allow)
    override suspend fun setAllowStories(allow: Boolean) = preferences.updateAllowStories(allow)
    override suspend fun setAllowFollowRequests(allow: Boolean) = preferences.updateAllowFollowRequests(allow)
    override suspend fun setAllowProfiles(allow: Boolean) = preferences.updateAllowProfiles(allow)
    override suspend fun setAllowNotifications(allow: Boolean) = preferences.updateAllowNotifications(allow)
    override suspend fun setStrictMode(enabled: Boolean) = preferences.updateStrictMode(enabled)
    override suspend fun setFrictionSurvey(enabled: Boolean) = preferences.updateFrictionSurvey(enabled)
    override suspend fun setBypassDuration(minutes: Int) = preferences.updateBypassDuration(minutes)
    override suspend fun completeOnboarding() = preferences.setOnboardingCompleted(true)

    override suspend fun activateBypass(durationMinutes: Int) = preferences.startBypass(durationMinutes)
    override suspend fun deactivateBypass() = preferences.cancelBypass()
}
