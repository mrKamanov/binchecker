package com.binchecker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binchecker.domain.repository.ApiKeyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val apiKeyRepository: ApiKeyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadApiKey()
    }

    private fun loadApiKey() {
        viewModelScope.launch {
            try {
                val apiKey = apiKeyRepository.getApiKey()
                _uiState.value = _uiState.value.copy(apiKey = apiKey)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка загрузки API ключа: ${e.message}"
                )
            }
        }
    }

    fun onApiKeyChanged(apiKey: String) {
        _uiState.value = _uiState.value.copy(apiKey = apiKey)
    }

    fun saveApiKey() {
        viewModelScope.launch {
            try {
                val oldApiKey = apiKeyRepository.getApiKey()
                val newApiKey = _uiState.value.apiKey
                
                
                apiKeyRepository.saveApiKey(newApiKey)
                
                
                val oldMode = oldApiKey.isNotEmpty()
                val newMode = newApiKey.isNotEmpty()
                
                if (oldMode != newMode) {
                    println("DEBUG: API mode changed from ${if (oldMode) "Premium" else "Free"} to ${if (newMode) "Premium" else "Free"}")
                    println("DEBUG: History preserved - user data should not be lost")
                }
                
                _uiState.value = _uiState.value.copy(
                    error = null,
                    successMessage = "API ключ успешно сохранен"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка сохранения API ключа: ${e.message}"
                )
            }
        }
    }

    fun clearApiKey() {
        viewModelScope.launch {
            try {
                val oldApiKey = apiKeyRepository.getApiKey()
                
                
                apiKeyRepository.saveApiKey("")
                
                
                if (oldApiKey.isNotEmpty()) {
                    println("DEBUG: Switching from Premium to Free mode")
                    println("DEBUG: History preserved - user data should not be lost")
                }
                
                _uiState.value = _uiState.value.copy(
                    apiKey = "",
                    error = null,
                    successMessage = "API ключ очищен"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка очистки API ключа: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

data class SettingsUiState(
    val apiKey: String = "",
    val error: String? = null,
    val successMessage: String? = null
) 