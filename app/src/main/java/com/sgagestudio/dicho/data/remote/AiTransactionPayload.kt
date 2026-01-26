package com.sgagestudio.dicho.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiTransactionPayload(
    @SerialName("concept") val concept: String,
    @SerialName("amount") val amount: Double,
    @SerialName("currency") val currency: String,
    @SerialName("category") val category: String,
    @SerialName("expense_date") val expenseDate: String,
)
