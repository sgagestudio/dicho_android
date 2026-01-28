package com.sgagestudio.dicho.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sgagestudio.dicho.domain.model.ReceiptBatchStatus
import com.sgagestudio.dicho.domain.model.ReceiptJobStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptQueueDao {
    @Query("SELECT * FROM receipt_batches WHERE status = :status ORDER BY createdAt DESC LIMIT 1")
    suspend fun getBatchByStatus(status: ReceiptBatchStatus): ReceiptBatchEntity?

    @Query("SELECT * FROM receipt_batches WHERE id = :batchId LIMIT 1")
    suspend fun getBatchById(batchId: Long): ReceiptBatchEntity?

    @Insert
    suspend fun insertBatch(entity: ReceiptBatchEntity): Long

    @Update
    suspend fun updateBatch(entity: ReceiptBatchEntity)

    @Query("SELECT * FROM receipt_jobs WHERE batchId = :batchId ORDER BY createdAt ASC")
    fun observeJobs(batchId: Long): Flow<List<ReceiptImageJobEntity>>

    @Query("SELECT * FROM receipt_jobs WHERE batchId = :batchId AND status = :status ORDER BY createdAt ASC")
    fun observeJobsByStatus(batchId: Long, status: ReceiptJobStatus): Flow<List<ReceiptImageJobEntity>>

    @Insert
    suspend fun insertJob(entity: ReceiptImageJobEntity): Long

    @Update
    suspend fun updateJob(entity: ReceiptImageJobEntity)

    @Query("SELECT * FROM receipt_jobs WHERE status = :status ORDER BY createdAt ASC LIMIT 1")
    suspend fun getNextJobByStatus(status: ReceiptJobStatus): ReceiptImageJobEntity?

    @Query("SELECT * FROM receipt_jobs WHERE id = :jobId LIMIT 1")
    suspend fun getJobById(jobId: Long): ReceiptImageJobEntity?

    @Query(
        """
        UPDATE receipt_jobs
        SET status = :status,
            ocrText = COALESCE(:ocrText, ocrText),
            geminiRaw = COALESCE(:geminiRaw, geminiRaw),
            parsedData = COALESCE(:parsedData, parsedData),
            errorMessage = COALESCE(:errorMessage, errorMessage)
        WHERE id = :jobId
        """
    )
    suspend fun updateJobStatus(
        jobId: Long,
        status: ReceiptJobStatus,
        ocrText: String?,
        geminiRaw: String?,
        parsedData: String?,
        errorMessage: String?,
    )

    @Query("UPDATE receipt_batches SET status = :status WHERE id = :batchId")
    suspend fun updateBatchStatus(batchId: Long, status: ReceiptBatchStatus)

    @Query("UPDATE receipt_jobs SET status = :status WHERE batchId = :batchId AND status = :currentStatus")
    suspend fun updateJobsStatus(
        batchId: Long,
        currentStatus: ReceiptJobStatus,
        status: ReceiptJobStatus,
    )
}
