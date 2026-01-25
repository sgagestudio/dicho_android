package com.sgagestudio.dicho.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sgagestudio.dicho.domain.model.ProcessingSource
import com.sgagestudio.dicho.domain.model.Transaction
import com.sgagestudio.dicho.domain.model.TransactionStatus
import com.sgagestudio.dicho.domain.repository.AIProcessorRepository
import com.sgagestudio.dicho.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ProcessPendingTransactionsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val aiProcessorRepository: AIProcessorRepository,
    private val transactionRepository: TransactionRepository,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val pending = transactionRepository.getPendingTransactions()
        if (pending.isEmpty()) return Result.success()

        aiProcessorRepository.refreshCapabilities()

        pending.forEach { transaction ->
            val result = aiProcessorRepository.process(transaction.rawText)
            result.fold(
                onSuccess = { (processed, source) ->
                    transactionRepository.upsertTransaction(
                        processed.copy(
                            id = transaction.id,
                            recordDate = transaction.recordDate,
                            status = TransactionStatus.COMPLETED,
                            processingSource = source,
                        )
                    )
                },
                onFailure = {
                    transactionRepository.upsertTransaction(
                        transaction.copy(
                            status = TransactionStatus.FAILED,
                            processingSource = ProcessingSource.CLOUD_AI,
                        )
                    )
                }
            )
        }
        return Result.success()
    }
}
