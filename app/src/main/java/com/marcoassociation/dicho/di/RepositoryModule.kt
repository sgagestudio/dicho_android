package com.marcoassociation.dicho.di

import android.content.Context
import androidx.work.WorkManager
import com.marcoassociation.dicho.data.repository.AIProcessorRepositoryImpl
import com.marcoassociation.dicho.data.repository.LocalAICapabilityCheckerImpl
import com.marcoassociation.dicho.data.repository.NetworkMonitorImpl
import com.marcoassociation.dicho.data.repository.SettingsRepositoryImpl
import com.marcoassociation.dicho.data.repository.TransactionRepositoryImpl
import com.marcoassociation.dicho.domain.repository.AIProcessorRepository
import com.marcoassociation.dicho.domain.repository.LocalAICapabilityChecker
import com.marcoassociation.dicho.domain.repository.NetworkMonitor
import com.marcoassociation.dicho.domain.repository.SettingsRepository
import com.marcoassociation.dicho.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl
    ): TransactionRepository

    @Binds
    @Singleton
    abstract fun bindAiProcessorRepository(
        impl: AIProcessorRepositoryImpl
    ): AIProcessorRepository

    @Binds
    @Singleton
    abstract fun bindLocalAiCapabilityChecker(
        impl: LocalAICapabilityCheckerImpl
    ): LocalAICapabilityChecker

    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        impl: NetworkMonitorImpl
    ): NetworkMonitor

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object WorkManagerModule {
    @Provides
    @Singleton
    fun provideWorkManager(@ApplicationContext context: Context): WorkManager =
        WorkManager.getInstance(context)
}
