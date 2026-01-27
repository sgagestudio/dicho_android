package com.sgagestudio.dicho.presentation.receipts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.sgagestudio.dicho.domain.model.ReceiptImageJob

@Composable
fun ReceiptJobDetailScreen(
    jobId: Long,
    onBack: () -> Unit,
    viewModel: ReceiptQueueViewModel = hiltViewModel(),
) {
    var job by remember { mutableStateOf<ReceiptImageJob?>(null) }
    LaunchedEffect(jobId) {
        job = viewModel.getJob(jobId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Detalle de factura") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        val currentJob = job
        if (currentJob == null) {
            Text(
                text = "Cargando...",
                modifier = Modifier.padding(padding).padding(24.dp),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(currentJob.imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Factura",
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(text = "Estado: ${currentJob.status}", style = MaterialTheme.typography.titleMedium)
                currentJob.ocrText?.let {
                    Text(text = "OCR:", style = MaterialTheme.typography.titleSmall)
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
                currentJob.errorMessage?.let {
                    Text(text = "Error: $it", color = MaterialTheme.colorScheme.error)
                }
                currentJob.parsedData?.let {
                    Text(text = "Datos extraídos:", style = MaterialTheme.typography.titleSmall)
                    Text(text = it, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
