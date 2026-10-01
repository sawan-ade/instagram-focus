package com.instagramfocus.app.ui.dashboard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagramfocus.app.data.repository.FocusSettingsRepository
import com.instagramfocus.app.data.repository.StatisticsRepository
import com.instagramfocus.app.domain.model.BypassState
import com.instagramfocus.app.domain.model.FocusSettings
import com.instagramfocus.app.domain.model.StatisticsSummary
import com.instagramfocus.app.util.PermissionUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DashboardUiState(
    val settings: FocusSettings = FocusSettings(),
    val statistics: StatisticsSummary = StatisticsSummary(),
    val bypassState: BypassState = BypassState(),
    val isAccessibilityEnabled: Boolean = false,
    val showFrictionDialog: Boolean = false
)

class DashboardViewModel(
    private val settingsRepository: FocusSettingsRepository,
    private val statisticsRepository: StatisticsRepository
) : ViewModel() {

    private val _isAccessibilityEnabled = MutableStateFlow(false)
    private val _showFrictionDialog = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> = combine(
        settingsRepository.settingsFlow,
        statisticsRepository.summaryFlow,
        settingsRepository.bypassFlow,
        _isAccessibilityEnabled,
        _showFrictionDialog
    ) { settings, stats, bypass, accessEnabled, friction ->
        DashboardUiState(
            settings = settings,
            statistics = stats,
            bypassState = bypass,
            isAccessibilityEnabled = accessEnabled,
            showFrictionDialog = friction
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    init {
        // Ticker to keep bypass remaining countdown updated
        viewModelScope.launch {
            while (isActive) {
                delay(1000)
                // Trigger flow update if bypass is active
            }
        }
    }

    fun checkPermissions(context: Context) {
        _isAccessibilityEnabled.value = PermissionUtils.isAccessibilityServiceEnabled(context)
    }

    fun onToggleFocusMode(targetState: Boolean) {
        if (!targetState && uiState.value.settings.enableFrictionSurvey) {
            // Show friction dialog before disabling
            _showFrictionDialog.value = true
        } else {
            setFocusMode(targetState)
        }
    }

    fun dismissFrictionDialog() {
        _showFrictionDialog.value = false
    }

    fun confirmDisableFocusMode(reason: String) {
        _showFrictionDialog.value = false
        setFocusMode(false)
    }

    private fun setFocusMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFocusMode(enabled)
        }
    }

    fun startTemporaryBypass(minutes: Int) {
        viewModelScope.launch {
            _showFrictionDialog.value = false
            settingsRepository.activateBypass(minutes)
        }
    }

    fun cancelTemporaryBypass() {
        viewModelScope.launch {
            settingsRepository.deactivateBypass()
        }
    }

    fun refreshStatistics() {
        viewModelScope.launch {
            statisticsRepository.refreshSummary()
        }
    }
}
