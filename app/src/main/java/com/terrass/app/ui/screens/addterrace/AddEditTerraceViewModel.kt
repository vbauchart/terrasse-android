package com.terrass.app.ui.screens.addterrace

import android.location.Location
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrass.app.domain.model.Comfort
import com.terrass.app.domain.model.Environment
import com.terrass.app.domain.model.NoiseLevel
import com.terrass.app.domain.model.PlaceResult
import com.terrass.app.domain.model.SunTime
import com.terrass.app.domain.model.PriceRange
import com.terrass.app.domain.model.RoadProximity
import com.terrass.app.domain.model.Service
import com.terrass.app.domain.model.ServiceQuality
import com.terrass.app.domain.model.SunExposure
import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.model.TerraceSize
import com.terrass.app.domain.model.ViewQuality
import com.terrass.app.data.location.ReverseGeocodingService
import com.terrass.app.domain.usecase.AddTerraceUseCase
import com.terrass.app.domain.usecase.GetTerraceDetailUseCase
import com.terrass.app.domain.usecase.SearchPlacesUseCase
import com.terrass.app.domain.usecase.UpdateTerraceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTerraceUiState(
    val editingId: Long? = null,
    val name: String = "",
    val latitude: Double = 48.8566,
    val longitude: Double = 2.3522,
    val address: String? = null,
    val sunTimes: Set<SunTime> = emptySet(),
    val isCovered: Boolean = false,
    val isHeated: Boolean = false,
    val size: TerraceSize? = null,
    val roadProximity: RoadProximity? = null,
    val noiseLevel: NoiseLevel? = null,
    val viewQuality: ViewQuality? = null,
    val hasVegetation: Boolean = false,
    val serviceQuality: ServiceQuality? = null,
    val priceRange: PriceRange? = null,
    val cuisineType: String = "",
    val nameError: String? = null,
    val isSaving: Boolean = false,
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<PlaceResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
) {
    val isEditMode: Boolean get() = editingId != null
}

sealed interface AddTerraceEvent {
    data object SaveSuccess : AddTerraceEvent
    data class SaveError(val message: String) : AddTerraceEvent
}

