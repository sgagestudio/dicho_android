package com.sgagestudio.dicho

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.sgagestudio.dicho.presentation.home.HomeScreen
import com.sgagestudio.dicho.presentation.home.HomeViewModel
import com.sgagestudio.dicho.presentation.manual.ManualEntryScreen
import com.sgagestudio.dicho.presentation.settings.SettingsScreen
import com.sgagestudio.dicho.ui.theme.DichoTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DichoTheme {
                DichoApp()
            }
        }
    }
}

private enum class AppScreen(val label: String) {
    Home("Inicio"),
    Manual("Manual"),
    Settings("Ajustes"),
}

@Composable
private fun DichoApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.Home) }
    val viewModel: HomeViewModel = hiltViewModel()

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
