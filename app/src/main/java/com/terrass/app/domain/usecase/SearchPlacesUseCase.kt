package com.terrass.app.domain.usecase

import com.terrass.app.data.remote.PhotonService
import com.terrass.app.domain.model.PlaceResult
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val photonService: PhotonService,
) {
    suspend operator fun invoke(query: String, bbox: String? = null): Result<List<PlaceResult>> =
        runCatching { photonService.search(query, bbox) }
}
