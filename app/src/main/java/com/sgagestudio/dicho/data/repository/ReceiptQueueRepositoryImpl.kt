package com.sgagestudio.dicho.data.repository

import com.sgagestudio.dicho.data.local.db.ReceiptBatchEntity
import com.sgagestudio.dicho.data.local.db.ReceiptImageJobEntity
import com.sgagestudio.dicho.data.local.db.ReceiptQueueDao
import com.sgagestudio.dicho.domain.model.ReceiptBatch
import com.sgagestudio.dicho.domain.model.ReceiptBatchStatus
import com.sgagestudio.dicho.domain.model.ReceiptImageJob
import com.sgagestudio.dicho.domain.model.ReceiptJobStatus
import com.sgagestudio.dicho.domain.repository.ReceiptQueueRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject

class ReceiptQueueRepositoryImpl @Inject constructor(
    private val receiptQueueDao: ReceiptQueueDao,
) : ReceiptQueueRepository {
    override suspend fun getOrCreateActiveBatch(): ReceiptBatch {
        val active = receiptQueueDao.getBatchByStatus(ReceiptBatchStatus.ACTIVE)
        if (active != null) return active.toDomain()
        val now = Instant.now().toEpochMilli()
        val id = receiptQueueDao.insertBatch(
            ReceiptBatchEntity(
                createdAt = now,
                status = ReceiptBatchStatus.ACTIVE,
            ),
        )
        return ReceiptBatch(id = id, createdAt = now, status = ReceiptBatchStatus.ACTIVE)
    }

    override suspend fun getBatch(batchId: Long): ReceiptBatch? {
        return receiptQueueDao.getBatchById(batchId)?.toDomain()
    }

    override fun observeJobs(batchId: Long): Flow<List<ReceiptImageJob>> {
        return receiptQueueDao.observeJobs(batchId).map { list -> list.map { it.toDomain() } }
    }

    override fun observeReadyJobs(batchId: Long): Flow<List<ReceiptImageJob>> {
        return receiptQueueDao.observeJobsByStatus(batchId, ReceiptJobStatus.GEMINI_DONE)
            .map { list -> list.map { it.toDomain() } }
    }

    override suspend fun insertJob(batchId: Long, imageUri: String): Long {
        val now = Instant.now().toEpochMilli()
        return receiptQueueDao.insertJob(
            ReceiptImageJobEntity(
                batchId = batchId,
                imageUri = imageUri,
                status = ReceiptJobStatus.QUEUED,
                ocrText = null,
                geminiRaw = null,
                parsedData = null,
                errorMessage = null,
                createdAt = now,
            ),
        )
    }

    override suspend fun updateJob(job: ReceiptImageJob) {
        receiptQueueDao.updateJob(job.toEntity())
    }

    override suspend fun updateJobStatus(
        jobId: Long,
        status: ReceiptJobStatus,
        ocrText: String?,
        geminiRaw: String?,
        parsedData: String?,
        errorMessage: String?,
    ) {
        receiptQueueDao.updateJobStatus(jobId, status, ocrText, geminiRaw, parsedData, errorMessage)
    }

    override suspend fun getNextQueuedJob(): ReceiptImageJob? {
        return receiptQueueDao.getNextJobByStatus(ReceiptJobStatus.QUEUED)?.toDomain()
    }

    override suspend fun getJob(jobId: Long): ReceiptImageJob? {
        return receiptQueueDao.getJobById(jobId)?.toDomain()
    }

    override suspend fun markBatchConfirmed(batchId: Long) {
        receiptQueueDao.updateBatchStatus(batchId, ReceiptBatchStatus.CONFIRMED)
    }

    override suspend fun markJobsConfirmed(batchId: Long) {
        receiptQueueDao.updateJobsStatus(
            batchId = batchId,
            currentStatus = ReceiptJobStatus.GEMINI_DONE,
            status = ReceiptJobStatus.CONFIRMED,
        )
    }
}
