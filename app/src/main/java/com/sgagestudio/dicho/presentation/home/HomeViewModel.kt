package com.sgagestudio.dicho.presentation.home

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgagestudio.dicho.data.export.describeSavedLocation
import com.sgagestudio.dicho.data.export.saveBytesToPublicDocuments
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

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing

    // Estado para controlar la visibilidad del overlay de voz
    private val _showVoiceOverlay = MutableStateFlow(false)
    val showVoiceOverlay: StateFlow<Boolean> = _showVoiceOverlay

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepository.observeTransactions(),
        aiProcessorRepository.localModelSupported,
        aiProcessorRepository.localModelAvailable,
        _showVoiceOverlay,
        _isProcessing
    ) { transactions, supported, available, showOverlay, processing ->
        HomeUiState(
            transactions = transactions,
            monthlyTotal = calculateMonthlyTotal(transactions),
            localAiSupported = supported,
            localModelAvailable = available,
            showVoiceOverlay = showOverlay,
            isProcessing = processing
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    // Activa la interfaz de escucha
    fun startListening() {
        _showVoiceOverlay.value = true
    }

    // Cierra la interfaz de escucha
    fun stopListening() {
        _showVoiceOverlay.value = false
    }

    fun onVoiceInput(rawText: String) {
        if (rawText.isBlank()) {
            stopListening()
            return
        }

        stopListening()
        viewModelScope.launch {
            _isProcessing.value = true
            _snackbar.value = "Analizando tu gasto..."
            try {
                aiProcessorRepository.process(rawText)
                    .onFailure { error ->
                        _snackbar.value = "Error al procesar: ${error.localizedMessage}"
                    }
                    .onSuccess {
                        _snackbar.value = "¡Gasto registrado con éxito!"
                    }
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun onManualEntry(concept: String, amount: Double, category: String, expenseDate: Long) {
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

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
            _snackbar.value = "Registro actualizado"
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(id)
            _snackbar.value = "Registro eliminado"
        }
    }

    fun exportCsv(context: Context) {
        viewModelScope.launch {
            val transactions = uiState.value.transactions
            runCatching {
                val bytes = csvExporter.export(transactions)
                val fileName = "export_dicho_fecha_hora_${System.currentTimeMillis()}.csv"
                saveBytesToPublicDocuments(
                    context = context,
                    fileName = fileName,
                    mimeType = "text/csv",
                    bytes = bytes,
                )
            }.onSuccess { uri ->
                if (uri == null) {
                    _snackbar.value = "No se pudo exportar el CSV"
                    return@onSuccess
                }
                val location = describeSavedLocation(context, uri)
                Log.i("HomeViewModel", "CSV guardado en $location")
                _snackbar.value = "CSV exportado en $location"
            }.onFailure { error ->
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
            val date = Instant.ofEpochMilli(transaction.expenseDate).atZone(ZoneId.systemDefault())
            date.month == now.month && date.year == now.year
        }.sumOf { it.amount }
    }
}

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val monthlyTotal: Double = 0.0,
    val localAiSupported: Boolean = false,
    val localModelAvailable: Boolean = false,
    val showVoiceOverlay: Boolean = false,
    val isProcessing: Boolean = false
)
