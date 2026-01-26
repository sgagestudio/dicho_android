package com.sgagestudio.dicho.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content // Importación clave
import com.sgagestudio.dicho.BuildConfig
import kotlinx.serialization.json.Json
import javax.inject.Inject

class GeminiClientImpl @Inject constructor() : GeminiClient {
    private val json = Json { ignoreUnknownKeys = true }

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        // CORRECCIÓN: Envolvemos el String en un objeto Content
        systemInstruction = content { text(SYSTEM_PROMPT) }
    )

    override suspend fun extractTransaction(rawText: String): AiTransactionPayload {
        // CORRECCIÓN: Envolvemos el prompt del usuario también por seguridad y claridad
        val response = model.generateContent(
            content {
                text(rawText)
            }
        )

        val payload = response.text ?: error("Empty response")
        return json.decodeFromString(AiTransactionPayload.serializer(), payload)
    }

    private companion object {
        const val SYSTEM_PROMPT = """
        Eres un asistente contable. Extrae la información del texto y devuélvela en formato JSON estricto con estas llaves:
        - "concept": descripción breve.
        - "amount": número (monto).
        - "currency": código ISO (ej: EUR, USD).
        - "category": categoría del gasto.
        - "expense_date": fecha en formato ISO-8601.
        
        Si el usuario corrige un dato, usa el último mencionado. Si falta la fecha, usa la de hoy.
    """
    }
}