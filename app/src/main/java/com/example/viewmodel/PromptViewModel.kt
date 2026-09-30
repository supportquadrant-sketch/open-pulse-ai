package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.PromptTemplateEntity
import com.example.data.repository.PromptRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PromptUiState(
    val selectedCategory: String = "All",
    val searchQuery: String = "",
    val showCreateDialog: Boolean = false,
    val selectedPromptForUse: PromptTemplateEntity? = null
)

class PromptViewModel(
    private val promptRepository: PromptRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PromptUiState())
    val uiState: StateFlow<PromptUiState> = _uiState.asStateFlow()

    private val _filterCategory = MutableStateFlow("All")
    private val _searchQuery = MutableStateFlow("")

    val categories: StateFlow<List<String>> = promptRepository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prompts: StateFlow<List<PromptTemplateEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isNotBlank()) {
                promptRepository.searchPrompts(query)
            } else {
                promptRepository.allPrompts
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String) {
        _filterCategory.value = category
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun searchPrompts(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun toggleFavorite(id: Long) {
        viewModelScope.launch {
            promptRepository.toggleFavorite(id)
        }
    }

    fun createPrompt(
        title: String,
        promptText: String,
        category: String = "General",
        tags: String = ""
    ) {
        viewModelScope.launch {
            val entity = PromptTemplateEntity(
                title = title,
                promptText = promptText,
                category = category,
                tags = tags
            )
            promptRepository.insertPrompt(entity)
            _uiState.value = _uiState.value.copy(showCreateDialog = false)
        }
    }

    fun usePrompt(prompt: PromptTemplateEntity) {
        viewModelScope.launch {
            promptRepository.incrementUsage(prompt.id)
            _uiState.value = _uiState.value.copy(selectedPromptForUse = prompt)
        }
    }

    fun clearSelectedPrompt() {
        _uiState.value = _uiState.value.copy(selectedPromptForUse = null)
    }

    fun deletePrompt(id: Long) {
        viewModelScope.launch {
            promptRepository.deletePrompt(id)
        }
    }

    fun toggleCreateDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCreateDialog = show)
    }
}

class PromptViewModelFactory(
    private val promptRepository: PromptRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PromptViewModel(promptRepository) as T
    }
}
