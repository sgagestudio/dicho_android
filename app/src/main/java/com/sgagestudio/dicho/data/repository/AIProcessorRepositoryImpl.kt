package com.sgagestudio.dicho.data.repository

import com.sgagestudio.dicho.data.local.LocalAiProcessor
import com.sgagestudio.dicho.data.local.NetworkMonitor
import com.sgagestudio.dicho.data.remote.GeminiClient
import com.sgagestudio.dicho.data.worker.ProcessingWorkScheduler
import com.sgagestudio.dicho.domain.ai.LocalAICapabilityChecker
import com.sgagestudio.dicho.domain.model.ProcessingSource
import com.sgagestudio.dicho.domain.model.Transaction
import com.sgagestudio.dicho.domain.model.TransactionStatus
import com.sgagestudio.dicho.domain.repository.AIProcessorRepository
import com.sgagestudio.dicho.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class AIProcessorRepositoryImpl @Inject constructor(
    private val localAICapabilityChecker: LocalAICapabilityChecker,
    private val localAiProcessor: LocalAiProcessor,
    private val geminiClient: GeminiClient,
    private val networkMonitor: NetworkMonitor,
    private val transactionRepository: TransactionRepository,
    private val workScheduler: ProcessingWorkScheduler,
) : AIProcessorRepository {
    private val _localModelAvailable = MutableStateFlow(false)
    override val localModelAvailable: StateFlow<Boolean> = _localModelAvailable

    private val _localModelSupported = MutableStateFlow(false)
    override val localModelSupported: StateFlow<Boolean> = _localModelSupported

    override suspend fun refreshCapabilities() {
        val supported = localAICapabilityChecker.isLocalAiSupported()
        _localModelSupported.value = supported
        _localModelAvailable.value = supported && localAICapabilityChecker.isLocalModelDownloaded()
    }

    override suspend fun process(rawText: String): Result<Pair<List<Transaction>, ProcessingSource>> {
        refreshCapabilities()
        val hasInternet = networkMonitor.isConnected()
        val recordDate = Instant.now().toEpochMilli()

        return runCatching {
            when {
                localModelAvailable.value -> {
                    val payloads = localAiProcessor.extractTransaction(rawText)
                    val transactions = payloads.map { payload ->
                        payload.toTransaction(
                            rawText = rawText,
                            recordDate = recordDate,
                            status = TransactionStatus.COMPLETED,
                            source = ProcessingSource.LOCAL_AI,
                        )
                    }
                    if (transactions.isEmpty()) error("Respuesta vacía del modelo local.")
                    val saved = transactions.map { transaction ->
                        val id = transactionRepository.insertTransaction(transaction)
                        transaction.copy(id = id)
                    }
                    saved to ProcessingSource.LOCAL_AI
                }
                !localModelAvailable.value && hasInternet -> {
                    val payloads = geminiClient.extractTransaction(rawText)
                    val transactions = payloads.map { payload ->
                        payload.toTransaction(
                            rawText = rawText,
                            recordDate = recordDate,
                            status = TransactionStatus.COMPLETED,
                            source = ProcessingSource.CLOUD_AI,
                        )
                    }
                    if (transactions.isEmpty()) error("Respuesta vacía del modelo remoto.")
                    val saved = transactions.map { transaction ->
                        val id = transactionRepository.insertTransaction(transaction)
                        transaction.copy(id = id)
                    }
                    saved to ProcessingSource.CLOUD_AI
                }
                else -> {
                    val transaction = Transaction(
                        id = 0L,
                        rawText = rawText,
                        concept = rawText.take(48),
                        amount = 0.0,
                        currency = "EUR",
                        expenseDate = recordDate,
                        recordDate = recordDate,
                        category = "Pendiente",
                        status = TransactionStatus.PENDING_PROCESSING,
                        processingSource = ProcessingSource.CLOUD_AI,
                    )
                    val id = transactionRepository.insertTransaction(transaction)
                    workScheduler.enqueuePendingProcessing()
                    listOf(transaction.copy(id = id)) to ProcessingSource.CLOUD_AI
                }
            }
        }
    }

    private fun com.sgagestudio.dicho.data.remote.AiTransactionPayload.toTransaction(
        rawText: String,
        recordDate: Long,
        status: TransactionStatus,
        source: ProcessingSource,
    ): Transaction {
        val expenseDateMillis = runCatching {
            val date = LocalDate.parse(expenseDate, DateTimeFormatter.ISO_DATE)
            date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrElse { recordDate }
        return Transaction(
            id = 0L,
            rawText = rawText,
            concept = concept,
            amount = amount,
            currency = currency,
            expenseDate = expenseDateMillis,
            recordDate = recordDate,
            category = category,
            status = status,
            processingSource = source,
        )
    }
}
