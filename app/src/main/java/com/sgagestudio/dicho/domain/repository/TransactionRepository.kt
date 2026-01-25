package com.sgagestudio.dicho.domain.repository

import com.sgagestudio.dicho.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun observeTransactions(): Flow<List<Transaction>>
    suspend fun upsertTransaction(transaction: Transaction)
    suspend fun insertTransaction(transaction: Transaction): Long
    suspend fun getPendingTransactions(): List<Transaction>
}
