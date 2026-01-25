package com.marcoassociation.dicho.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcoassociation.dicho.domain.model.LocalAiStatus
import com.marcoassociation.dicho.domain.model.Transaction
import com.marcoassociation.dicho.domain.model.TransactionStatus
import com.marcoassociation.dicho.domain.repository.AIProcessorRepository
import com.marcoassociation.dicho.domain.usecase.GetMonthlyTotalUseCase
import com.marcoassociation.dicho.domain.usecase.ObserveTransactionsUseCase
import com.marcoassociation.dicho.domain.usecase.ProcessRawTextUseCase
import com.marcoassociation.dicho.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeTransactionsUseCase: ObserveTransactionsUseCase,
    private val getMonthlyTotalUseCase: GetMonthlyTotalUseCase,
    private val processRawTextUseCase: ProcessRawTextUseCase,
    private val aiProcessorRepository: AIProcessorRepository
) : ViewModel() {
    private val monthRange = DateUtils.monthRangeInMillis()

    val uiState: StateFlow<HomeUiState> = combine(
        observeTransactionsUseCase(),
        getMonthlyTotalUseCase(monthRange.first, monthRange.second, TransactionStatus.COMPLETED),
        aiProcessorRepository.localAiStatus
    ) { transactions, total, localAiStatus ->
        HomeUiState(
            transactions = transactions,
            monthlyTotal = total ?: 0.0,
            localAiStatus = localAiStatus
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun refreshCapabilities() {
        viewModelScope.launch {
            aiProcessorRepository.refreshCapabilities()
        }
    }

    fun processSpeech(rawText: String) {
        viewModelScope.launch {
            processRawTextUseCase(rawText)
        }
    }
}

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val monthlyTotal: Double = 0.0,
    val localAiStatus: LocalAiStatus = LocalAiStatus(
        supportsLocalAi = false,
        isModelAvailable = false,
        canDownloadModel = false,
        hasInternet = false
    )
)
