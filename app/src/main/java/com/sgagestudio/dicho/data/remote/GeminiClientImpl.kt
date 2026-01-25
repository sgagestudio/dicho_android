package com.sgagestudio.dicho.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.sgagestudio.dicho.BuildConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GeminiClientImpl @Inject constructor() : GeminiClient {
    private val json = Json { ignoreUnknownKeys = true }
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = SYSTEM_PROMPT,
    )

    override suspend fun extractTransaction(rawText: String): AiTransactionPayload {
        val response = model.generateContent(rawText)
        val payload = response.text ?: error("Empty response")
        return json.decodeFromString(AiTransactionPayload.serializer(), payload)
    }

    private companion object {
        const val SYSTEM_PROMPT =
            "Eres un asistente contable. Extrae: monto, moneda (ISO), concepto, categoría y fecha (ISO-8601) del texto. Si hay correcciones, usa el último valor válido. Si falta fecha, usa hoy. Salida JSON estricta."
    }
}
