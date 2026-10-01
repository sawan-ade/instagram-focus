package com.instagramfocus.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagramfocus.app.data.repository.FocusSettingsRepository
import com.instagramfocus.app.domain.model.FocusSettings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: FocusSettingsRepository
) : ViewModel() {

    val settings: StateFlow<FocusSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FocusSettings()
    )

    fun setBlockReels(block: Boolean) = viewModelScope.launch { repository.setBlockReels(block) }
    fun setBlockExplore(block: Boolean) = viewModelScope.launch { repository.setBlockExplore(block) }
    fun setBlockFeed(block: Boolean) = viewModelScope.launch { repository.setBlockFeed(block) }
    fun setBlockSuggestedContent(block: Boolean) = viewModelScope.launch { repository.setBlockSuggestedContent(block) }

    fun setAllowDms(allow: Boolean) = viewModelScope.launch { repository.setAllowDms(allow) }
    fun setAllowStories(allow: Boolean) = viewModelScope.launch { repository.setAllowStories(allow) }
    fun setAllowFollowRequests(allow: Boolean) = viewModelScope.launch { repository.setAllowFollowRequests(allow) }
    fun setAllowProfiles(allow: Boolean) = viewModelScope.launch { repository.setAllowProfiles(allow) }
    fun setAllowNotifications(allow: Boolean) = viewModelScope.launch { repository.setAllowNotifications(allow) }

    fun setStrictMode(enabled: Boolean) = viewModelScope.launch { repository.setStrictMode(enabled) }
    fun setFrictionSurvey(enabled: Boolean) = viewModelScope.launch { repository.setFrictionSurvey(enabled) }
    fun setBypassDuration(minutes: Int) = viewModelScope.launch { repository.setBypassDuration(minutes) }
}
