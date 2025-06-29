/*
 * Реализация репозитория для работы с BIN данными
 * 
 * Этот класс является центральным компонентом для работы с данными и отвечает за:
 * - Получение информации о BIN номерах из API binlist.net
 * - Кэширование данных в локальной базе данных
 * - Геокодинг для получения дополнительной информации о банках
 * - Обработку ошибок и fallback на кэшированные данные
 * - Управление премиум и бесплатным режимами API
 * 
 * Основная логика:
 * 1. Сначала проверяется локальный кэш
 * 2. Если данных нет, выполняется запрос к API
 * 3. Полученные данные дополняются через геокодинг
 * 4. Результат сохраняется в базу данных
 * 5. При ошибках API используется кэшированная версия
 * 
 * Особенности реализации:
 * - Поддерживает как бесплатный (5 запросов/час), так и премиум API
 * - Автоматически добавляет API ключ в заголовки для премиум доступа
 * - Геокодинг выполняется для получения координат банка
 * - Graceful degradation при недоступности API
 * - Логирование всех операций для отладки
 * - Обработка различных типов ошибок (rate limit, network, server)
 * 
 * Зависимости:
 * - BinApiService - для запросов к binlist.net
 * - GeocodingService - для геокодинга
 * - BinDao - для работы с локальной базой данных
 * - ApiKeyRepository - для получения API ключа
 */

package com.binchecker.data.repository

import com.binchecker.data.api.BinApiService
import com.binchecker.data.api.GeocodingService
import com.binchecker.data.local.BinDao
import com.binchecker.domain.model.BinInfo
import com.binchecker.domain.repository.BinRepository
import com.binchecker.domain.repository.ApiKeyRepository
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class BinRepositoryImpl @Inject constructor(
    private val api: BinApiService,
    private val geocodingService: GeocodingService,
    private val apiKeyRepository: ApiKeyRepository,
    private val dao: BinDao
) : BinRepository {

    override suspend fun getBinInfo(bin: String): Result<BinInfo> {
        return try {
            
            val cachedBin = dao.getBinByNumber(bin)
            if (cachedBin != null) {
                println("DEBUG: Using cached BIN data for $bin")
                return Result.success(cachedBin)
            }
            
            
            val apiKey = apiKeyRepository.getApiKey()
            val authHeader = if (apiKey.isNotEmpty()) "Bearer $apiKey" else null
            
            
            println("DEBUG: Making API request for BIN: $bin with API key: ${if (apiKey.isNotEmpty()) "present" else "none"}")
            val apiResponse = api.getBinInfo(bin, apiKey = authHeader)
            
            
            println("DEBUG: API Response for $bin:")
            println("  - scheme: ${apiResponse.scheme}")
            println("  - type: ${apiResponse.type}")
            println("  - brand: ${apiResponse.brand}")
            println("  - prepaid: ${apiResponse.prepaid}")
            println("  - country: ${apiResponse.country}")
            println("  - bank: ${apiResponse.bank}")
            println("  - bank.name: ${apiResponse.bank?.name}")
            println("  - bank.url: ${apiResponse.bank?.url}")
            println("  - bank.phone: ${apiResponse.bank?.phone}")
            println("  - bank.city: ${apiResponse.bank?.city}")
            println("  - country.latitude: ${apiResponse.country?.latitude}")
            println("  - country.longitude: ${apiResponse.country?.longitude}")
            
            
            var enhancedBank = apiResponse.bank
            if (apiResponse.bank?.name != null) {
                
                try {
                    println("DEBUG: Attempting geocoding for bank: ${apiResponse.bank.name}")
                    
                    
                    val searchQuery = if (apiResponse.bank.city != null) {
                        "${apiResponse.bank.name}, ${apiResponse.bank.city}"
                    } else {
                        "${apiResponse.bank.name} bank"
                    }
                    
                    val geocodingResults = geocodingService.searchLocation(
                        query = searchQuery
                    )
                    
                    if (geocodingResults.isNotEmpty()) {
                        val result = geocodingResults[0]
                        println("DEBUG: Geocoding result: $result")
                        
                        
                        enhancedBank = apiResponse.bank.copy(
                            // Используем city из API если есть, иначе из геокодинга
                            city = apiResponse.bank.city ?: (result.address?.city ?: result.address?.town),
                            // Добавляем координаты банка
                            latitude = result.lat?.toDoubleOrNull(),
                            longitude = result.lon?.toDoubleOrNull()
                        )
                    }
                } catch (e: Exception) {
                    println("DEBUG: Geocoding failed: ${e.message}")
                }
            }
            
            
            val binInfo = BinInfo(
                bin = bin, // Используем исходный bin номер
                scheme = apiResponse.scheme,
                type = apiResponse.type,
                brand = apiResponse.brand,
                prepaid = apiResponse.prepaid,
                country = apiResponse.country,
                bank = enhancedBank
            )
            
            
            saveBin(binInfo)
            Result.success(binInfo)
            
        } catch (e: HttpException) {
            println("DEBUG: HTTP Exception for $bin: ${e.code()} - ${e.message}")
            when (e.code()) {
                429 -> {
                    // Rate limit exceeded - проверяем кэш
                    val cachedBin = dao.getBinByNumber(bin)
                    if (cachedBin != null) {
                        Result.success(cachedBin)
                    } else {
                        Result.failure(Exception("Превышен лимит запросов. Попробуйте позже."))
                    }
                }
                404 -> {
                    Result.failure(Exception("BIN номер не найден"))
                }
                else -> {
                    
                    val cachedBin = dao.getBinByNumber(bin)
                    if (cachedBin != null) {
                        Result.success(cachedBin)
                    } else {
                        Result.failure(Exception("Ошибка сети: ${e.code()}"))
                    }
                }
            }
        } catch (e: IOException) {
            println("DEBUG: IOException for $bin: ${e.message}")
            
            val cachedBin = dao.getBinByNumber(bin)
            if (cachedBin != null) {
                Result.success(cachedBin)
            } else {
                Result.failure(Exception("Нет подключения к интернету"))
            }
        } catch (e: Exception) {
            println("DEBUG: General Exception for $bin: ${e.message}")
            
            val cachedBin = dao.getBinByNumber(bin)
            if (cachedBin != null) {
                Result.success(cachedBin)
            } else {
                Result.failure(e)
            }
        }
    }

    override fun getAllBins(): Flow<List<BinInfo>> {
        return dao.getAllBins()
    }

    override suspend fun saveBin(binInfo: BinInfo) {
        try {
            dao.insertBin(binInfo)
        } catch (e: Exception) {
            
            println("Error saving BIN to database: ${e.message}")
        }
    }

    override suspend fun getBinByNumber(bin: String): BinInfo? {
        return dao.getBinByNumber(bin)
    }
    
    override suspend fun clearHistory() {
        dao.clearHistory()
    }
} 