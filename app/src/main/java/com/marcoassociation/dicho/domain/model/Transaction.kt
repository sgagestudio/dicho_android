package com.marcoassociation.dicho.domain.model

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
    val processingSource: ProcessingSource
)

enum class TransactionStatus {
    PENDING_PROCESSING,
    COMPLETED,
    FAILED
}

enum class ProcessingSource {
    LOCAL_AI,
    CLOUD_AI,
    MANUAL
}
