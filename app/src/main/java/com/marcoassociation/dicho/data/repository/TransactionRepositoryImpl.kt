package com.marcoassociation.dicho.data.repository

import com.marcoassociation.dicho.data.local.TransactionDao
import com.marcoassociation.dicho.domain.model.Transaction
import com.marcoassociation.dicho.domain.model.TransactionStatus
import com.marcoassociation.dicho.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao
) : TransactionRepository {
    override fun observeTransactions(): Flow<List<Transaction>> {
        return transactionDao.observeTransactions().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeMonthlyTotal(
        from: Long,
        to: Long,
        status: TransactionStatus
    ): Flow<Double?> {
        return transactionDao.observeMonthlyTotal(from, to, status.toEntity())
    }

    override suspend fun insert(transaction: Transaction): Long {
        return transactionDao.insert(transaction.toEntity())
    }

    override suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction.toEntity())
    }

    override suspend fun getByStatus(status: TransactionStatus): List<Transaction> {
        return transactionDao.getTransactionsByStatus(status.toEntity()).map { it.toDomain() }
    }
}
