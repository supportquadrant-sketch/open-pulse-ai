package com.example.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.screens.analytics.AnalyticsScreen
import com.example.ui.screens.chat.ChatScreen
import com.example.ui.screens.personas.PersonaStudioScreen
import com.example.ui.screens.prompts.PromptLibraryScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.viewmodel.AnalyticsViewModel
import com.example.viewmodel.ChatViewModel
import com.example.viewmodel.PersonaViewModel
import com.example.viewmodel.PromptViewModel
import com.example.viewmodel.SettingsViewModel

enum class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CHAT("Chat", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline),
    ANALYTICS("Analytics", Icons.Filled.Analytics, Icons.Outlined.Analytics),
    PERSONAS("Personas", Icons.Filled.SmartToy, Icons.Outlined.SmartToy),
    PROMPTS("Prompts", Icons.Filled.Tune, Icons.Outlined.Tune),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun AppNavigation(
    chatViewModel: ChatViewModel,
    analyticsViewModel: AnalyticsViewModel,
    personaViewModel: PersonaViewModel,
    promptViewModel: PromptViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(NavigationTab.CHAT) }

    // Handle back button on secondary screens to return to chat
    if (currentTab != NavigationTab.CHAT) {
        BackHandler {
            currentTab = NavigationTab.CHAT
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                NavigationTab.CHAT -> {
                    ChatScreen(
                        chatViewModel = chatViewModel,
                        promptViewModel = promptViewModel,
                        onNavigateToPersonas = { currentTab = NavigationTab.PERSONAS },
                        onNavigateToPrompts = { currentTab = NavigationTab.PROMPTS }
                    )
                }
                NavigationTab.ANALYTICS -> {
                    AnalyticsScreen(analyticsViewModel = analyticsViewModel)
                }
                NavigationTab.PERSONAS -> {
                    PersonaStudioScreen(
                        personaViewModel = personaViewModel,
                        chatViewModel = chatViewModel,
                        onSelectAndOpenChat = { currentTab = NavigationTab.CHAT }
                    )
                }
                NavigationTab.PROMPTS -> {
                    PromptLibraryScreen(
                        promptViewModel = promptViewModel,
                        onUsePromptInChat = { currentTab = NavigationTab.CHAT }
                    )
                }
                NavigationTab.SETTINGS -> {
                    SettingsScreen(settingsViewModel = settingsViewModel)
                }
            }
        }
    }
}
