package com.sgagestudio.dicho

import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.remember
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sgagestudio.dicho.presentation.home.HomeScreen
import com.sgagestudio.dicho.presentation.home.HomeViewModel
import com.sgagestudio.dicho.presentation.manual.ManualEntryScreen
import com.sgagestudio.dicho.presentation.receipts.CaptureScreen
import com.sgagestudio.dicho.presentation.receipts.PreviewScreen
import com.sgagestudio.dicho.presentation.receipts.QueueScreen
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

private enum class AppScreen(val route: String, val label: String) {
    Home("home", "Inicio"),
    Manual("manual", "Manual"),
    Settings("settings", "Ajustes"),
}

private object ReceiptRoutes {
    const val Capture = "receipt_capture"
    const val Preview = "receipt_preview"
    const val Queue = "receipt_queue"
    const val PreviewArg = "imageUri"
}

@Composable
private fun DichoApp(viewModel: HomeViewModel) {
    val navController = rememberNavController()
    val navEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navEntry?.destination?.route
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbar by viewModel.snackbar.collectAsState()

    LaunchedEffect(snackbar) {
        snackbar?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeSnackbar()
        }
    }

    val bottomRoutes = AppScreen.entries.map { it.route }.toSet()
    val showBottomBar = currentRoute in bottomRoutes

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    AppScreen.entries.forEach { screen ->
                        NavigationBarItem(
                            selected = screen.route == currentRoute,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            label = { Text(text = screen.label) },
                            icon = {},
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = AppScreen.Home.route,
        ) {
            composable(AppScreen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    paddingValues = paddingValues,
                    onOpenCamera = { navController.navigate(ReceiptRoutes.Capture) },
                )
            }
            composable(AppScreen.Manual.route) {
                ManualEntryScreen(viewModel = viewModel, paddingValues = paddingValues)
            }
            composable(AppScreen.Settings.route) {
                SettingsScreen(viewModel = viewModel, paddingValues = paddingValues)
            }
            composable(ReceiptRoutes.Capture) {
                CaptureScreen(
                    onClose = { navController.popBackStack() },
                    onOpenQueue = { navController.navigate(ReceiptRoutes.Queue) },
                    onPhotoCaptured = { uri ->
                        navController.navigate("${ReceiptRoutes.Preview}/${Uri.encode(uri)}")
                    },
                )
            }
            composable(
                route = "${ReceiptRoutes.Preview}/{${ReceiptRoutes.PreviewArg}}",
                arguments = listOf(navArgument(ReceiptRoutes.PreviewArg) { type = NavType.StringType }),
            ) { backStackEntry ->
                val uri = backStackEntry.arguments?.getString(ReceiptRoutes.PreviewArg).orEmpty()
                val viewModel = androidx.hilt.navigation.compose.hiltViewModel<com.sgagestudio.dicho.presentation.receipts.ReceiptCaptureViewModel>()
                PreviewScreen(
                    imageUri = uri,
                    onRetry = { navController.popBackStack() },
                    onUse = {
                        viewModel.confirmImage(it)
                        navController.popBackStack(ReceiptRoutes.Capture, inclusive = false)
                    },
                )
            }
            composable(ReceiptRoutes.Queue) {
                QueueScreen(
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
