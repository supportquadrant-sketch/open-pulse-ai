package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.PromptTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompt_templates ORDER BY isFavorite DESC, usageCount DESC, createdAt DESC")
    fun getAllPrompts(): Flow<List<PromptTemplateEntity>>

    @Query("SELECT * FROM prompt_templates WHERE category = :category ORDER BY isFavorite DESC, usageCount DESC")
    fun getPromptsByCategory(category: String): Flow<List<PromptTemplateEntity>>

    @Query("SELECT DISTINCT category FROM prompt_templates")
    fun getCategories(): Flow<List<String>>

    @Query("SELECT * FROM prompt_templates WHERE title LIKE '%' || :query || '%' OR promptText LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%'")
    fun searchPrompts(query: String): Flow<List<PromptTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptTemplateEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPrompts(prompts: List<PromptTemplateEntity>)

    @Update
    suspend fun updatePrompt(prompt: PromptTemplateEntity)

    @Query("UPDATE prompt_templates SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: Long)

    @Query("UPDATE prompt_templates SET usageCount = usageCount + 1 WHERE id = :id")
    suspend fun incrementUsage(id: Long)

    @Query("DELETE FROM prompt_templates WHERE id = :id")
    suspend fun deletePromptById(id: Long)

    @Query("SELECT COUNT(*) FROM prompt_templates")
    suspend fun getPromptCount(): Int
}
