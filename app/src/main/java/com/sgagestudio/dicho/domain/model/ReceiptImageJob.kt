package com.sgagestudio.dicho.domain.model

data class ReceiptImageJob(
    val id: Long,
    val batchId: Long?,
    val imageUri: String,
    val status: ReceiptJobStatus,
    val ocrText: String?,
    val geminiRaw: String?,
    val parsedData: String?,
    val errorMessage: String?,
    val createdAt: Long,
)
