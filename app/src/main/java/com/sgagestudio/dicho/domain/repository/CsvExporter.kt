package com.sgagestudio.dicho.domain.repository

import com.sgagestudio.dicho.domain.model.Transaction
interface CsvExporter {
    suspend fun export(transactions: List<Transaction>): ByteArray
}
