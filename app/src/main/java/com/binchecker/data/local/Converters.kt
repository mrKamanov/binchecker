package com.binchecker.data.local

import androidx.room.TypeConverter
import com.binchecker.domain.model.Bank
import com.binchecker.domain.model.Country
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromCountry(country: Country?): String? {
        return country?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toCountry(countryString: String?): Country? {
        return countryString?.let { gson.fromJson(it, Country::class.java) }
    }

    @TypeConverter
    fun fromBank(bank: Bank?): String? {
        return bank?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toBank(bankString: String?): Bank? {
        return bankString?.let { gson.fromJson(it, Bank::class.java) }
    }
} 