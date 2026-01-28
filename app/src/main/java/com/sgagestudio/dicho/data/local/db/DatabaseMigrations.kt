package com.sgagestudio.dicho.data.local.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS receipt_batches (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    createdAt INTEGER NOT NULL,
                    status TEXT NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS receipt_jobs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    batchId INTEGER,
                    imageUri TEXT NOT NULL,
                    status TEXT NOT NULL,
                    ocrText TEXT,
                    geminiRaw TEXT,
                    parsedData TEXT,
                    errorMessage TEXT,
                    createdAt INTEGER NOT NULL
                )
                """.trimIndent()
            )
        }
    }
}
