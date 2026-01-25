package com.marcoassociation.dicho.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY recordDate DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE status = :status")
    suspend fun getTransactionsByStatus(status: TransactionStatusEntity): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity): Long

    @Update
    suspend fun update(entity: TransactionEntity)

    @Query(
        "SELECT SUM(amount) FROM transactions WHERE expenseDate BETWEEN :from AND :to AND status = :status"
    )
    fun observeMonthlyTotal(from: Long, to: Long, status: TransactionStatusEntity): Flow<Double?>
}
