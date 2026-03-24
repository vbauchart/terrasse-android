package com.terrass.app.domain.usecase

import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.repository.TerraceRepository
import javax.inject.Inject

class UpdateTerraceUseCase @Inject constructor(
    private val repository: TerraceRepository,
) {
    suspend operator fun invoke(terrace: Terrace): Result<Unit> {
        if (terrace.name.isBlank()) {
            return Result.failure(IllegalArgumentException("Le nom est obligatoire"))
        }
        repository.updateTerrace(terrace)
        return Result.success(Unit)
    }
}
