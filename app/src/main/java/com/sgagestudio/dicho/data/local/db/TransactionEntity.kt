package com.sgagestudio.dicho.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sgagestudio.dicho.domain.model.ProcessingSource
import com.sgagestudio.dicho.domain.model.TransactionStatus

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val rawText: String,
    val concept: String,
    val amount: Double,
    val currency: String = "EUR",
    val expenseDate: Long,
    val recordDate: Long,
    val category: String,
    val status: TransactionStatus,
    val processingSource: ProcessingSource,
)
