package com.binchecker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binchecker.domain.model.BinInfo
import com.binchecker.domain.usecase.CheckBinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BinInputViewModel @Inject constructor(
    private val checkBinUseCase: CheckBinUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BinInputUiState())
    val uiState: StateFlow<BinInputUiState> = _uiState.asStateFlow()

    fun onBinInputChanged(bin: String) {
        
        val filteredBin = bin.filter { it.isDigit() }
        
        
        val isValid = filteredBin.length in 6..8 && filteredBin.all { it.isDigit() }
        
        _uiState.value = _uiState.value.copy(
            binInput = filteredBin,
            isButtonEnabled = isValid,
            validationError = when {
                filteredBin.isNotEmpty() && filteredBin.length < 6 -> "BIN должен содержать минимум 6 цифр"
                filteredBin.length > 8 -> "BIN не может содержать более 8 цифр"
                filteredBin.isNotEmpty() && !filteredBin.all { it.isDigit() } -> "BIN должен содержать только цифры"
                else -> null
            }
        )
    }

    fun checkBin() {
        val bin = _uiState.value.binInput.trim()
        if (bin.isEmpty()) return
        
        
        if (bin.length !in 6..8 || !bin.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(
                error = "Неверный формат BIN номера"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                binInfo = null
            )

            checkBinUseCase(bin)
                .onSuccess { binInfo ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        binInfo = binInfo,
                        error = null
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Произошла ошибка",
                        binInfo = null
                    )
                }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class BinInputUiState(
    val binInput: String = "",
    val isLoading: Boolean = false,
    val binInfo: BinInfo? = null,
    val error: String? = null,
    val validationError: String? = null,
    val isButtonEnabled: Boolean = false
) 