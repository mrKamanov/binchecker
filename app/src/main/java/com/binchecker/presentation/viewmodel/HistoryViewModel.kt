package com.binchecker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.binchecker.domain.model.BinInfo
import com.binchecker.domain.usecase.GetHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    private var allBins = listOf<BinInfo>()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getHistoryUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Ошибка загрузки истории"
                    )
                }
                .collect { bins ->
                    allBins = bins
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bins = filterBins(bins, _uiState.value.searchQuery),
                        error = null
                    )
                }
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query,
            bins = filterBins(allBins, query)
        )
    }
    
    private fun filterBins(bins: List<BinInfo>, query: String): List<BinInfo> {
        if (query.isEmpty()) return bins
        return bins.filter { bin ->
            bin.bin.contains(query, ignoreCase = true) ||
            bin.bank?.name?.contains(query, ignoreCase = true) == true ||
            bin.country?.name?.contains(query, ignoreCase = true) == true ||
            bin.scheme?.contains(query, ignoreCase = true) == true
        }
    }
    
    fun clearHistory() {
        viewModelScope.launch {
            try {
                getHistoryUseCase.clearHistory()
                allBins = emptyList()
                _uiState.value = _uiState.value.copy(
                    bins = emptyList(),
                    searchQuery = "",
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Ошибка при очистке истории: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun refreshHistory() {
        loadHistory()
    }

    fun onBinClick(bin: BinInfo) {
        _uiState.value = _uiState.value.copy(selectedBin = bin)
    }

    fun onDetailDismiss() {
        _uiState.value = _uiState.value.copy(selectedBin = null)
    }
}

data class HistoryUiState(
    val bins: List<BinInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val searchQuery: String = "",
    val selectedBin: BinInfo? = null
) 