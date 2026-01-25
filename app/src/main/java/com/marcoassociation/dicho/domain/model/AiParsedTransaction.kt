package com.marcoassociation.dicho.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AiParsedTransaction(
    @SerialName("concepto")
    val concept: String,
    @SerialName("monto")
    val amount: Double,
    @SerialName("moneda")
    val currency: String,
    @SerialName("categoria")
    val category: String,
    @SerialName("fecha")
    val date: String
)
