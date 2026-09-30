package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.DailyTokenStat
import com.example.data.local.entity.ModelUsageStat
import com.example.data.repository.AnalyticsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class AnalyticsViewModel(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    val totalConversations: StateFlow<Int> = analyticsRepository.totalConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalMessages: StateFlow<Int> = analyticsRepository.totalMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalTokens: StateFlow<Int> = analyticsRepository.totalTokens
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val avgLatency: StateFlow<Double> = analyticsRepository.avgLatency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val topPersona: StateFlow<String?> = analyticsRepository.topPersona
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val modelUsageStats: StateFlow<List<ModelUsageStat>> = analyticsRepository.modelUsageStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dailyStats: StateFlow<List<DailyTokenStat>> = analyticsRepository.getDailyStats(14)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class AnalyticsViewModelFactory(
    private val analyticsRepository: AnalyticsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return AnalyticsViewModel(analyticsRepository) as T
    }
}
