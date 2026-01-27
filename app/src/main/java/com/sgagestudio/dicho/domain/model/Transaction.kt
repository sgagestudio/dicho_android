package com.sgagestudio.dicho.domain.model

import androidx.room.PrimaryKey

data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val rawText: String,
    val concept: String,
    val amount: Double,
    val currency: String,
    val expenseDate: Long,
    val recordDate: Long,
    val category: String,
    val status: TransactionStatus,
    val processingSource: ProcessingSource,
)
