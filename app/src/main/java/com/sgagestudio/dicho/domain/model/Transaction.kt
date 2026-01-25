package com.sgagestudio.dicho.domain.model

data class Transaction(
    val id: Long,
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
