package com.binchecker.domain.repository

interface ApiKeyRepository {
    suspend fun getApiKey(): String
    suspend fun saveApiKey(apiKey: String)
} 