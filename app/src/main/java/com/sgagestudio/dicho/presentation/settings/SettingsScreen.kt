package com.sgagestudio.dicho.presentation.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sgagestudio.dicho.presentation.home.HomeViewModel

@Composable
fun SettingsScreen(
    viewModel: HomeViewModel,
    paddingValues: PaddingValues,
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(text = "IA Local")
        Switch(
            checked = uiState.localModelAvailable,
            onCheckedChange = { viewModel.refreshCapabilities() },
        )
        Text(
            text = if (uiState.localAiSupported) {
                if (uiState.localModelAvailable) {
                    "Modelo local disponible"
                } else {
                    "Compatible, descarga pendiente"
                }
            } else {
                "No compatible con IA local"
            }
        )
        Button(
            onClick = {
                val outputDir = context.getExternalFilesDir(null) ?: context.cacheDir
                viewModel.exportCsv(outputDir)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Exportar a CSV")
        }
    }
}
