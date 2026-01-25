package com.marcoassociation.dicho.presentation.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.marcoassociation.dicho.presentation.viewmodel.ManualEntryViewModel
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManualEntryViewModel = hiltViewModel()
) {
    var concept by rememberSaveable { mutableStateOf("") }
    var amount by rememberSaveable { mutableStateOf("") }
    var currency by rememberSaveable { mutableStateOf("EUR") }
    var date by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var category by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro manual") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = concept,
                onValueChange = { concept = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Concepto") }
            )
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monto") }
            )
            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Moneda") }
            )
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Fecha (YYYY-MM-DD)") }
            )
            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Categoría") }
            )
            Button(
                onClick = {
                    val parsedAmount = amount.toDoubleOrNull() ?: 0.0
                    val parsedDate = runCatching {
                        LocalDate.parse(date)
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    }.getOrElse { System.currentTimeMillis() }
                    viewModel.saveManualEntry(
                        concept = concept,
                        amount = parsedAmount,
                        currency = currency,
                        date = parsedDate,
                        category = category
                    )
                    onNavigateBack()
                },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(text = "Guardar")
            }
        }
    }
}
