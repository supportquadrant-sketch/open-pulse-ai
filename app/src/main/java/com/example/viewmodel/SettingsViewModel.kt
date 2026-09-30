package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AppSettings
import com.example.data.repository.ChatRepository
import com.example.data.repository.SettingsRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = settingsRepository.settings

    fun updateApiKey(apiKey: String) {
        settingsRepository.updateApiKey(apiKey)
    }

    fun updateDefaultModel(model: String) {
        settingsRepository.updateDefaultModel(model)
    }

    fun updateTemperature(temp: Float) {
        settingsRepository.updateTemperature(temp)
    }

    fun updateStreaming(enabled: Boolean) {
        settingsRepository.updateStreaming(enabled)
    }

    fun updateTurboSpeed(enabled: Boolean) {
        settingsRepository.updateTurboSpeed(enabled)
    }

    fun updateDarkTheme(isDark: Boolean?) {
        settingsRepository.updateDarkTheme(isDark)
    }

    fun clearAllChatHistory() {
        viewModelScope.launch {
            chatRepository.clearAllConversations()
        }
    }
}

class SettingsViewModelFactory(
    private val settingsRepository: SettingsRepository,
    private val chatRepository: ChatRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(settingsRepository, chatRepository) as T
    }
}
