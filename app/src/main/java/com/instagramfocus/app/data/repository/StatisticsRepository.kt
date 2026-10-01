package com.instagramfocus.app.data.repository

import com.instagramfocus.app.data.local.FocusDatabaseHelper
import com.instagramfocus.app.domain.model.BlockEvent
import com.instagramfocus.app.domain.model.ScreenType
import com.instagramfocus.app.domain.model.StatisticsSummary
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

interface StatisticsRepository {
    val summaryFlow: StateFlow<StatisticsSummary>

    suspend fun recordBlock(screenType: ScreenType, reason: String = "", matchedRule: String = "")
    suspend fun recordSession(type: String)
    suspend fun refreshSummary()
    suspend fun clearAll()
}

class StatisticsRepositoryImpl(
    private val dbHelper: FocusDatabaseHelper,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : StatisticsRepository {

    private val _summaryFlow = MutableStateFlow(StatisticsSummary())
    override val summaryFlow: StateFlow<StatisticsSummary> = _summaryFlow.asStateFlow()

    init {
        // Initial load
        try {
            _summaryFlow.value = dbHelper.getStatisticsSummary()
        } catch (_: Exception) {}
    }

    override suspend fun recordBlock(screenType: ScreenType, reason: String, matchedRule: String) = withContext(ioDispatcher) {
        val event = BlockEvent(
            screenType = screenType,
            timestamp = System.currentTimeMillis(),
            reason = reason,
            matchedRule = matchedRule
        )
        dbHelper.recordBlockEvent(event)
        refreshSummary()
    }

    override suspend fun recordSession(type: String) = withContext(ioDispatcher) {
        dbHelper.recordSession(type)
        refreshSummary()
    }

    override suspend fun refreshSummary() = withContext(ioDispatcher) {
        val summary = dbHelper.getStatisticsSummary()
        _summaryFlow.value = summary
    }

    override suspend fun clearAll() = withContext(ioDispatcher) {
        dbHelper.clearAllData()
        _summaryFlow.value = StatisticsSummary()
    }
}
