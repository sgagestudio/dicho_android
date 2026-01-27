package com.sgagestudio.dicho.presentation.receipts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@Composable
fun ReviewScreen(
    onBack: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    viewModel: ReceiptReviewViewModel = hiltViewModel(),
) {
    val jobs by viewModel.readyJobs.collectAsState()
    val drafts by viewModel.drafts.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Revisión") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch { viewModel.confirmAll() }
                        },
                        enabled = jobs.isNotEmpty(),
                    ) {
                        Icon(imageVector = Icons.Filled.DoneAll, contentDescription = "Confirmar todo")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            if (jobs.isEmpty()) {
                Text(
                    text = "No hay facturas listas para revisar.",
                    modifier = Modifier.padding(24.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(jobs, key = { it.id }) { job ->
                        val draft = drafts[job.id]
                        Card(
                            onClick = { onOpenDetail(job.id) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = draft?.concept ?: "Factura #${job.id}",
                                    style = MaterialTheme.typography.titleMedium,
                                )
                                Text(
                                    text = "Monto: ${draft?.amount ?: "--"} ${draft?.currency ?: ""}",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                                Text(
                                    text = "Fecha: ${draft?.expenseDate ?: "--"}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }
            if (jobs.isNotEmpty()) {
                Button(
                    onClick = { scope.launch { viewModel.confirmAll() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                ) {
                    Text(text = "Confirmar todo")
                }
            }
        }
    }
}
