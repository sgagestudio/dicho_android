package com.sgagestudio.dicho.data.local.db

import androidx.room.TypeConverter
import com.sgagestudio.dicho.domain.model.ProcessingSource
import com.sgagestudio.dicho.domain.model.ReceiptBatchStatus
import com.sgagestudio.dicho.domain.model.ReceiptJobStatus
import com.sgagestudio.dicho.domain.model.TransactionStatus

class TransactionConverters {
    @TypeConverter
    fun fromStatus(value: TransactionStatus): String = value.name

    @TypeConverter
    fun toStatus(value: String): TransactionStatus = TransactionStatus.valueOf(value)

    @TypeConverter
    fun fromProcessingSource(value: ProcessingSource): String = value.name

    @TypeConverter
    fun toProcessingSource(value: String): ProcessingSource = ProcessingSource.valueOf(value)

    @TypeConverter
    fun fromReceiptBatchStatus(value: ReceiptBatchStatus): String = value.name

    @TypeConverter
    fun toReceiptBatchStatus(value: String): ReceiptBatchStatus = ReceiptBatchStatus.valueOf(value)

    @TypeConverter
    fun fromReceiptJobStatus(value: ReceiptJobStatus): String = value.name

    @TypeConverter
    fun toReceiptJobStatus(value: String): ReceiptJobStatus = ReceiptJobStatus.valueOf(value)
}
