package com.marcoassociation.dicho.domain.repository

import com.marcoassociation.dicho.domain.model.Transaction
import com.marcoassociation.dicho.domain.model.TransactionStatus
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>

    fun observeMonthlyTotal(from: Long, to: Long, status: TransactionStatus): Flow<Double?>

    suspend fun insert(transaction: Transaction): Long

    suspend fun update(transaction: Transaction)

    suspend fun getByStatus(status: TransactionStatus): List<Transaction>
}
