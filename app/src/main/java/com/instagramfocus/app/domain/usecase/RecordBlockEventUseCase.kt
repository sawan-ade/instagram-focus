package com.instagramfocus.app.domain.usecase

import com.instagramfocus.app.data.repository.StatisticsRepository
import com.instagramfocus.app.domain.model.ScreenType

class RecordBlockEventUseCase(
    private val statisticsRepository: StatisticsRepository
) {
    suspend operator fun invoke(screenType: ScreenType, reason: String = "", ruleMatched: String = "") {
        statisticsRepository.recordBlock(screenType, reason, ruleMatched)
    }
}
