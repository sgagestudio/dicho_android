package com.sgagestudio.dicho.presentation.manual

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sgagestudio.dicho.presentation.home.HomeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun ManualEntryScreen(
    viewModel: HomeViewModel,
    paddingValues: PaddingValues,
) {
    var concept by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var dateText by remember { mutableStateOf(LocalDate.now().toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        OutlinedTextField(
            value = concept,
            onValueChange = { concept = it },
            label = { Text("Concepto") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Monto") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Categoría") },
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = dateText,
            onValueChange = { dateText = it },
            label = { Text("Fecha (YYYY-MM-DD)") },
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                val parsedAmount = amount.replace(",", ".").toDoubleOrNull() ?: 0.0
                val parsedDate = runCatching {
                    LocalDate.parse(dateText).atStartOfDay(ZoneId.systemDefault()).toInstant()
                }.getOrElse { Instant.now() }
                viewModel.onManualEntry(
                    concept = concept,
                    amount = parsedAmount,
                    category = category.ifBlank { "Otros" },
                    expenseDate = parsedDate.toEpochMilli(),
                )
                concept = ""
                amount = ""
                category = ""
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Guardar")
        }
    }
}
