package com.sgagestudio.dicho

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        val data = intent?.data

        // 1. Caso: El usuario pulsó el acceso directo "Registrar Gasto" (abre el overlay)
        if (data?.scheme == "dicho" && data.host == "open_mic") {
            homeViewModel.startListening()
            return
        }

        // 2. Caso: Google Assistant envió texto directamente para procesar
        val rawText = intent?.let { extractVoiceText(it) }
        if (!rawText.isNullOrBlank()) {
            homeViewModel.onVoiceInput(rawText)
        }
    }

    private fun extractVoiceText(intent: Intent): String? {
        // Buscamos en todas las posibles llaves de entrada de texto
        val candidates = listOfNotNull(
            intent.getStringExtra("text_body"),  // Nuestra clave en shortcuts.xml
            intent.getStringExtra("query"),      // Estándar de Google Assistant
            intent.getStringExtra("rawText"),    // Tu clave anterior
            intent.getStringExtra(Intent.EXTRA_TEXT),
            intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT)
        )

        if (candidates.isNotEmpty()) {
            return candidates.firstOrNull { it.isNotBlank() }
        }

        return intent.extras?.getCharSequence(Intent.EXTRA_TEXT)?.toString()
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
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbar by viewModel.snackbar.collectAsState()

    LaunchedEffect(snackbar) {
        snackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
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
