package com.sgagestudio.dicho.presentation.receipts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sgagestudio.dicho.data.worker.ReceiptQueueWorkScheduler
import com.sgagestudio.dicho.domain.repository.ReceiptQueueRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptCaptureViewModel @Inject constructor(
    private val receiptQueueRepository: ReceiptQueueRepository,
    private val workScheduler: ReceiptQueueWorkScheduler,
) : ViewModel() {
    private val _batchId = MutableStateFlow<Long?>(null)
    val batchId: StateFlow<Long?> = _batchId

    init {
        viewModelScope.launch {
            _batchId.value = receiptQueueRepository.getOrCreateActiveBatch().id
        }
    }

    fun confirmImage(imageUri: String) {
        viewModelScope.launch {
            val batchId = receiptQueueRepository.getOrCreateActiveBatch().id
            _batchId.value = batchId
            receiptQueueRepository.insertJob(batchId, imageUri)
            workScheduler.enqueueProcessing()
        }
    }
}
