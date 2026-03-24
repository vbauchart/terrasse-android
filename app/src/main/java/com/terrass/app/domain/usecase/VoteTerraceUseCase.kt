package com.terrass.app.domain.usecase

import com.terrass.app.domain.repository.TerraceRepository
import javax.inject.Inject

class VoteTerraceUseCase @Inject constructor(
    private val repository: TerraceRepository,
) {
    suspend operator fun invoke(terraceId: Long, isPositive: Boolean) =
        repository.vote(terraceId, isPositive)
}
