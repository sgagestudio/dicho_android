package com.marcoassociation.dicho.domain.usecase

import com.marcoassociation.dicho.domain.model.ProcessingSource
import com.marcoassociation.dicho.domain.model.Transaction
import com.marcoassociation.dicho.domain.model.TransactionStatus
import com.marcoassociation.dicho.domain.repository.TransactionRepository
import javax.inject.Inject

class AddManualTransactionUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend operator fun invoke(
        concept: String,
        amount: Double,
        currency: String,
        date: Long,
        category: String
    ) {
        val transaction = Transaction(
            id = 0L,
            rawText = concept,
            concept = concept,
            amount = amount,
            currency = currency,
            expenseDate = date,
            recordDate = System.currentTimeMillis(),
            category = category,
            status = TransactionStatus.COMPLETED,
            processingSource = ProcessingSource.MANUAL
        )
        transactionRepository.insert(transaction)
    }
}
