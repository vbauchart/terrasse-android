package com.terrass.app.ui.screens.status

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.terrass.app.data.location.LocationProvider
import com.terrass.app.domain.model.SyncStatus
import com.terrass.app.domain.repository.TerraceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class GpsStatus {
    NO_PERMISSION,
    SEARCHING,
    ACTIVE,
}

data class StatusUiState(
    val gpsStatus: GpsStatus = GpsStatus.SEARCHING,
    val syncStatus: SyncStatus = SyncStatus.IDLE,
)

@HiltViewModel
class StatusViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationProvider: LocationProvider,
    private val repository: TerraceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatusUiState(gpsStatus = initialGpsStatus()))
    val uiState: StateFlow<StatusUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.syncStatus.collect { sync ->
                _uiState.value = _uiState.value.copy(syncStatus = sync)
            }
        }
        if (_uiState.value.gpsStatus != GpsStatus.NO_PERMISSION) {
            viewModelScope.launch {
                locationProvider.locationUpdates()
                    .collect { _uiState.value = _uiState.value.copy(gpsStatus = GpsStatus.ACTIVE) }
            }
        }
    }

    private fun initialGpsStatus(): GpsStatus {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED)
            GpsStatus.SEARCHING
        else
            GpsStatus.NO_PERMISSION
    }
}
