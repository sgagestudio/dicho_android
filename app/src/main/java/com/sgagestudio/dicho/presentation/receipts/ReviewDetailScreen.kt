package com.sgagestudio.dicho.presentation.receipts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.launch

@Composable
fun ReviewDetailScreen(
    jobId: Long,
    onBack: () -> Unit,
    viewModel: ReceiptReviewViewModel = hiltViewModel(),
) {
    val drafts by viewModel.drafts.collectAsState()
    val draft = drafts[jobId]
    val job = viewModel.findJob(jobId)
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Revisar factura") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (job != null) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(job.imageUri)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Factura",
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            OutlinedTextField(
                value = draft?.concept.orEmpty(),
                onValueChange = {
                    viewModel.updateDraft(
                        jobId,
                        (draft ?: ReceiptDraft("", "", "EUR", "Otros", ""))
                            .copy(concept = it),
                    )
                },
                label = { Text("Concepto") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = draft?.amount.orEmpty(),
                onValueChange = {
                    viewModel.updateDraft(
                        jobId,
                        (draft ?: ReceiptDraft("", "", "EUR", "Otros", ""))
                            .copy(amount = it),
                    )
                },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
            OutlinedTextField(
                value = draft?.currency.orEmpty(),
                onValueChange = {
                    viewModel.updateDraft(
                        jobId,
                        (draft ?: ReceiptDraft("", "", "EUR", "Otros", ""))
                            .copy(currency = it),
                    )
                },
                label = { Text("Moneda") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = draft?.expenseDate.orEmpty(),
                onValueChange = {
                    viewModel.updateDraft(
                        jobId,
                        (draft ?: ReceiptDraft("", "", "EUR", "Otros", ""))
                            .copy(expenseDate = it),
                    )
                },
                label = { Text("Fecha (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = draft?.category.orEmpty(),
                onValueChange = {
                    viewModel.updateDraft(
                        jobId,
                        (draft ?: ReceiptDraft("", "", "EUR", "Otros", ""))
                            .copy(category = it),
                    )
                },
                label = { Text("Categoría") },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { scope.launch { viewModel.persistDraft(jobId) } },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = "Guardar cambios")
            }
        }
    }
}
