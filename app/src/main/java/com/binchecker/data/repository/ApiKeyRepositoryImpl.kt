package com.binchecker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.binchecker.domain.repository.ApiKeyRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ApiKeyRepository {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "bin_checker_prefs",
        Context.MODE_PRIVATE
    )

    override suspend fun getApiKey(): String {
        return prefs.getString(KEY_API_KEY, "") ?: ""
    }

    override suspend fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey).apply()
    }

    companion object {
        private const val KEY_API_KEY = "api_key"
    }
} 