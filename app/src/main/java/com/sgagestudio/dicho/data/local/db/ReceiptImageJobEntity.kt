package com.sgagestudio.dicho.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sgagestudio.dicho.domain.model.ReceiptJobStatus

@Entity(tableName = "receipt_jobs")
data class ReceiptImageJobEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val batchId: Long?,
    val imageUri: String,
    val status: ReceiptJobStatus,
    val ocrText: String?,
    val geminiRaw: String?,
    val parsedData: String?,
    val errorMessage: String?,
    val createdAt: Long,
)
