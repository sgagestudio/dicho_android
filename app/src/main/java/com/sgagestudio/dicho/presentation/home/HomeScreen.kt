package com.sgagestudio.dicho.presentation.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.sgagestudio.dicho.domain.model.Transaction
import com.sgagestudio.dicho.domain.model.TransactionStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    paddingValues: PaddingValues = PaddingValues(),
    onOpenCamera: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    var isListening by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    var editTransaction by remember { mutableStateOf<Transaction?>(null) }
    var deleteTransaction by remember { mutableStateOf<Transaction?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            isListening = true
        } else {
            isListening = false
            viewModel.stopListening()
        }
    }

    val voiceInputManager = rememberVoiceInputManager(
        onResult = { text ->
            isListening = false
            viewModel.stopListening()
            viewModel.onVoiceInput(text)
        },
        onError = {
            isListening = false
            viewModel.stopListening()
        }
    )

    LaunchedEffect(uiState.showVoiceOverlay) {
        if (uiState.showVoiceOverlay) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO,
            ) == PackageManager.PERMISSION_GRANTED
            if (hasPermission) {
                isListening = true
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        } else {
            isListening = false
        }
    }

    LaunchedEffect(isListening) {
        if (isListening) {
            voiceInputManager.startListening()
        }
    }

    Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        Column(modifier = Modifier.fillMaxSize()) {
            MonthlySummary(total = uiState.monthlyTotal)
            TransactionList(
                transactions = uiState.transactions,
                onEdit = { editTransaction = it },
                onDelete = { deleteTransaction = it },
            )
        }

        FloatingActionButton(
            onClick = {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.startListening()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp),
        ) {
            Icon(imageVector = Icons.Filled.Mic, contentDescription = "Hablar")
        }

        FloatingActionButton(
            onClick = onOpenCamera,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Icon(imageVector = Icons.Filled.PhotoCamera, contentDescription = "Cámara")
        }

        VoiceOverlay(isListening = uiState.showVoiceOverlay)
    }

    editTransaction?.let { transaction ->
        EditTransactionDialog(
            transaction = transaction,
            onDismiss = { editTransaction = null },
            onConfirm = { updated ->
                viewModel.updateTransaction(updated)
                editTransaction = null
            },
        )
    }

    deleteTransaction?.let { transaction ->
        DeleteTransactionDialog(
            transaction = transaction,
            onDismiss = { deleteTransaction = null },
            onConfirm = {
                viewModel.deleteTransaction(transaction.id)
                deleteTransaction = null
            },
        )
    }
}

@Composable
private fun MonthlySummary(total: Double) {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Gasto mensual", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "€ %.2f".format(total),
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

@Composable
private fun TransactionList(
    transactions: List<Transaction>,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("dd MMM")
    }
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(transactions, key = { it.id }) { transaction ->
            TransactionRow(
                transaction = transaction,
                formatter = formatter,
                onEdit = onEdit,
                onDelete = onDelete,
            )
        }
    }
}

@Composable
private fun TransactionRow(
    transaction: Transaction,
    formatter: DateTimeFormatter,
    onEdit: (Transaction) -> Unit,
    onDelete: (Transaction) -> Unit,
) {
    val dateText = formatter.format(
        Instant.ofEpochMilli(transaction.expenseDate).atZone(ZoneId.systemDefault()),
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.concept, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${transaction.category} · $dateText",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "${transaction.currency} %.2f".format(transaction.amount))
                if (transaction.status == TransactionStatus.PENDING_PROCESSING) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Text(text = "Procesando", modifier = Modifier.padding(start = 8.dp))
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { onEdit(transaction) }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    IconButton(onClick = { onDelete(transaction) }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFD32F2F),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VoiceOverlay(isListening: Boolean) {
    if (!isListening) return
    val statusText = "Escuchando..."
    val primary = MaterialTheme.colorScheme.primary
    val transition = rememberInfiniteTransition(label = "voiceWaves")
    val waves = listOf(
        transition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(0),
            ),
            label = "wave1",
        ),
        transition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(300),
            ),
            label = "wave2",
        ),
        transition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart,
                initialStartOffset = StartOffset(600),
            ),
            label = "wave3",
        ),
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0f, 0f, 0f, 0.7f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center,
            ) {
                waves.forEach { wave ->
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .scale(wave.value)
                            .alpha(0.6f)
                            .border(2.dp, primary, CircleShape),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(primary, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = statusText, color = Color.White)
        }
    }
}

@Composable
private fun EditTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: (Transaction) -> Unit,
) {
    var concept by remember(transaction) { mutableStateOf(transaction.concept) }
    var amountText by remember(transaction) { mutableStateOf(transaction.amount.toString()) }
    var category by remember(transaction) { mutableStateOf(transaction.category) }
    val amount = amountText.replace(",", ".").toDoubleOrNull()
    val isValid = concept.isNotBlank() && category.isNotBlank() && amount != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Editar registro") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                TextField(
                    value = concept,
                    onValueChange = { concept = it },
                    label = { Text(text = "Concepto") },
                )
                TextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text(text = "Monto") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
                TextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text(text = "Categoría") },
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updated = transaction.copy(
                        concept = concept.trim(),
                        amount = amount ?: transaction.amount,
                        category = category.trim(),
                        rawText = concept.trim(),
                    )
                    onConfirm(updated)
                },
                enabled = isValid,
            ) {
                Text(text = "Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        },
    )
}

@Composable
private fun DeleteTransactionDialog(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Eliminar registro") },
        text = { Text(text = "¿Deseas eliminar este registro?") },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(text = "Eliminar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        },
    )
}
