package com.sgagestudio.dicho.data.repository

import com.sgagestudio.dicho.data.local.db.ReceiptBatchEntity
import com.sgagestudio.dicho.data.local.db.ReceiptImageJobEntity
import com.sgagestudio.dicho.domain.model.ReceiptBatch
import com.sgagestudio.dicho.domain.model.ReceiptImageJob

fun ReceiptBatchEntity.toDomain(): ReceiptBatch = ReceiptBatch(
    id = id,
    createdAt = createdAt,
    status = status,
)

fun ReceiptImageJobEntity.toDomain(): ReceiptImageJob = ReceiptImageJob(
    id = id,
    batchId = batchId,
    imageUri = imageUri,
    status = status,
    ocrText = ocrText,
    geminiRaw = geminiRaw,
    parsedData = parsedData,
    errorMessage = errorMessage,
    createdAt = createdAt,
)

fun ReceiptImageJob.toEntity(): ReceiptImageJobEntity = ReceiptImageJobEntity(
    id = id,
    batchId = batchId,
    imageUri = imageUri,
    status = status,
    ocrText = ocrText,
    geminiRaw = geminiRaw,
    parsedData = parsedData,
    errorMessage = errorMessage,
    createdAt = createdAt,
)
