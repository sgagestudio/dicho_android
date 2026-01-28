package com.sgagestudio.dicho.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sgagestudio.dicho.domain.usecase.ProcessTextInputUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ReceiptGeminiWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val processTextInputUseCase: ProcessTextInputUseCase,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val text = inputData.getString(ReceiptWorkKeys.KEY_OCR_TEXT).orEmpty()
        if (text.isBlank()) {
            Log.w("ReceiptGeminiWorker", "Texto OCR vacío.")
            return Result.failure()
        }
        return processTextInputUseCase.processTextInput(text)
            .fold(
                onSuccess = { Result.success() },
                onFailure = { error ->
                    Log.e("ReceiptGeminiWorker", "Error procesando con Gemini.", error)
                    Result.failure()
                },
            )
    }
}
