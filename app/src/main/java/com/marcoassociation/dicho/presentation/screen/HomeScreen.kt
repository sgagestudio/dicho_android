package com.marcoassociation.dicho.presentation.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marcoassociation.dicho.domain.model.Transaction
import com.marcoassociation.dicho.domain.model.TransactionStatus
import com.marcoassociation.dicho.presentation.viewmodel.HomeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToManual: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onMicTap: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.refreshCapabilities()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Dicho") },
                actions = {
                    Button(onClick = onNavigateToSettings) {
                        Text(text = "Ajustes")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onMicTap) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "Grabar")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            MonthlyTotalCard(total = state.monthlyTotal)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Últimos movimientos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = onNavigateToManual) {
                    Text(text = "Agregar manual")
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.transactions) { transaction ->
                    TransactionItem(transaction = transaction)
                }
            }
        }
    }
}

@Composable
private fun MonthlyTotalCard(total: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Gasto total del mes", style = MaterialTheme.typography.titleMedium)
            Text(
                text = String.format(Locale.getDefault(), "%.2f EUR", total),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TransactionItem(transaction: Transaction) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.concept, fontWeight = FontWeight.Bold)
                Text(
                    text = "${transaction.amount} ${transaction.currency}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = dateFormat.format(Date(transaction.expenseDate)),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Categoría: ${transaction.category}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            when (transaction.status) {
                TransactionStatus.PENDING_PROCESSING -> {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Pendiente"
                        )
                    }
                }
                TransactionStatus.COMPLETED -> {
                    Text(text = "OK", color = MaterialTheme.colorScheme.primary)
                }
                TransactionStatus.FAILED -> {
                    Text(text = "Error", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
