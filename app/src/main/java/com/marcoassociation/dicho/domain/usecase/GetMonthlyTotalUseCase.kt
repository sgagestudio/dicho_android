package com.marcoassociation.dicho.domain.usecase

import com.marcoassociation.dicho.domain.model.TransactionStatus
import com.marcoassociation.dicho.domain.repository.TransactionRepository
import javax.inject.Inject

class GetMonthlyTotalUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(from: Long, to: Long, status: TransactionStatus) =
        transactionRepository.observeMonthlyTotal(from, to, status)
}
