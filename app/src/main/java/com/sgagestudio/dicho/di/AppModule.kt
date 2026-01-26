package com.sgagestudio.dicho.di

import android.content.Context
import androidx.room.Room
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.sgagestudio.dicho.data.export.CsvExporterImpl
import com.sgagestudio.dicho.data.local.LocalAiProcessor
import com.sgagestudio.dicho.data.local.LocalAiProcessorImpl
import com.sgagestudio.dicho.data.local.LocalAICapabilityCheckerImpl
import com.sgagestudio.dicho.data.local.NetworkMonitor
import com.sgagestudio.dicho.data.local.NetworkMonitorImpl
import com.sgagestudio.dicho.data.local.db.DichoDatabase
import com.sgagestudio.dicho.data.local.db.TransactionDao
import com.sgagestudio.dicho.data.remote.GeminiClient
import com.sgagestudio.dicho.data.remote.GeminiClientImpl
import com.sgagestudio.dicho.data.repository.AIProcessorRepositoryImpl
import com.sgagestudio.dicho.data.repository.TransactionRepositoryImpl
import com.sgagestudio.dicho.domain.ai.LocalAICapabilityChecker
import com.sgagestudio.dicho.domain.repository.AIProcessorRepository
import com.sgagestudio.dicho.domain.repository.CsvExporter
import com.sgagestudio.dicho.domain.repository.TransactionRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): DichoDatabase {
        return Room.databaseBuilder(context, DichoDatabase::class.java, "dicho.db")
            .build()
    }

    @Provides
    fun provideTransactionDao(database: DichoDatabase): TransactionDao = database.transactionDao()

    @Provides
    @Singleton
    fun provideRetrofit(json: Json): Retrofit {
        val logger = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl("https://example.com/")
            .client(client)
            // La clave es invocar asConverterFactory sobre el objeto json:
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds
    abstract fun bindLocalAICapabilityChecker(
        impl: LocalAICapabilityCheckerImpl,
    ): LocalAICapabilityChecker

    @Binds
    abstract fun bindLocalAiProcessor(impl: LocalAiProcessorImpl): LocalAiProcessor

    @Binds
    abstract fun bindNetworkMonitor(impl: NetworkMonitorImpl): NetworkMonitor

    @Binds
    abstract fun bindGeminiClient(impl: GeminiClientImpl): GeminiClient

    @Binds
    abstract fun bindTransactionRepository(
        impl: TransactionRepositoryImpl,
    ): TransactionRepository

    @Binds
    abstract fun bindAiProcessorRepository(
        impl: AIProcessorRepositoryImpl,
    ): AIProcessorRepository

    @Binds
    abstract fun bindCsvExporter(impl: CsvExporterImpl): CsvExporter
}
