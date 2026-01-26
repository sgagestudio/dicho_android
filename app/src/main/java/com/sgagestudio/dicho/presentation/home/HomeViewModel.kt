package com.sgagestudio.dicho.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgagestudio.dicho.domain.model.ProcessingSource
import com.sgagestudio.dicho.domain.model.Transaction
import com.sgagestudio.dicho.domain.model.TransactionStatus
import com.sgagestudio.dicho.domain.repository.AIProcessorRepository
import com.sgagestudio.dicho.domain.repository.CsvExporter
import com.sgagestudio.dicho.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val aiProcessorRepository: AIProcessorRepository,
    private val csvExporter: CsvExporter,
) : ViewModel() {
    private val _snackbar = MutableStateFlow<String?>(null)
    val snackbar: StateFlow<String?> = _snackbar

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepository.observeTransactions(),
        aiProcessorRepository.localModelSupported,
        aiProcessorRepository.localModelAvailable,
    ) { transactions, supported, available ->
        val monthlyTotal = calculateMonthlyTotal(transactions)
        HomeUiState(
            transactions = transactions,
            monthlyTotal = monthlyTotal,
            localAiSupported = supported,
            localModelAvailable = available,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun onVoiceInput(rawText: String) {
        if (rawText.isBlank()) return // Evita procesar silencios

        viewModelScope.launch {
            _snackbar.value = "Analizando tu gasto..." // Feedback inmediato
            aiProcessorRepository.process(rawText)
                .onFailure { error ->
                    _snackbar.value = "Error al procesar: ${error.localizedMessage}"
                }
                .onSuccess {
                    _snackbar.value = "¡Gasto registrado con éxito!"
                }
        }
    }

    fun onManualEntry(
        concept: String,
        amount: Double,
        category: String,
        expenseDate: Long,
    ) {
        viewModelScope.launch {
            val recordDate = Instant.now().toEpochMilli()
            val transaction = Transaction(
                id = 0L,
                rawText = concept,
                concept = concept,
                amount = amount,
                currency = "EUR",
                expenseDate = expenseDate,
                recordDate = recordDate,
                category = category,
                status = TransactionStatus.COMPLETED,
                processingSource = ProcessingSource.MANUAL,
            )
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun exportCsv(outputDir: File) {
        viewModelScope.launch {
            val transactions = uiState.value.transactions
            runCatching { csvExporter.export(transactions, outputDir) }
                .onSuccess { file ->
                    _snackbar.value = "CSV exportado en ${file.absolutePath}"
                }
                .onFailure { error ->
                    _snackbar.value = "Error al exportar: ${error.message}"
                }
        }
    }

    fun refreshCapabilities() {
        viewModelScope.launch { aiProcessorRepository.refreshCapabilities() }
    }

    fun consumeSnackbar() {
        _snackbar.value = null
    }

    private fun calculateMonthlyTotal(transactions: List<Transaction>): Double {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        return transactions.filter { transaction ->
            val date = Instant.ofEpochMilli(transaction.expenseDate)
                .atZone(ZoneId.systemDefault())
            date.month == now.month && date.year == now.year
        }.sumOf { it.amount }
    }
}


data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val monthlyTotal: Double = 0.0,
    val localAiSupported: Boolean = false,
    val localModelAvailable: Boolean = false,
)
