package com.terrass.app.domain.usecase

import com.terrass.app.data.remote.NominatimService
import com.terrass.app.domain.model.PlaceResult
import javax.inject.Inject

class SearchPlacesUseCase @Inject constructor(
    private val nominatimService: NominatimService,
) {
    suspend operator fun invoke(query: String, viewbox: String? = null): Result<List<PlaceResult>> =
        runCatching { nominatimService.search(query, viewbox) }
}
