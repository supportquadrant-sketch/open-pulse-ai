package com.example.data.repository

import com.example.data.local.dao.PromptDao
import com.example.data.local.entity.PromptTemplateEntity
import kotlinx.coroutines.flow.Flow

class PromptRepository(private val promptDao: PromptDao) {
    val allPrompts: Flow<List<PromptTemplateEntity>> = promptDao.getAllPrompts()
    val categories: Flow<List<String>> = promptDao.getCategories()

    fun getPromptsByCategory(category: String): Flow<List<PromptTemplateEntity>> =
        promptDao.getPromptsByCategory(category)

    fun searchPrompts(query: String): Flow<List<PromptTemplateEntity>> =
        promptDao.searchPrompts(query)

    suspend fun insertPrompt(prompt: PromptTemplateEntity): Long =
        promptDao.insertPrompt(prompt)

    suspend fun updatePrompt(prompt: PromptTemplateEntity) =
        promptDao.updatePrompt(prompt)

    suspend fun toggleFavorite(id: Long) =
        promptDao.toggleFavorite(id)

    suspend fun incrementUsage(id: Long) =
        promptDao.incrementUsage(id)

    suspend fun deletePrompt(id: Long) =
        promptDao.deletePromptById(id)
}
