package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppSettings(
    val customApiKey: String = "",
    val defaultModel: String = "openpulse-fast-lite",
    val defaultTemperature: Float = 0.7f,
    val topP: Float = 0.95f,
    val isStreamingEnabled: Boolean = true,
    val isDarkTheme: Boolean? = null, // null means follow system
    val isTurboSpeedEnabled: Boolean = true
)

class SettingsRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("openpulse_settings", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    private fun loadSettings(): AppSettings {
        val darkThemeValue = if (prefs.contains(KEY_DARK_THEME)) {
            prefs.getBoolean(KEY_DARK_THEME, true)
        } else null

        return AppSettings(
            customApiKey = prefs.getString(KEY_API_KEY, "") ?: "",
            defaultModel = prefs.getString(KEY_MODEL, "openpulse-fast-lite") ?: "openpulse-fast-lite",
            defaultTemperature = prefs.getFloat(KEY_TEMP, 0.7f),
            topP = prefs.getFloat(KEY_TOP_P, 0.95f),
            isStreamingEnabled = prefs.getBoolean(KEY_STREAMING, true),
            isDarkTheme = darkThemeValue,
            isTurboSpeedEnabled = prefs.getBoolean(KEY_TURBO_SPEED, true)
        )
    }

    fun updateApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
        _settings.value = _settings.value.copy(customApiKey = apiKey.trim())
    }

    fun updateDefaultModel(model: String) {
        prefs.edit().putString(KEY_MODEL, model).apply()
        _settings.value = _settings.value.copy(defaultModel = model)
    }

    fun updateTemperature(temp: Float) {
        prefs.edit().putFloat(KEY_TEMP, temp).apply()
        _settings.value = _settings.value.copy(defaultTemperature = temp)
    }

    fun updateStreaming(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_STREAMING, enabled).apply()
        _settings.value = _settings.value.copy(isStreamingEnabled = enabled)
    }

    fun updateTurboSpeed(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_TURBO_SPEED, enabled).apply()
        _settings.value = _settings.value.copy(isTurboSpeedEnabled = enabled)
    }

    fun updateDarkTheme(isDark: Boolean?) {
        if (isDark == null) {
            prefs.edit().remove(KEY_DARK_THEME).apply()
        } else {
            prefs.edit().putBoolean(KEY_DARK_THEME, isDark).apply()
        }
        _settings.value = _settings.value.copy(isDarkTheme = isDark)
    }

    companion object {
        private const val KEY_API_KEY = "custom_api_key"
        private const val KEY_MODEL = "default_model"
        private const val KEY_TEMP = "default_temperature"
        private const val KEY_TOP_P = "top_p"
        private const val KEY_STREAMING = "streaming_enabled"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_TURBO_SPEED = "turbo_speed"
    }
}
