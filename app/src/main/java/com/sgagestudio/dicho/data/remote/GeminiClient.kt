package com.sgagestudio.dicho.data.remote

interface GeminiClient {
    suspend fun extractTransaction(rawText: String): AiTransactionPayload
}
