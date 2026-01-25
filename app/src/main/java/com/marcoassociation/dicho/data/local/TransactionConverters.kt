package com.marcoassociation.dicho.data.local

import androidx.room.TypeConverter

class TransactionConverters {
    @TypeConverter
    fun fromStatus(status: TransactionStatusEntity): String = status.name

    @TypeConverter
    fun toStatus(value: String): TransactionStatusEntity = TransactionStatusEntity.valueOf(value)

    @TypeConverter
    fun fromProcessingSource(source: ProcessingSourceEntity): String = source.name

    @TypeConverter
    fun toProcessingSource(value: String): ProcessingSourceEntity = ProcessingSourceEntity.valueOf(value)
}
