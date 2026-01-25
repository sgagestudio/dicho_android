package com.marcoassociation.dicho.domain.usecase

import com.marcoassociation.dicho.domain.repository.TransactionRepository
import javax.inject.Inject

class ObserveTransactionsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke() = transactionRepository.observeTransactions()
}
