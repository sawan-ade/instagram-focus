package com.instagramfocus.app.domain.usecase

import com.instagramfocus.app.data.repository.FocusSettingsRepository
import com.instagramfocus.app.domain.model.BypassState
import kotlinx.coroutines.flow.Flow

class ManageBypassUseCase(
    private val settingsRepository: FocusSettingsRepository
) {
    val bypassStateFlow: Flow<BypassState> = settingsRepository.bypassFlow

    suspend fun startBypass(durationMinutes: Int) {
        settingsRepository.activateBypass(durationMinutes)
    }

    suspend fun cancelBypass() {
        settingsRepository.deactivateBypass()
    }
}
