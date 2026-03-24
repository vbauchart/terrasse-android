package com.terrass.app.domain.usecase

import com.terrass.app.domain.model.FilterCriteria
import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.repository.TerraceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTerracesUseCase @Inject constructor(
    private val repository: TerraceRepository,
) {
    operator fun invoke(filter: FilterCriteria = FilterCriteria()): Flow<List<Terrace>> =
        repository.getAllTerraces().map { terraces ->
            if (filter.activeCount == 0) terraces
            else terraces.filter { filter.matches(it) }
        }
}
