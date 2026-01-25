package com.marcoassociation.dicho.domain.repository

import com.marcoassociation.dicho.domain.model.LocalAiStatus
import kotlinx.coroutines.flow.StateFlow

interface AIProcessorRepository {
    val localAiStatus: StateFlow<LocalAiStatus>

    suspend fun refreshCapabilities()

    suspend fun processAndStore(rawText: String)

    suspend fun processPendingTransactions()
}
