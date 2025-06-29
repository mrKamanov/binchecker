package com.binchecker.domain.usecase

import com.binchecker.domain.model.BinInfo
import com.binchecker.domain.repository.BinRepository
import javax.inject.Inject

class CheckBinUseCase @Inject constructor(
    private val repository: BinRepository
) {
    suspend operator fun invoke(bin: String): Result<BinInfo> {
        if (bin.length < 6 || bin.length > 8) {
            return Result.failure(IllegalArgumentException("BIN должен содержать от 6 до 8 цифр"))
        }
        if (!bin.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("BIN должен содержать только цифры"))
        }
        return repository.getBinInfo(bin)
    }
} 