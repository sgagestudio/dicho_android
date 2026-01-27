package com.sgagestudio.dicho.data.export

import com.sgagestudio.dicho.domain.model.Transaction
import com.sgagestudio.dicho.domain.repository.CsvExporter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CsvExporterImpl @Inject constructor() : CsvExporter {
    override suspend fun export(transactions: List<Transaction>): ByteArray {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault())
        val builder = StringBuilder()
        builder.appendLine("Concepto,Cantidad,Moneda,Fecha de Gasto,Fecha de Registro")
        transactions.forEach { transaction ->
            val expenseDate = formatter.format(Instant.ofEpochMilli(transaction.expenseDate))
            val recordDate = formatter.format(Instant.ofEpochMilli(transaction.recordDate))
            builder.appendLine(
                listOf(
                    transaction.concept,
                    transaction.amount.toString(),
                    transaction.currency,
                    expenseDate,
                    recordDate,
                ).joinToString(separator = ",")
            )
        }
        return builder.toString().toByteArray(Charsets.UTF_8)
    }
}
