package com.terrass.app.domain.usecase

import com.terrass.app.domain.repository.TerraceRepository
import javax.inject.Inject

class DeleteTerraceUseCase @Inject constructor(
    private val repository: TerraceRepository,
) {
    suspend operator fun invoke(id: Long) = repository.deleteTerrace(id)
}
