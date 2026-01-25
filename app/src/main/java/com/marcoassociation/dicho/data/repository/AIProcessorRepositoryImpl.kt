package com.marcoassociation.dicho.data.repository

import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.marcoassociation.dicho.data.work.ProcessPendingTransactionsWorker
import com.marcoassociation.dicho.domain.model.LocalAiStatus
import com.marcoassociation.dicho.domain.model.ProcessingSource
import com.marcoassociation.dicho.domain.model.Transaction
import com.marcoassociation.dicho.domain.model.TransactionStatus
import com.marcoassociation.dicho.domain.repository.AIProcessorRepository
import com.marcoassociation.dicho.domain.repository.LocalAICapabilityChecker
import com.marcoassociation.dicho.domain.repository.NetworkMonitor
import com.marcoassociation.dicho.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class AIProcessorRepositoryImpl @Inject constructor(
    private val localAiDataSource: LocalAiDataSource,
    private val cloudAiDataSource: CloudAiDataSource,
    private val transactionRepository: com.marcoassociation.dicho.domain.repository.TransactionRepository,
    private val localAICapabilityChecker: LocalAICapabilityChecker,
    private val networkMonitor: NetworkMonitor,
    private val settingsRepository: SettingsRepository,
    private val workManager: WorkManager
) : AIProcessorRepository {
    private val localAiStatusFlow = MutableStateFlow(
        LocalAiStatus(
            supportsLocalAi = false,
            isModelAvailable = false,
            canDownloadModel = false,
            hasInternet = false
        )
    )

    override val localAiStatus: StateFlow<LocalAiStatus> = localAiStatusFlow

    override suspend fun refreshCapabilities() {
        val supportsLocalAi = localAICapabilityChecker.isHardwareSupported()
        val isModelAvailable = if (supportsLocalAi) {
            localAICapabilityChecker.isModelDownloaded()
        } else {
            false
        }
        if (supportsLocalAi && !isModelAvailable) {
            localAICapabilityChecker.triggerModelDownload()
        }
        val hasInternet = networkMonitor.hasInternetConnection()
        localAiStatusFlow.value = LocalAiStatus(
            supportsLocalAi = supportsLocalAi,
            isModelAvailable = isModelAvailable,
            canDownloadModel = supportsLocalAi && !isModelAvailable,
            hasInternet = hasInternet
        )
        if (isModelAvailable) {
            val pending = transactionRepository.getByStatus(TransactionStatus.PENDING_PROCESSING)
            if (pending.isNotEmpty()) {
                enqueuePendingWorker(requireNetwork = false)
            }
        }
    }

    override suspend fun processAndStore(rawText: String) {
        refreshCapabilities()
        val localAiEnabled = settingsRepository.useLocalAi.value
        val status = localAiStatusFlow.value
        val now = System.currentTimeMillis()
        if (localAiEnabled && status.isModelAvailable) {
            val result = localAiDataSource.parseTransaction(rawText)
            if (result.isSuccess) {
                val parsed = result.getOrThrow()
                val transaction = createTransactionFromParsed(
                    rawText = rawText,
                    parsed = parsed,
                    status = TransactionStatus.COMPLETED,
                    source = ProcessingSource.LOCAL_AI,
                    recordDate = now
                )
                transactionRepository.insert(transaction)
            } else {
                saveFailedTransaction(rawText, now, ProcessingSource.LOCAL_AI)
            }
            return
        }

        if (!status.isModelAvailable && status.hasInternet) {
            val result = cloudAiDataSource.parseTransaction(rawText)
            if (result.isSuccess) {
                val parsed = result.getOrThrow()
                val transaction = createTransactionFromParsed(
                    rawText = rawText,
                    parsed = parsed,
                    status = TransactionStatus.COMPLETED,
                    source = ProcessingSource.CLOUD_AI,
                    recordDate = now
                )
                transactionRepository.insert(transaction)
            } else {
                saveFailedTransaction(rawText, now, ProcessingSource.CLOUD_AI)
            }
            return
        }

        val pendingTransaction = Transaction(
            id = 0L,
            rawText = rawText,
            concept = rawText,
            amount = 0.0,
            currency = "EUR",
            expenseDate = now,
            recordDate = now,
            category = "Pendiente",
            status = TransactionStatus.PENDING_PROCESSING,
            processingSource = ProcessingSource.CLOUD_AI
        )
        transactionRepository.insert(pendingTransaction)
        enqueuePendingWorker(requireNetwork = true)
    }

    override suspend fun processPendingTransactions() {
        refreshCapabilities()
        val status = localAiStatusFlow.value
        val localAiEnabled = settingsRepository.useLocalAi.value
        val pending = transactionRepository.getByStatus(TransactionStatus.PENDING_PROCESSING)
        if (pending.isEmpty()) return

        if (!status.isModelAvailable && !status.hasInternet) {
            enqueuePendingWorker(requireNetwork = true)
            return
        }

        pending.forEach { transaction ->
            val result = if (localAiEnabled && status.isModelAvailable) {
                localAiDataSource.parseTransaction(transaction.rawText)
            } else {
                cloudAiDataSource.parseTransaction(transaction.rawText)
            }
            if (result.isSuccess) {
                val parsed = result.getOrThrow()
                val updated = createTransactionFromParsed(
                    rawText = transaction.rawText,
                    parsed = parsed,
                    status = TransactionStatus.COMPLETED,
                    source = if (localAiEnabled && status.isModelAvailable) {
                        ProcessingSource.LOCAL_AI
                    } else {
                        ProcessingSource.CLOUD_AI
                    },
                    recordDate = transaction.recordDate,
                    id = transaction.id
                )
                transactionRepository.update(updated)
            } else {
                val failed = transaction.copy(status = TransactionStatus.FAILED)
                transactionRepository.update(failed)
            }
        }
    }

    private fun enqueuePendingWorker(requireNetwork: Boolean) {
        val builder = OneTimeWorkRequestBuilder<ProcessPendingTransactionsWorker>()
        if (requireNetwork) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
            builder.setConstraints(constraints)
        }
        workManager.enqueue(builder.build())
    }

    private fun createTransactionFromParsed(
        rawText: String,
        parsed: com.marcoassociation.dicho.domain.model.AiParsedTransaction,
        status: TransactionStatus,
        source: ProcessingSource,
        recordDate: Long,
        id: Long = 0L
    ): Transaction {
        val expenseDate = runCatching {
            val localDate = LocalDate.parse(parsed.date)
            localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }.getOrElse {
            Instant.now().toEpochMilli()
        }
        return Transaction(
            id = id,
            rawText = rawText,
            concept = parsed.concept,
            amount = parsed.amount,
            currency = parsed.currency.ifBlank { "EUR" },
            expenseDate = expenseDate,
            recordDate = recordDate,
            category = parsed.category,
            status = status,
            processingSource = source
        )
    }

    private suspend fun saveFailedTransaction(rawText: String, now: Long, source: ProcessingSource) {
        val failedTransaction = Transaction(
            id = 0L,
            rawText = rawText,
            concept = rawText,
            amount = 0.0,
            currency = "EUR",
            expenseDate = now,
            recordDate = now,
            category = "Sin categoría",
            status = TransactionStatus.FAILED,
            processingSource = source
        )
        transactionRepository.insert(failedTransaction)
    }
}
