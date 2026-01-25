package com.sgagestudio.dicho.domain.repository

import com.sgagestudio.dicho.domain.model.ProcessingSource
import com.sgagestudio.dicho.domain.model.Transaction
import kotlinx.coroutines.flow.StateFlow

interface AIProcessorRepository {
    val localModelAvailable: StateFlow<Boolean>
    val localModelSupported: StateFlow<Boolean>
    suspend fun refreshCapabilities()
    suspend fun process(rawText: String): Result<Pair<Transaction, ProcessingSource>>
}
