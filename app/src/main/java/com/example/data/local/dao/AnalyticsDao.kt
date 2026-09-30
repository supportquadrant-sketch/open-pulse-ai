package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.DailyTokenStat
import com.example.data.local.entity.ModelUsageStat
import com.example.data.local.entity.TokenUsageSnapshot
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun recordSnapshot(snapshot: TokenUsageSnapshot): Long

    @Query("""
        SELECT 
            strftime('%Y-%m-%d', timestamp / 1000, 'unixepoch') as dateString,
            COALESCE(SUM(tokenUsage), 0) as totalTokens,
            COUNT(*) as totalMessages
        FROM messages 
        WHERE timestamp >= :sinceTimestamp
        GROUP BY dateString 
        ORDER BY dateString ASC
    """)
    fun getDailyStats(sinceTimestamp: Long): Flow<List<DailyTokenStat>>

    @Query("""
        SELECT 
            COALESCE(modelUsed, 'openpulse-3.5-flash') as modelId,
            COUNT(*) as count,
            COALESCE(SUM(tokenUsage), 0) as totalTokens
        FROM messages 
        WHERE role = 'assistant'
        GROUP BY modelUsed
        ORDER BY count DESC
    """)
    fun getModelUsageStats(): Flow<List<ModelUsageStat>>

    @Query("SELECT COUNT(*) FROM conversations")
    fun getTotalConversations(): Flow<Int>

    @Query("SELECT COUNT(*) FROM messages")
    fun getTotalMessages(): Flow<Int>

    @Query("SELECT COALESCE(SUM(tokenUsage), 0) FROM messages")
    fun getTotalTokens(): Flow<Int>

    @Query("SELECT COALESCE(AVG(latencyMs), 0) FROM messages WHERE role = 'assistant' AND latencyMs > 0")
    fun getAverageLatency(): Flow<Double>

    @Query("""
        SELECT COALESCE(personaName, 'Default Assistant') 
        FROM conversations 
        GROUP BY personaName 
        ORDER BY COUNT(*) DESC 
        LIMIT 1
    """)
    fun getMostUsedPersona(): Flow<String?>
}
