package com.sgagestudio.dicho.data.local.db

import androidx.room.TypeConverter
import com.sgagestudio.dicho.domain.model.ProcessingSource
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
}
