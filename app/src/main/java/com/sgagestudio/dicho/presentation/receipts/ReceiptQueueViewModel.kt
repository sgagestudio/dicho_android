package com.sgagestudio.dicho.presentation.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgagestudio.dicho.domain.model.ReceiptImageJob
import com.sgagestudio.dicho.domain.repository.ReceiptQueueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptQueueViewModel @Inject constructor(
    private val receiptQueueRepository: ReceiptQueueRepository,
) : ViewModel() {
    private val _batchId = MutableStateFlow<Long?>(null)
    val batchId: StateFlow<Long?> = _batchId

    val jobs: StateFlow<List<ReceiptImageJob>> = _batchId
        .flatMapLatest { batchId ->
            if (batchId == null) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                receiptQueueRepository.observeJobs(batchId)
            }
        }
        .stateIn(viewModelScope, kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5_000), emptyList())

    init {
        viewModelScope.launch {
            _batchId.value = receiptQueueRepository.getOrCreateActiveBatch().id
        }
    }

    suspend fun getJob(jobId: Long): ReceiptImageJob? = receiptQueueRepository.getJob(jobId)
}
