package com.marcoassociation.dicho.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.marcoassociation.dicho.domain.usecase.AddManualTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ManualEntryViewModel @Inject constructor(
    private val addManualTransactionUseCase: AddManualTransactionUseCase
) : ViewModel() {
    fun saveManualEntry(
        concept: String,
        amount: Double,
        currency: String,
        date: Long,
        category: String
    ) {
        viewModelScope.launch {
            addManualTransactionUseCase(concept, amount, currency, date, category)
        }
    }
}
