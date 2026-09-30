package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.local.AppDatabase
import com.example.data.repository.AnalyticsRepository
import com.example.data.repository.ChatRepository
import com.example.data.repository.PersonaRepository
import com.example.data.repository.PromptRepository
import com.example.data.repository.SettingsRepository
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.OpenPulseAITheme
import com.example.viewmodel.AnalyticsViewModel
import com.example.viewmodel.AnalyticsViewModelFactory
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.ChatViewModelFactory
import com.example.viewmodel.PersonaViewModel
import com.example.viewmodel.PersonaViewModelFactory
import com.example.viewmodel.PromptViewModel
import com.example.viewmodel.PromptViewModelFactory
import com.example.viewmodel.SettingsViewModel
import com.example.viewmodel.SettingsViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getInstance(this)
        val settingsRepository = SettingsRepository(this)
        val chatRepository = ChatRepository(database.conversationDao(), database.messageDao())
        val personaRepository = PersonaRepository(database.personaDao())
        val promptRepository = PromptRepository(database.promptDao())
        val analyticsRepository = AnalyticsRepository(database.analyticsDao())

        val chatViewModel: ChatViewModel by viewModels {
            ChatViewModelFactory(chatRepository, personaRepository, settingsRepository)
        }
        val analyticsViewModel: AnalyticsViewModel by viewModels {
            AnalyticsViewModelFactory(analyticsRepository)
        }
        val personaViewModel: PersonaViewModel by viewModels {
            PersonaViewModelFactory(personaRepository)
        }
        val promptViewModel: PromptViewModel by viewModels {
            PromptViewModelFactory(promptRepository)
        }
        val settingsViewModel: SettingsViewModel by viewModels {
            SettingsViewModelFactory(settingsRepository, chatRepository)
        }

        setContent {
            val settings by settingsViewModel.settings.collectAsState()
            val isDark = settings.isDarkTheme ?: isSystemInDarkTheme()

            OpenPulseAITheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(
                        chatViewModel = chatViewModel,
                        analyticsViewModel = analyticsViewModel,
                        personaViewModel = personaViewModel,
                        promptViewModel = promptViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
