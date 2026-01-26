package com.sgagestudio.dicho.domain.repository

import com.sgagestudio.dicho.domain.model.Transaction
import java.io.File

interface CsvExporter {
    suspend fun export(transactions: List<Transaction>, outputDir: File): File
}
