package com.terrass.app.ui.screens.map

import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrass.app.data.location.LocationProvider
import com.terrass.app.domain.model.Terrace
import com.terrass.app.domain.usecase.DeleteTerraceUseCase
import com.terrass.app.domain.usecase.GetTerracesUseCase
import com.terrass.app.domain.usecase.VoteTerraceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

data class MapUiState(
    val center: GeoPoint = GeoPoint(48.8566, 2.3522),
    val zoom: Double = 15.0,
    val hasLocationPermission: Boolean = false,
    val isTrackingLocation: Boolean = false,
    val terraces: List<Terrace> = emptyList(),
    val userLocation: GeoPoint? = null,
    val selectedTerrace: Terrace? = null,
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

    init {
        loadTerraces()
    }

    private fun loadTerraces() {
        viewModelScope.launch {
            getTerracesUseCase()
                .catch { /* silently handle */ }
                .collect { terraces ->
                    val selected = _uiState.value.selectedTerrace
                    _uiState.value = _uiState.value.copy(
                        terraces = terraces,
                        // Rafraîchir la terrasse sélectionnée si elle existe encore
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
        if (_uiState.value.hasLocationPermission) {
            viewModelScope.launch {
                locationProvider.lastLocation()?.let { updateCenter(it) }
            }
        }
    }

    fun onMarkerClick(terraceId: Long) {
        val terrace = _uiState.value.terraces.find { it.id == terraceId }
        _uiState.value = _uiState.value.copy(selectedTerrace = terrace)
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
}
