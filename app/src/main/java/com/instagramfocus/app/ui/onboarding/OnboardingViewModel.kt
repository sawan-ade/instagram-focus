package com.instagramfocus.app.ui.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagramfocus.app.data.repository.FocusSettingsRepository
import com.instagramfocus.app.util.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val currentStep: Int = 0,
    val isAccessibilityEnabled: Boolean = false,
    val isNotificationListenerEnabled: Boolean = false,
    val canDrawOverlays: Boolean = false,
    val isInstagramInstalled: Boolean = true
)

class OnboardingViewModel(
    private val settingsRepository: FocusSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun refreshPermissions(context: Context) {
        _uiState.value = _uiState.value.copy(
            isAccessibilityEnabled = PermissionUtils.isAccessibilityServiceEnabled(context),
            isNotificationListenerEnabled = PermissionUtils.isNotificationListenerEnabled(context),
            canDrawOverlays = PermissionUtils.canDrawOverlays(context),
            isInstagramInstalled = PermissionUtils.isInstagramInstalled(context)
        )
    }

    fun nextStep() {
        if (_uiState.value.currentStep < 3) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep + 1)
        }
    }

    fun previousStep() {
        if (_uiState.value.currentStep > 0) {
            _uiState.value = _uiState.value.copy(currentStep = _uiState.value.currentStep - 1)
        }
    }

    fun completeOnboarding(onFinished: () -> Unit) {
        viewModelScope.launch {
            settingsRepository.completeOnboarding()
            onFinished()
        }
    }
}
