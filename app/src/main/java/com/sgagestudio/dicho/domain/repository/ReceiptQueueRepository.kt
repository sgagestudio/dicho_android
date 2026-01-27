package com.sgagestudio.dicho.domain.repository

import com.sgagestudio.dicho.domain.model.ReceiptBatch
import com.sgagestudio.dicho.domain.model.ReceiptImageJob
import com.sgagestudio.dicho.domain.model.ReceiptJobStatus
import kotlinx.coroutines.flow.Flow

interface ReceiptQueueRepository {
    suspend fun getOrCreateActiveBatch(): ReceiptBatch
    suspend fun getBatch(batchId: Long): ReceiptBatch?
    fun observeJobs(batchId: Long): Flow<List<ReceiptImageJob>>
    fun observeReadyJobs(batchId: Long): Flow<List<ReceiptImageJob>>
    suspend fun insertJob(batchId: Long, imageUri: String): Long
    suspend fun updateJob(job: ReceiptImageJob)
    suspend fun updateJobStatus(
        jobId: Long,
        status: ReceiptJobStatus,
        ocrText: String? = null,
        geminiRaw: String? = null,
        parsedData: String? = null,
        errorMessage: String? = null,
    )
    suspend fun getNextQueuedJob(): ReceiptImageJob?
    suspend fun getJob(jobId: Long): ReceiptImageJob?
    suspend fun markBatchConfirmed(batchId: Long)
    suspend fun markJobsConfirmed(batchId: Long)
}
