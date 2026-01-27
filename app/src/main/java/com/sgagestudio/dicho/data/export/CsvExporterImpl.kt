package com.sgagestudio.dicho.data.export

import com.sgagestudio.dicho.domain.model.Transaction
import com.sgagestudio.dicho.domain.repository.CsvExporter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class CsvExporterImpl @Inject constructor() : CsvExporter {

    // Si tu interfaz CsvExporter no permite params, elige un default aquí:
    private val encodingMode: CsvEncodingMode = CsvEncodingMode.UTF16LE_BOM

    override suspend fun export(transactions: List<Transaction>): ByteArray {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault())
        val builder = StringBuilder()

        // CSV: usa \r\n por compatibilidad (Excel/hojas de cálculo)
        builder.append("Concepto,Cantidad,Moneda,Fecha de Gasto,Fecha de Registro\r\n")

        transactions.forEach { transaction ->
            val expenseDate = formatter.format(Instant.ofEpochMilli(transaction.expenseDate))
            val recordDate = formatter.format(Instant.ofEpochMilli(transaction.recordDate))

            builder.append(
                listOf(
                    escapeCsv(transaction.concept),
                    transaction.amount.toString(),
                    escapeCsv(transaction.currency),
                    expenseDate,
                    recordDate,
                ).joinToString(separator = ",")
            )
            builder.append("\r\n")
        }

        val csvString = builder.toString()

        return when (encodingMode) {
            CsvEncodingMode.UTF8_BOM ->
                BOM_UTF8 + csvString.toByteArray(Charsets.UTF_8)

            CsvEncodingMode.UTF16LE_BOM ->
                BOM_UTF16LE + csvString.toByteArray(Charsets.UTF_16LE)
        }
    }

    /**
     * Escapa campos con comas, comillas o saltos de línea:
     * - envuelve en comillas
     * - duplica comillas internas
     */
    private fun escapeCsv(value: String): String {
        val needsQuoting = value.any { it == ',' || it == '"' || it == '\n' || it == '\r' }
        if (!needsQuoting) return value
        return "\"" + value.replace("\"", "\"\"") + "\""
    }

    private enum class CsvEncodingMode {
        UTF8_BOM,
        UTF16LE_BOM
    }

    private companion object {
        val BOM_UTF8 = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val BOM_UTF16LE = byteArrayOf(0xFF.toByte(), 0xFE.toByte())
    }
}
