package com.sgagestudio.dicho.data.local

import com.sgagestudio.dicho.data.remote.AiTransactionPayload

interface LocalAiProcessor {
    suspend fun extractTransaction(rawText: String): AiTransactionPayload
}
