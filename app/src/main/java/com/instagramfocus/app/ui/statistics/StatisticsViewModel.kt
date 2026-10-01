package com.instagramfocus.app.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.instagramfocus.app.data.repository.StatisticsRepository
import com.instagramfocus.app.domain.model.StatisticsSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StatisticsViewModel(
    private val repository: StatisticsRepository
) : ViewModel() {

    val statistics: StateFlow<StatisticsSummary> = repository.summaryFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsSummary()
    )

    fun refresh() {
        viewModelScope.launch {
            repository.refreshSummary()
        }
    }

    fun clearStatistics() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }
}
