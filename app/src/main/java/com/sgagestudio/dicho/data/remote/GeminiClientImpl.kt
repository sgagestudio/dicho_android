package com.sgagestudio.dicho.data.remote

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.sgagestudio.dicho.BuildConfig
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import java.time.LocalDate
import javax.inject.Inject

class GeminiClientImpl @Inject constructor() : GeminiClient {
    private val json = Json { ignoreUnknownKeys = true }

    private val model = GenerativeModel(
        modelName = "gemini-flash-latest",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content { text(SYSTEM_PROMPT) }
    )

    override suspend fun extractTransaction(rawText: String): List<AiTransactionPayload> {
        // Obtenemos la fecha real de hoy para que el cálculo mensual del ViewModel funcione
        val today = LocalDate.now().toString()

        val userPrompt = """
            Contexto: Hoy es $today.
            Entrada de usuario: "$rawText"
        """.trimIndent()

        val response = model.generateContent(
            content {
                text(userPrompt)
            }
        )

        val rawPayload = response.text ?: error("Empty response")

        // Limpieza de Markdown para evitar errores de parseo JSON
        val cleanJson = rawPayload
            .replace("```json", "")
            .replace("```", "")
            .trim()

        return json.decodeFromString(
            ListSerializer(AiTransactionPayload.serializer()),
            cleanJson
        )
    }

    private companion object {
        const val SYSTEM_PROMPT = """
        Eres un asistente contable experto en extracción de datos.
        Tu tarea es convertir texto en un objeto JSON estricto.
        
        REGLAS DE FECHA:
        1. Usa la fecha proporcionada en el "Contexto" como fecha de hoy.
        2. Si el usuario no menciona una fecha específica (ej: "ayer", "el lunes"), usa SIEMPRE la fecha de hoy.
        
        FORMATO JSON REQUERIDO:
        [
          {
            "concept": "Descripción breve",
            "amount": 0.0,
            "currency": "EUR",
            "category": "Categoría adecuada",
            "expense_date": "YYYY-MM-DD"
          }
        ]

        IMPORTANTE: Responde ÚNICAMENTE con el JSON en forma de lista. No añadas texto extra ni formato Markdown.
    """
    }
}
