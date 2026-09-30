package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token_analytics")
data class TokenUsageSnapshot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateString: String, // Format: YYYY-MM-DD
    val tokenCount: Int,
    val promptCount: Int,
    val avgLatencyMs: Long,
    val modelId: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class DailyTokenStat(
    val dateString: String,
    val totalTokens: Int,
    val totalMessages: Int
)

data class ModelUsageStat(
    val modelId: String,
    val count: Int,
    val totalTokens: Int
)

data class OverallAnalyticsSummary(
    val totalConversations: Int,
    val totalMessages: Int,
    val totalTokens: Int,
    val avgLatencyMs: Long,
    val topModel: String,
    val topPersona: String
)
