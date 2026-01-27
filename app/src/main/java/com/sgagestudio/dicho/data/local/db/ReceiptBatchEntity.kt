package com.sgagestudio.dicho.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sgagestudio.dicho.domain.model.ReceiptBatchStatus

@Entity(tableName = "receipt_batches")
data class ReceiptBatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val createdAt: Long,
    val status: ReceiptBatchStatus,
)
