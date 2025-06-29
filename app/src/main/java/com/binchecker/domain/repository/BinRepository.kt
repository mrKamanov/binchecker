package com.binchecker.domain.repository

import com.binchecker.domain.model.BinInfo
import kotlinx.coroutines.flow.Flow

interface BinRepository {
    suspend fun getBinInfo(bin: String): Result<BinInfo>
    fun getAllBins(): Flow<List<BinInfo>>
    suspend fun saveBin(binInfo: BinInfo)
    suspend fun getBinByNumber(bin: String): BinInfo?
    suspend fun clearHistory()
} 