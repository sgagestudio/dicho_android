package com.sgagestudio.dicho

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.sgagestudio.dicho.presentation.home.HomeScreen
import com.sgagestudio.dicho.presentation.home.HomeViewModel
import com.sgagestudio.dicho.presentation.manual.ManualEntryScreen
import com.sgagestudio.dicho.presentation.settings.SettingsScreen
import com.sgagestudio.dicho.ui.theme.DichoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DichoTheme {
                DichoApp(viewModel = homeViewModel)
            }
        }
        handleAssistantIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAssistantIntent(intent)
    }

    private fun handleAssistantIntent(intent: Intent?) {
        val rawText = intent?.let { extractVoiceText(it) }
        if (!rawText.isNullOrBlank()) {
            homeViewModel.onVoiceInput(rawText)
        }
    }
}

private enum class AppScreen(val label: String) {
    Home("Inicio"),
    Manual("Manual"),
    Settings("Ajustes"),
}

@Composable
private fun DichoApp(viewModel: HomeViewModel) {
    var currentScreen by remember { mutableStateOf(AppScreen.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                AppScreen.entries.forEach { screen ->
                    NavigationBarItem(
                        selected = screen == currentScreen,
                        onClick = { currentScreen = screen },
                        label = { Text(text = screen.label) },
                        icon = {},
                    )
                }
            }
        },
    ) { paddingValues ->
        when (currentScreen) {
            AppScreen.Home -> HomeScreen(viewModel = viewModel, paddingValues = paddingValues)
            AppScreen.Manual -> ManualEntryScreen(viewModel = viewModel, paddingValues = paddingValues)
            AppScreen.Settings -> SettingsScreen(viewModel = viewModel, paddingValues = paddingValues)
        }
    }
}

private fun extractVoiceText(intent: Intent): String? {
    val candidates = listOfNotNull(
        intent.getStringExtra(Intent.EXTRA_TEXT),
        intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT),
        intent.getStringExtra("rawText"),
        intent.getStringExtra("query"),
        intent.getStringExtra("assistant_query"),
    )
    if (candidates.isNotEmpty()) {
        return candidates.firstOrNull { it.isNotBlank() }
    }
    return intent.extras?.getCharSequence(Intent.EXTRA_TEXT)?.toString()
}
