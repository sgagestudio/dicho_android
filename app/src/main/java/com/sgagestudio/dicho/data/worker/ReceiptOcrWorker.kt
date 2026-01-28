package com.sgagestudio.dicho.data.worker

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sgagestudio.dicho.data.camera.ReceiptImageStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class ReceiptOcrWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val uriString = inputData.getString(ReceiptWorkKeys.KEY_IMAGE_URI)
            ?: return Result.failure()
        val uri = Uri.parse(uriString)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return try {
            val image = InputImage.fromFilePath(applicationContext, uri)
            val visionText = withContext(Dispatchers.IO) {
                Tasks.await(recognizer.process(image))
            }
            val text = visionText.text.orEmpty()
            if (text.isBlank()) {
                Log.w("ReceiptOcrWorker", "OCR sin texto.")
                Result.failure()
            } else {
                Result.success(workDataOf(ReceiptWorkKeys.KEY_OCR_TEXT to text))
            }
        } catch (error: Exception) {
            Log.e("ReceiptOcrWorker", "Error en OCR.", error)
            Result.failure()
        } finally {
            ReceiptImageStore.deleteImageUri(applicationContext, uri)
            runCatching { recognizer.close() }
        }
    }
}
