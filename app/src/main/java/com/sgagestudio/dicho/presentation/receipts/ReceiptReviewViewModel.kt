package com.sgagestudio.dicho.presentation.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgagestudio.dicho.data.remote.AiTransactionPayload
import com.sgagestudio.dicho.domain.model.ProcessingSource
import com.sgagestudio.dicho.domain.model.ReceiptImageJob
import com.sgagestudio.dicho.domain.model.Transaction
import com.sgagestudio.dicho.domain.model.TransactionStatus
import com.sgagestudio.dicho.domain.repository.ReceiptQueueRepository
import com.sgagestudio.dicho.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ReceiptDraft(
    val concept: String,
    val amount: String,
    val currency: String,
    val category: String,
    val expenseDate: String,
)

@HiltViewModel
class ReceiptReviewViewModel @Inject constructor(
    private val receiptQueueRepository: ReceiptQueueRepository,
    private val transactionRepository: TransactionRepository,
    private val json: Json,
) : ViewModel() {
    private val _batchId = MutableStateFlow<Long?>(null)
    val batchId: StateFlow<Long?> = _batchId

    private val _drafts = MutableStateFlow<Map<Long, ReceiptDraft>>(emptyMap())
    val drafts: StateFlow<Map<Long, ReceiptDraft>> = _drafts

    val readyJobs: StateFlow<List<ReceiptImageJob>> = _batchId
        .flatMapLatest { batchId ->
            if (batchId == null) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                receiptQueueRepository.observeReadyJobs(batchId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            _batchId.value = receiptQueueRepository.getOrCreateActiveBatch().id
        }
        viewModelScope.launch {
            readyJobs.collect { jobs -> ensureDrafts(jobs) }
        }
    }

    fun updateDraft(jobId: Long, draft: ReceiptDraft) {
        _drafts.value = _drafts.value.toMutableMap().apply { put(jobId, draft) }
    }

    suspend fun persistDraft(jobId: Long) {
        val draft = _drafts.value[jobId] ?: return
        val payload = AiTransactionPayload(
            concept = draft.concept,
            amount = draft.amount.replace(",", ".").toDoubleOrNull() ?: 0.0,
            currency = draft.currency.ifBlank { "EUR" },
            category = draft.category.ifBlank { "Otros" },
            expenseDate = draft.expenseDate,
        )
        val serialized = json.encodeToString(AiTransactionPayload.serializer(), payload)
        receiptQueueRepository.updateJobStatus(
            jobId = jobId,
            status = com.sgagestudio.dicho.domain.model.ReceiptJobStatus.GEMINI_DONE,
            parsedData = serialized,
            geminiRaw = serialized,
        )
    }

    suspend fun confirmAll(): Int {
        val batchId = _batchId.value ?: return 0
        val jobs = readyJobs.value
        val now = Instant.now().toEpochMilli()
        var inserted = 0
        for (job in jobs) {
            val draft = _drafts.value[job.id] ?: draftFromJob(job)
            val transaction = draft.toTransaction(job, now)
            transactionRepository.insertTransaction(transaction)
            inserted += 1
        }
        receiptQueueRepository.markJobsConfirmed(batchId)
        receiptQueueRepository.markBatchConfirmed(batchId)
        return inserted
    }

    fun findJob(jobId: Long): ReceiptImageJob? = readyJobs.value.firstOrNull { it.id == jobId }

    private fun ensureDrafts(jobs: List<ReceiptImageJob>) {
        val current = _drafts.value.toMutableMap()
        var changed = false
        jobs.forEach { job ->
            if (!current.containsKey(job.id)) {
                current[job.id] = draftFromJob(job)
                changed = true
            }
        }
        if (changed) {
            _drafts.value = current
        }
    }

    private fun draftFromJob(job: ReceiptImageJob): ReceiptDraft {
        val payload = job.parsedData?.let {
            runCatching { json.decodeFromString(AiTransactionPayload.serializer(), it) }.getOrNull()
        }
        return ReceiptDraft(
            concept = payload?.concept.orEmpty(),
            amount = payload?.amount?.toString() ?: "",
            currency = payload?.currency ?: "EUR",
            category = payload?.category ?: "Otros",
            expenseDate = payload?.expenseDate ?: LocalDate.now().toString(),
        )
    }

    private fun ReceiptDraft.toTransaction(job: ReceiptImageJob, recordDate: Long): Transaction {
        val expenseDateMillis = runCatching {
            LocalDate.parse(expenseDate, DateTimeFormatter.ISO_DATE)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrElse { recordDate }
        return Transaction(
            id = 0L,
            rawText = job.ocrText ?: "",
            concept = concept,
            amount = amount.replace(",", ".").toDoubleOrNull() ?: 0.0,
            currency = currency.ifBlank { "EUR" },
            expenseDate = expenseDateMillis,
            recordDate = recordDate,
            category = category.ifBlank { "Otros" },
            status = TransactionStatus.COMPLETED,
            processingSource = ProcessingSource.CLOUD_AI,
        )
    }
}
