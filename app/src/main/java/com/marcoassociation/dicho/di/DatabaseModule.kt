package com.marcoassociation.dicho.di

import android.content.Context
import androidx.room.Room
import com.marcoassociation.dicho.data.local.DichoDatabase
import com.marcoassociation.dicho.data.local.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DichoDatabase {
        return Room.databaseBuilder(context, DichoDatabase::class.java, "dicho.db")
            .build()
    }

    @Provides
    fun provideTransactionDao(database: DichoDatabase): TransactionDao = database.transactionDao()
}
