/*
 * API сервисы для работы с внешними API
 * 
 * Этот файл содержит интерфейсы и модели для взаимодействия с внешними API:
 * 
 * BinApiService - основной API сервис для binlist.net:
 * - Получение информации о BIN номерах
 * - Поддержка бесплатного и премиум API
 * - Автоматическое добавление заголовков (Accept-Version, Authorization)
 * - Обработка различных форматов ответов
 * 
 * GeocodingService - сервис для геокодинга через OpenStreetMap:
 * - Получение координат по названию банка
 * - Дополнение информации о городе банка
 * - Используется для получения точных координат банка
 * 
 * Модели данных:
 * - BinApiResponse - ответ от binlist.net API (без поля bin)
 * - GeocodingResult - результат геокодинга
 * - AddressDetails - детали адреса из геокодинга
 * - CardNumber - информация о номере карты
 * 
 * Особенности:
 * - Использует Retrofit для HTTP запросов
 * - Поддерживает как GET запросы с параметрами, так и с путями
 * - Все модели совместимы с Gson сериализацией
 * - API ключ передается в заголовке Authorization для премиум доступа
 * - Геокодинг выполняется для дополнения данных банка
 */

package com.binchecker.data.api

import com.binchecker.domain.model.BinInfo
import com.binchecker.domain.model.Bank
import com.binchecker.domain.model.Country
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface BinApiService {
    @GET("{bin}")
    suspend fun getBinInfo(
        @Path("bin") bin: String,
        @Header("Accept-Version") acceptVersion: String = "3",
        @Header("Authorization") apiKey: String? = null
    ): BinApiResponse
}


interface GeocodingService {
    @GET("search")
    suspend fun searchLocation(
        @Query("q") query: String,
        @Query("format") format: String = "json",
        @Query("limit") limit: Int = 1,
        @Query("addressdetails") addressDetails: Int = 1
    ): List<GeocodingResult>
}

data class GeocodingResult(
    val lat: String? = null,
    val lon: String? = null,
    val display_name: String? = null,
    val address: AddressDetails? = null
)

data class AddressDetails(
    val city: String? = null,
    val town: String? = null,
    val state: String? = null,
    val country: String? = null
)


data class BinApiResponse(
    val number: CardNumber? = null,
    val scheme: String? = null,
    val type: String? = null,
    val brand: String? = null,
    val prepaid: Boolean? = null,
    val country: Country? = null,
    val bank: Bank? = null
)

data class CardNumber(
    val length: Int? = null,
    val luhn: Boolean? = null
) 