package com.sgagestudio.dicho.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ProcessingWorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun enqueuePendingProcessing() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<ProcessPendingTransactionsWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context)
            .enqueue(request)
    }
}
