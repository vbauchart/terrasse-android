package com.terrass.app.domain.usecase

import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.repository.TerraceRepository
import javax.inject.Inject

class AddTerraceUseCase @Inject constructor(
    private val repository: TerraceRepository,
) {
    suspend operator fun invoke(terrace: Terrace): Result<Long> {
        if (terrace.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Le nom est obligatoire"))
        }
        return Result.success(repository.addTerrace(terrace))
    }
}
