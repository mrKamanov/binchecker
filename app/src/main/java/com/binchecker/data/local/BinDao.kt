package com.binchecker.data.local

import androidx.room.*
import com.binchecker.domain.model.BinInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface BinDao {
    @Query("SELECT * FROM bin_history ORDER BY timestamp DESC")
    fun getAllBins(): Flow<List<BinInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBin(binInfo: BinInfo)

    @Query("SELECT * FROM bin_history WHERE bin = :bin")
    suspend fun getBinByNumber(bin: String): BinInfo?

    @Query("DELETE FROM bin_history")
    suspend fun clearHistory()
} 