/*
 * Модуль зависимостей Hilt для приложения
 * 
 * Этот файл содержит все зависимости, необходимые для работы приложения:
 * 
 * Основные провайдеры:
 * - BinApiService - Retrofit клиент для binlist.net API
 * - GeocodingService - Retrofit клиент для OpenStreetMap геокодинга
 * - BinDatabase - Room база данных для локального кэширования
 * - BinDao - Data Access Object для работы с базой данных
 * - BinRepository - репозиторий для работы с BIN данными
 * - ApiKeyRepository - репозиторий для хранения API ключей
 * 
 * Конфигурация:
 * - Два отдельных Retrofit клиента с разными базовыми URL
 * - Квалификаторы для различий клиентов (@BinApiRetrofit, @GeocodingRetrofit)
 * - Настройка таймаутов и логирования для HTTP клиентов
 * - Конвертеры для работы с JSON (Gson)
 * - Настройка Room базы данных с миграциями
 * 
 * Особенности:
 * - Использует Hilt для внедрения зависимостей
 * - Поддерживает как бесплатный, так и премиум API
 * - Разделение ответственности между разными API сервисами
 * - Автоматическое управление жизненным циклом зависимостей
 * - Конфигурируемые параметры (таймауты, URL, логирование)
 */

package com.binchecker.di

import android.content.Context
import com.binchecker.data.api.BinApiService
import com.binchecker.data.local.BinDatabase
import com.binchecker.data.repository.BinRepositoryImpl
import com.binchecker.domain.repository.BinRepository
import com.binchecker.data.api.GeocodingService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import javax.inject.Qualifier
import androidx.room.Room
import com.binchecker.data.local.BinDao
import com.binchecker.data.repository.ApiKeyRepositoryImpl
import com.binchecker.domain.repository.ApiKeyRepository

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BinApiRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeocodingRetrofit

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    @BinApiRetrofit
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://lookup.binlist.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("Accept-Version", "3")
                        .addHeader("User-Agent", "BIN-Checker-Android-App")
                        .build()
                    chain.proceed(request)
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build())
            .build()
    }

    @Provides
    @Singleton
    @GeocodingRetrofit
    fun provideGeocodingRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://nominatim.openstreetmap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val request = chain.request().newBuilder()
                        .addHeader("User-Agent", "BIN-Checker-Android-App")
                        .build()
                    chain.proceed(request)
                }
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build())
            .build()
    }

    @Provides
    @Singleton
    fun provideBinApiService(@BinApiRetrofit retrofit: Retrofit): BinApiService {
        return retrofit.create(BinApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGeocodingService(@GeocodingRetrofit geocodingRetrofit: Retrofit): GeocodingService {
        return geocodingRetrofit.create(GeocodingService::class.java)
    }

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): BinDatabase {
        return Room.databaseBuilder(
            context,
            BinDatabase::class.java,
            "bin_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideBinDao(database: BinDatabase): BinDao {
        return database.binDao()
    }

    @Provides
    @Singleton
    fun provideBinRepository(
        api: BinApiService,
        geocodingService: GeocodingService,
        apiKeyRepository: ApiKeyRepository,
        dao: BinDao
    ): BinRepository {
        return BinRepositoryImpl(api, geocodingService, apiKeyRepository, dao)
    }

    @Provides
    @Singleton
    fun provideApiKeyRepository(
        @ApplicationContext context: Context
    ): ApiKeyRepository {
        return ApiKeyRepositoryImpl(context)
    }
} 