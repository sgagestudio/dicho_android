package com.sgagestudio.dicho.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.OutOfQuotaPolicy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ReceiptQueueWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun enqueueProcessing(immediate: Boolean = false) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val requestBuilder = OneTimeWorkRequestBuilder<ProcessReceiptQueueWorker>()
            .setConstraints(constraints)
        if (immediate) {
            requestBuilder.setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
        }
        val request = requestBuilder.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_NAME,
            ExistingWorkPolicy.APPEND,
            request,
        )
    }

    companion object {
        private const val WORK_NAME = "receipt_queue_processing"
    }
}
