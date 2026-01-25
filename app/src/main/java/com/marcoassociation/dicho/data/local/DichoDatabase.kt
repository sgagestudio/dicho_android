package com.marcoassociation.dicho.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(TransactionConverters::class)
abstract class DichoDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}