@HiltViewModel
class AddEditTerraceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val addTerraceUseCase: AddTerraceUseCase,
    private val updateTerraceUseCase: UpdateTerraceUseCase,
    private val getTerraceDetailUseCase: GetTerraceDetailUseCase,
    private val searchPlacesUseCase: SearchPlacesUseCase,
    private val reverseGeocodingService: ReverseGeocodingService,
) : ViewModel() {

    private val terraceId: Long? = savedStateHandle.get<String>("id")?.toLongOrNull()

    // Point de référence pour trier les résultats de recherche par distance
    private val refLat: Double = savedStateHandle.get<String>("lat")?.toDoubleOrNull() ?: 48.8566
    private val refLng: Double = savedStateHandle.get<String>("lng")?.toDoubleOrNull() ?: 2.3522
    private val refZoom: Double = savedStateHandle.get<String>("zoom")?.toDoubleOrNull() ?: 12.0

    // Viewbox au format Nominatim : minLon,maxLat,maxLon,minLat
    private fun viewbox(): String {
        val halfDeg = 360.0 / Math.pow(2.0, refZoom) * 2.0
        return "${refLng - halfDeg},${refLat + halfDeg * 0.6},${refLng + halfDeg},${refLat - halfDeg * 0.6}"
    }

    private val _uiState = MutableStateFlow(
        AddTerraceUiState(
            editingId = terraceId,
            latitude = savedStateHandle.get<String>("lat")?.toDoubleOrNull() ?: 48.8566,
            longitude = savedStateHandle.get<String>("lng")?.toDoubleOrNull() ?: 2.3522,
            isLoading = terraceId != null,
        )
    )
    val uiState: StateFlow<AddTerraceUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddTerraceEvent>()
    val events: SharedFlow<AddTerraceEvent> = _events.asSharedFlow()

    private var searchJob: Job? = null

    init {
        if (terraceId != null) {
            loadTerrace(terraceId)
        } else {
            val lat = savedStateHandle.get<String>("lat")?.toDoubleOrNull()
            val lng = savedStateHandle.get<String>("lng")?.toDoubleOrNull()
            if (lat != null && lng != null) {
                reverseGeocode(lat, lng)
            }
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        viewModelScope.launch {
            val address = reverseGeocodingService.getAddress(lat, lng)
            if (address != null && _uiState.value.address == null) {
                _uiState.value = _uiState.value.copy(address = address)
            }
        }
    }

    private fun loadTerrace(id: Long) {
        viewModelScope.launch {
            val terrace = getTerraceDetailUseCase(id).firstOrNull() ?: return@launch
            _uiState.value = _uiState.value.copy(
                name = terrace.name,
                latitude = terrace.latitude,
                longitude = terrace.longitude,
                address = terrace.address,
                sunTimes = terrace.sunExposure.sunTimes,
                isCovered = terrace.comfort.isCovered,
                isHeated = terrace.comfort.isHeated,
                size = terrace.comfort.size,
                roadProximity = terrace.environment.roadProximity,
                noiseLevel = terrace.environment.noiseLevel,
                viewQuality = terrace.environment.viewQuality,
                hasVegetation = terrace.environment.hasVegetation,
                serviceQuality = terrace.service.quality,
                priceRange = terrace.service.priceRange,
                cuisineType = terrace.service.cuisineType ?: "",
                isLoading = false,
            )
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = null)
    }

    fun toggleSunTime(sunTime: SunTime) {
        val current = _uiState.value.sunTimes
        val new = if (sunTime in current) current - sunTime else current + sunTime
        _uiState.value = _uiState.value.copy(sunTimes = new)
    }

    fun updateCovered(covered: Boolean) {
        _uiState.value = _uiState.value.copy(isCovered = covered)
    }

    fun updateHeated(heated: Boolean) {
        _uiState.value = _uiState.value.copy(isHeated = heated)
    }

    fun updateSize(size: TerraceSize?) {
        _uiState.value = _uiState.value.copy(size = size)
    }

    fun updateRoadProximity(proximity: RoadProximity?) {
        _uiState.value = _uiState.value.copy(roadProximity = proximity)
    }

    fun updateNoiseLevel(level: NoiseLevel?) {
        _uiState.value = _uiState.value.copy(noiseLevel = level)
    }

    fun updateViewQuality(quality: ViewQuality?) {
        _uiState.value = _uiState.value.copy(viewQuality = quality)
    }

    fun updateVegetation(has: Boolean) {
        _uiState.value = _uiState.value.copy(hasVegetation = has)
    }

    fun updateServiceQuality(quality: ServiceQuality?) {
        _uiState.value = _uiState.value.copy(serviceQuality = quality)
    }

    fun updatePriceRange(range: PriceRange?) {
        _uiState.value = _uiState.value.copy(priceRange = range)
    }

    fun updateCuisineType(type: String) {
        _uiState.value = _uiState.value.copy(cuisineType = type)
    }

    fun updatePosition(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(latitude = lat, longitude = lng)
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, searchError = null)
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }
        searchJob = viewModelScope.launch {
            delay(500)
            triggerSearch()
        }
    }

    fun triggerSearch() {
        val query = _uiState.value.searchQuery.trim()
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
            searchPlacesUseCase(query, viewbox())
                .onSuccess { results ->
                    val sorted = results.sortedBy { distanceTo(it.latitude, it.longitude) }
                    _uiState.value = _uiState.value.copy(searchResults = sorted, isSearching = false)
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        searchError = error.message ?: "Erreur de recherche",
                        isSearching = false,
                    )
                }
        }
    }

    private fun distanceTo(lat: Double, lng: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(refLat, refLng, lat, lng, results)
        return results[0]
    }

    fun applyPlaceResult(place: PlaceResult) {
        _uiState.value = _uiState.value.copy(
            name = place.name,
            latitude = place.latitude,
            longitude = place.longitude,
            address = place.address,
            nameError = null,
            searchQuery = "",
            searchResults = emptyList(),
        )
    }

    fun save() {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.value = state.copy(nameError = "Le nom est obligatoire")
            return
        }

        _uiState.value = state.copy(isSaving = true)
        viewModelScope.launch {
            val terrace = Terrace(
                id = state.editingId ?: 0,
                name = state.name.trim(),
                latitude = state.latitude,
                longitude = state.longitude,
                address = state.address,
                sunExposure = SunExposure(state.sunTimes),
                comfort = Comfort(state.isCovered, state.isHeated, state.size),
                environment = Environment(state.roadProximity, state.noiseLevel, state.viewQuality, state.hasVegetation),
                service = Service(state.serviceQuality, state.priceRange, state.cuisineType.ifBlank { null }),
            )

            val result = if (state.isEditMode) {
                updateTerraceUseCase(terrace).map { 0L }
            } else {
                addTerraceUseCase(terrace)
            }

            result
                .onSuccess { _events.emit(AddTerraceEvent.SaveSuccess) }
                .onFailure { _events.emit(AddTerraceEvent.SaveError(it.message ?: "Erreur")) }
            _uiState.value = _uiState.value.copy(isSaving = false)
        }
    }
}
