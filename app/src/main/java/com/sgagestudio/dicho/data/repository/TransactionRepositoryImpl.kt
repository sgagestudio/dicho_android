package com.sgagestudio.dicho.data.repository

import com.sgagestudio.dicho.data.local.db.TransactionDao
import com.sgagestudio.dicho.domain.model.Transaction
import com.sgagestudio.dicho.domain.model.TransactionStatus
import com.sgagestudio.dicho.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionRepository {
    override fun observeTransactions(): Flow<List<Transaction>> {
        return transactionDao.observeTransactions().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun upsertTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    override suspend fun insertTransaction(transaction: Transaction): Long {
        return transactionDao.insertTransaction(transaction.toEntity())
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.updateTransaction(transaction.toEntity())
    }

    override suspend fun deleteTransaction(id: Long) {
        transactionDao.deleteTransaction(id)
    }

    override suspend fun getPendingTransactions(): List<Transaction> {
        return transactionDao.getTransactionsByStatus(TransactionStatus.PENDING_PROCESSING.name)
            .map { it.toDomain() }
    }
}
