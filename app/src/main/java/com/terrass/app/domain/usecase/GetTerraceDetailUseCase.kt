package com.terrass.app.domain.usecase

import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.repository.TerraceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTerraceDetailUseCase @Inject constructor(
    private val repository: TerraceRepository,
) {
    operator fun invoke(id: Long): Flow<Terrace?> = repository.getTerraceById(id)
}
