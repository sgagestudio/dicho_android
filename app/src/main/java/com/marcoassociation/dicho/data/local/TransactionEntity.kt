package com.marcoassociation.dicho.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val rawText: String,
    val concept: String,
    val amount: Double,
    val currency: String,
    val expenseDate: Long,
    val recordDate: Long,
    val category: String,
    val status: TransactionStatusEntity,
    val processingSource: ProcessingSourceEntity
)

enum class TransactionStatusEntity {
    PENDING_PROCESSING,
    COMPLETED,
    FAILED
}

enum class ProcessingSourceEntity {
    LOCAL_AI,
    CLOUD_AI,
    MANUAL
}
