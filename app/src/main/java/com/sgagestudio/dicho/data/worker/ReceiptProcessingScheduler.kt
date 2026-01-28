package com.sgagestudio.dicho.data.worker

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OverwritingInputMerger
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ReceiptProcessingScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun enqueue(imageUriString: String) {
        val ocrWork = OneTimeWorkRequestBuilder<ReceiptOcrWorker>()
            .setInputData(workDataOf(ReceiptWorkKeys.KEY_IMAGE_URI to imageUriString))
            .build()
        val geminiWork = OneTimeWorkRequestBuilder<ReceiptGeminiWorker>()
            .setInputMerger(OverwritingInputMerger::class)
            .build()

        WorkManager.getInstance(context)
            .beginWith(ocrWork)
            .then(geminiWork)
            .enqueue()
    }
}
