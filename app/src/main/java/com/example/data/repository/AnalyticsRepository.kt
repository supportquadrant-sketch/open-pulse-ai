package com.example.data.repository

import com.example.data.local.dao.AnalyticsDao
import com.example.data.local.entity.DailyTokenStat
import com.example.data.local.entity.ModelUsageStat
import com.example.data.local.entity.TokenUsageSnapshot
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class AnalyticsRepository(private val analyticsDao: AnalyticsDao) {
    val totalConversations: Flow<Int> = analyticsDao.getTotalConversations()
    val totalMessages: Flow<Int> = analyticsDao.getTotalMessages()
    val totalTokens: Flow<Int> = analyticsDao.getTotalTokens()
    val avgLatency: Flow<Double> = analyticsDao.getAverageLatency()
    val topPersona: Flow<String?> = analyticsDao.getMostUsedPersona()
    val modelUsageStats: Flow<List<ModelUsageStat>> = analyticsDao.getModelUsageStats()

    fun getDailyStats(days: Int = 14): Flow<List<DailyTokenStat>> {
        val sinceTimestamp = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return analyticsDao.getDailyStats(sinceTimestamp)
    }

    suspend fun recordSnapshot(snapshot: TokenUsageSnapshot): Long =
        analyticsDao.recordSnapshot(snapshot)
}
