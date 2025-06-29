package com.binchecker.domain.usecase

import com.binchecker.domain.model.BinInfo
import com.binchecker.domain.repository.BinRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: BinRepository
) {
    operator fun invoke(): Flow<List<BinInfo>> {
        return repository.getAllBins()
    }
    
    suspend fun clearHistory() {
        repository.clearHistory()
    }
} 