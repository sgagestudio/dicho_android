package com.marcoassociation.dicho.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.marcoassociation.dicho.domain.repository.AIProcessorRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ProcessPendingTransactionsWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val aiProcessorRepository: AIProcessorRepository
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        return runCatching {
            aiProcessorRepository.processPendingTransactions()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() }
        )
    }
}
