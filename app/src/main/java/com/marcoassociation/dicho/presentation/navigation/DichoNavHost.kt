package com.marcoassociation.dicho.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.marcoassociation.dicho.presentation.screen.HomeScreen
import com.marcoassociation.dicho.presentation.screen.ManualEntryScreen
import com.marcoassociation.dicho.presentation.screen.SettingsScreen

object Routes {
    const val HOME = "home"
    const val MANUAL = "manual"
    const val SETTINGS = "settings"
}

@Composable
fun DichoNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onMicTap: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToManual = { navController.navigate(Routes.MANUAL) },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onMicTap = onMicTap
            )
        }
        composable(Routes.MANUAL) {
            ManualEntryScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
