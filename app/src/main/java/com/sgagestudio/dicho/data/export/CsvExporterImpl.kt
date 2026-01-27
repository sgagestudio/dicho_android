package com.sgagestudio.dicho.data.export

import com.sgagestudio.dicho.domain.model.Transaction
import com.sgagestudio.dicho.domain.repository.CsvExporter
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CsvExporterImpl @Inject constructor() : CsvExporter {
    override suspend fun export(transactions: List<Transaction>, outputDir: File): File {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault())
        val file = File(outputDir, "export_dicho_fecha_hora_${System.currentTimeMillis()}.csv")
        file.bufferedWriter().use { writer ->
            writer.appendLine("Concepto,Cantidad,Moneda,Fecha de Gasto,Fecha de Registro")
            transactions.forEach { transaction ->
                val expenseDate = formatter.format(Instant.ofEpochMilli(transaction.expenseDate))
                val recordDate = formatter.format(Instant.ofEpochMilli(transaction.recordDate))
                writer.appendLine(
                    listOf(
                        transaction.concept,
                        transaction.amount.toString(),
                        transaction.currency,
                        expenseDate,
                        recordDate,
                    ).joinToString(separator = ",")
                )
            }
        }
        return file
    }
}
