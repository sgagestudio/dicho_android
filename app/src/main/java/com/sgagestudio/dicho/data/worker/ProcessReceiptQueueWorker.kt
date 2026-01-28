package com.sgagestudio.dicho.data.worker

import android.content.Context
import android.net.Uri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.sgagestudio.dicho.data.remote.AiTransactionPayload
import com.sgagestudio.dicho.data.remote.GeminiClient
import com.sgagestudio.dicho.domain.model.ReceiptJobStatus
import com.sgagestudio.dicho.domain.repository.ReceiptQueueRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltWorker
class ProcessReceiptQueueWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val receiptQueueRepository: ReceiptQueueRepository,
    private val geminiClient: GeminiClient,
    private val json: Json,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        while (true) {
            val job = receiptQueueRepository.getNextQueuedJob() ?: break
            runCatching {
                receiptQueueRepository.updateJobStatus(job.id, ReceiptJobStatus.PROCESSING)
                val ocrText = recognizeText(job.imageUri)
                receiptQueueRepository.updateJobStatus(
                    job.id,
                    ReceiptJobStatus.OCR_DONE,
                    ocrText = ocrText,
                )
                val payload = geminiClient.extractTransaction(ocrText)
                val serialized = json.encodeToString(AiTransactionPayload.serializer(), payload)
                receiptQueueRepository.updateJobStatus(
                    job.id,
                    ReceiptJobStatus.GEMINI_DONE,
                    geminiRaw = serialized,
                    parsedData = serialized,
                )
            }.onFailure { error ->
                receiptQueueRepository.updateJobStatus(
                    job.id,
                    ReceiptJobStatus.FAILED,
                    errorMessage = error.message ?: "Error inesperado",
                )
            }
        }
        return Result.success()
    }

    private suspend fun recognizeText(uriString: String): String {
        val image = InputImage.fromFilePath(context, Uri.parse(uriString))
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        return recognizer.process(image).await().text
    }
}

private suspend fun <T> com.google.android.gms.tasks.Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnSuccessListener { continuation.resume(it) }
        addOnFailureListener { continuation.resumeWithException(it) }
    }
}
