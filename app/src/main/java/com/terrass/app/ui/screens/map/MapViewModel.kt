package com.terrass.app.ui.screens.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrass.app.data.location.LocationProvider
import com.terrass.app.domain.model.FilterCriteria
import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.usecase.DeleteTerraceUseCase
import com.terrass.app.domain.usecase.GetTerracesUseCase
import com.terrass.app.domain.usecase.VoteTerraceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

enum class ViewMode { MAP, LIST }

data class MapUiState(
    val center: GeoPoint = GeoPoint(48.8566, 2.3522),
    val zoom: Double = 12.0,
    val hasLocationPermission: Boolean = false,
    val isTrackingLocation: Boolean = false,
    val terraces: List<Terrace> = emptyList(),
    val userLocation: GeoPoint? = null,
    val selectedTerrace: Terrace? = null,
    val isLocating: Boolean = false,
    val locationError: String? = null,
    val filter: FilterCriteria = FilterCriteria(),
    val viewMode: ViewMode = ViewMode.MAP,
    val isFilterSheetVisible: Boolean = false,
)

@HiltViewModel
class MapViewModel @Inject constructor(
    private val locationProvider: LocationProvider,
    private val getTerracesUseCase: GetTerracesUseCase,
    private val voteTerraceUseCase: VoteTerraceUseCase,
    private val deleteTerraceUseCase: DeleteTerraceUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var terracesJob: Job? = null

    init {
        loadTerraces()
    }

    private fun loadTerraces() {
        terracesJob?.cancel()
        terracesJob = viewModelScope.launch {
            getTerracesUseCase(_uiState.value.filter)
                .catch { /* silently handle */ }
                .collect { terraces ->
                    val selected = _uiState.value.selectedTerrace
                    _uiState.value = _uiState.value.copy(
                        terraces = terraces,
                        selectedTerrace = selected?.let { sel ->
                            terraces.find { it.id == sel.id }
                        },
                    )
                }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.value = _uiState.value.copy(hasLocationPermission = granted)
        if (granted) {
            startLocationTracking()
        }
    }

    private fun startLocationTracking() {
        if (_uiState.value.isTrackingLocation) return
        _uiState.value = _uiState.value.copy(isTrackingLocation = true)

        viewModelScope.launch {
            locationProvider.lastLocation()?.let { location ->
                updateCenter(location)
            }

            locationProvider.locationUpdates()
                .catch { /* GPS indisponible */ }
                .collect { location ->
                    _uiState.value = _uiState.value.copy(
                        userLocation = GeoPoint(location.latitude, location.longitude),
                    )
                }
        }
    }

    private fun updateCenter(location: Location) {
        _uiState.value = _uiState.value.copy(
            center = GeoPoint(location.latitude, location.longitude),
            userLocation = GeoPoint(location.latitude, location.longitude),
        )
    }

    fun onCenterOnUser() {
        if (!_uiState.value.hasLocationPermission) {
            _uiState.value = _uiState.value.copy(
                locationError = "Permission de localisation non accordée",
            )
            return
        }
        _uiState.value = _uiState.value.copy(isLocating = true, locationError = null)
        viewModelScope.launch {
            try {
                val location = locationProvider.lastLocation()
                if (location != null) {
                    _uiState.value = _uiState.value.copy(
                        center = GeoPoint(location.latitude, location.longitude),
                        userLocation = GeoPoint(location.latitude, location.longitude),
                        zoom = 15.0,
                        isLocating = false,
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLocating = false,
                        locationError = "Position introuvable",
                    )
                }
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLocating = false,
                    locationError = "Impossible de récupérer la position",
                )
            }
        }
    }

    fun onDismissLocationError() {
        _uiState.value = _uiState.value.copy(locationError = null)
    }

    fun onMarkerClick(terraceId: Long) {
        val terrace = _uiState.value.terraces.find { it.id == terraceId }
        _uiState.value = _uiState.value.copy(selectedTerrace = terrace)
    }

    fun onTerraceSelected(terrace: Terrace) {
        _uiState.value = _uiState.value.copy(
            selectedTerrace = terrace,
            center = GeoPoint(terrace.latitude, terrace.longitude),
            zoom = 17.0,
        )
    }

    fun onDismissDetail() {
        _uiState.value = _uiState.value.copy(selectedTerrace = null)
    }

    fun onVote(terraceId: Long, isPositive: Boolean) {
        viewModelScope.launch {
            voteTerraceUseCase(terraceId, isPositive)
        }
    }

    fun onDelete(terraceId: Long) {
        viewModelScope.launch {
            deleteTerraceUseCase(terraceId)
            _uiState.value = _uiState.value.copy(selectedTerrace = null)
        }
    }

    fun onFilterChange(newFilter: FilterCriteria) {
        _uiState.value = _uiState.value.copy(filter = newFilter)
        loadTerraces()
    }

    fun onResetFilter() {
        onFilterChange(FilterCriteria())
    }

    fun onToggleViewMode() {
        val next = if (_uiState.value.viewMode == ViewMode.MAP) ViewMode.LIST else ViewMode.MAP
        _uiState.value = _uiState.value.copy(viewMode = next)
    }

    fun onToggleFilterSheet() {
        _uiState.value = _uiState.value.copy(isFilterSheetVisible = !_uiState.value.isFilterSheetVisible)
    }

    fun onDismissFilterSheet() {
        _uiState.value = _uiState.value.copy(isFilterSheetVisible = false)
    }
}
