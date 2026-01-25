package com.sgagestudio.dicho.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TransactionEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(TransactionConverters::class)
abstract class DichoDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}
