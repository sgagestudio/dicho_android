package com.marcoassociation.dicho.domain.usecase

import android.content.ContentResolver
import android.net.Uri
import com.marcoassociation.dicho.domain.repository.TransactionRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class CsvExporterUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val contentResolver: ContentResolver
) {
    suspend operator fun invoke(destination: Uri): Result<Unit> {
        return runCatching {
            val transactions = transactionRepository.observeTransactions()
            val snapshot = transactions.first()
            val csv = buildCsv(snapshot)
            contentResolver.openOutputStream(destination)?.use { outputStream ->
                outputStream.write(csv.toByteArray())
            } ?: error("No output stream available")
        }
    }

    private fun buildCsv(transactions: List<com.marcoassociation.dicho.domain.model.Transaction>): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val header = "id,concept,amount,currency,expenseDate,recordDate,category,status,processingSource"
        val rows = transactions.joinToString("\n") { transaction ->
            listOf(
                transaction.id,
                transaction.concept,
                transaction.amount,
                transaction.currency,
                dateFormat.format(Date(transaction.expenseDate)),
                dateFormat.format(Date(transaction.recordDate)),
                transaction.category,
                transaction.status.name,
                transaction.processingSource.name
            ).joinToString(",")
        }
        return "$header\n$rows"
    }
}
