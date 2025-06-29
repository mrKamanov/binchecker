/*
 * Модели данных для работы с BIN информацией
 * 
 * Этот файл содержит все основные модели данных приложения:
 * 
 * BinInfo - основная модель, представляющая информацию о BIN номере:
 * - Содержит все данные, полученные от API binlist.net
 * - Используется как для отображения, так и для сохранения в базу данных
 * - Включает вложенные объекты Country и Bank
 * - Содержит временную метку создания записи
 * - Аннотирована для работы с Room Database
 * 
 * Country - информация о стране:
 * - Основные данные страны (название, код, валюта)
 * - Координаты страны (широта, долгота)
 * - Эмодзи флага страны
 * 
 * Bank - информация о банке:
 * - Название банка
 * - Контактная информация (URL, телефон, город)
 * - Координаты банка (получаются через геокодинг)
 * 
 * Особенности:
 * - Все поля опциональные, так как API может не возвращать некоторые данные
 * - Модели сериализуются/десериализуются через Gson
 * - BinInfo является Entity для Room Database
 * - Поддерживает как бесплатные, так и премиум данные от API
 */

package com.binchecker.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = "bin_history")
data class BinInfo(
    @PrimaryKey
    val bin: String,
    val scheme: String? = null,
    val type: String? = null,
    val brand: String? = null,
    val prepaid: Boolean? = null,
    val country: Country? = null,
    val bank: Bank? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    init {
        require(bin.isNotBlank()) { "BIN cannot be empty" }
    }
}

data class Country(
    val numeric: String? = null,
    val alpha2: String? = null,
    val name: String? = null,
    val emoji: String? = null,
    val currency: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)

data class Bank(
    val name: String? = null,
    val url: String? = null,
    val phone: String? = null,
    val city: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
) 