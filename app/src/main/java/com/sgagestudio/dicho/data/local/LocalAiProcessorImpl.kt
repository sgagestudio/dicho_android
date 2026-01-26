package com.sgagestudio.dicho.data.local

import com.sgagestudio.dicho.data.remote.AiTransactionPayload
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class LocalAiProcessorImpl @Inject constructor() : LocalAiProcessor {
    override suspend fun extractTransaction(rawText: String): AiTransactionPayload {
        val amountRegex = "(-?\\d+(?:[.,]\\d+)?)".toRegex()
        val amountMatch = amountRegex.findAll(rawText).lastOrNull()?.value
        val amount = amountMatch?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        val currency = when {
            rawText.contains("usd", ignoreCase = true) -> "USD"
            rawText.contains("mxn", ignoreCase = true) -> "MXN"
            rawText.contains("cop", ignoreCase = true) -> "COP"
            rawText.contains("eur", ignoreCase = true) || rawText.contains("€") -> "EUR"
            else -> "EUR"
        }
        val category = when {
            rawText.contains("comida", ignoreCase = true) -> "Comida"
            rawText.contains("transporte", ignoreCase = true) -> "Transporte"
            rawText.contains("hogar", ignoreCase = true) -> "Hogar"
            else -> "Otros"
        }
        val today = LocalDate.now(ZoneId.systemDefault()).toString()
        return AiTransactionPayload(
            concept = rawText.take(48),
            amount = amount,
            currency = currency,
            category = category,
            expenseDate = today,
        )
    }
}
