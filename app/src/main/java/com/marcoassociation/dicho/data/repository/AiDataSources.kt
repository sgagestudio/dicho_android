package com.marcoassociation.dicho.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.marcoassociation.dicho.domain.model.AiParsedTransaction
import com.marcoassociation.dicho.util.SYSTEM_PROMPT
import kotlinx.serialization.json.Json
import javax.inject.Inject

interface AiDataSource {
    suspend fun parseTransaction(rawText: String): Result<AiParsedTransaction>
}

class LocalAiDataSource @Inject constructor() : AiDataSource {
    override suspend fun parseTransaction(rawText: String): Result<AiParsedTransaction> {
        // TODO: Integrate with on-device AICore/Gemini Nano model.
        return Result.success(
            AiParsedTransaction(
                concept = rawText,
                amount = 0.0,
                currency = "EUR",
                category = "Otros",
                date = java.time.LocalDate.now().toString()
            )
        )
    }
}

class CloudAiDataSource @Inject constructor(
    private val json: Json,
    private val generativeModel: GenerativeModel
) : AiDataSource {
    override suspend fun parseTransaction(rawText: String): Result<AiParsedTransaction> {
        return runCatching {
            val response = generativeModel.generateContent(
                content {
                    text(SYSTEM_PROMPT)
                    text(rawText)
                }
            )
            val textResponse = response.text ?: error("Empty response from Gemini")
            val jsonPayload = extractJson(textResponse)
            json.decodeFromString(AiParsedTransaction.serializer(), jsonPayload)
        }
    }

    private fun extractJson(text: String): String {
        val start = text.indexOf('{')
        val end = text.lastIndexOf('}')
        if (start == -1 || end == -1 || end <= start) {
            error("No JSON found in response")
        }
        return text.substring(start, end + 1)
    }
}
