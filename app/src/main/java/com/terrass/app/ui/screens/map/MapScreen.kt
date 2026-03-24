package com.terrass.app.ui.screens.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.terrass.app.ui.components.map.OsmMapView
import com.terrass.app.ui.screens.map.components.FilterSheet
import com.terrass.app.ui.screens.map.components.TerraceDetailSheet
import com.terrass.app.ui.screens.map.components.TerraceListContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToAdd: (lat: Double, lng: Double, zoom: Double) -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
    onNavigateToStatus: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.locationError) {
        uiState.locationError?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onDismissLocationError()
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.any { it }
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            )
        )
    }

    val filterCount = uiState.filter.activeCount
    val isMapMode = uiState.viewMode == ViewMode.MAP
    var menuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Terrasse") },
                navigationIcon = {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("Vue carte") },
                                onClick = {
                                    if (!isMapMode) viewModel.onToggleViewMode()
                                    menuExpanded = false
                                },
                                leadingIcon = if (isMapMode) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null,
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Vue liste") },
                                onClick = {
                                    if (isMapMode) viewModel.onToggleViewMode()
                                    menuExpanded = false
                                },
                                leadingIcon = if (!isMapMode) {
                                    { Icon(Icons.Default.Check, contentDescription = null) }
                                } else null,
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Statut") },
                                onClick = {
                                    menuExpanded = false
                                    onNavigateToStatus()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Info, contentDescription = null)
                                },
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.onToggleFilterSheet() }) {
                        BadgedBox(
                            badge = {
                                if (filterCount > 0) Badge { Text("$filterCount") }
                            },
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filtres")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isMapMode) {
                OsmMapView(
                    modifier = Modifier.fillMaxSize(),
                    center = uiState.center,
                    zoom = uiState.zoom,
                    terraces = uiState.terraces,
                    userLocation = uiState.userLocation,
                    onMarkerClick = { terraceId -> viewModel.onMarkerClick(terraceId) },
                    onMapLongClick = { geoPoint ->
                        onNavigateToAdd(geoPoint.latitude, geoPoint.longitude, uiState.zoom)
                    },
                )

                FloatingActionButton(
                    onClick = {
                        val loc = uiState.userLocation ?: uiState.center
                        onNavigateToAdd(loc.latitude, loc.longitude, uiState.zoom)
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter une terrasse")
                }

                SmallFloatingActionButton(
                    onClick = { viewModel.onCenterOnUser() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 88.dp, end = 20.dp),
                ) {
                    if (uiState.isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    } else {
                        Icon(Icons.Default.LocationOn, contentDescription = "Centrer sur ma position")
                    }
                }
            } else {
                TerraceListContent(
                    terraces = uiState.terraces,
                    onTerraceClick = { terrace ->
                        viewModel.onTerraceSelected(terrace)
                        viewModel.onToggleViewMode() // revenir à la carte
                    },
                    hasActiveFilters = filterCount > 0,
                )
            }
        }
    }

    // Bottom sheet filtre (modal)
    if (uiState.isFilterSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onDismissFilterSheet() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            FilterSheet(
                filter = uiState.filter,
                onFilterChange = { viewModel.onFilterChange(it) },
                onReset = { viewModel.onResetFilter() },
            )
        }
    }

    // Bottom sheet de détail (modal)
    uiState.selectedTerrace?.let { terrace ->
        ModalBottomSheet(
            onDismissRequest = { viewModel.onDismissDetail() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        ) {
            TerraceDetailSheet(
                terrace = terrace,
                onVoteUp = { viewModel.onVote(terrace.id, true) },
                onVoteDown = { viewModel.onVote(terrace.id, false) },
                onEdit = {
                    viewModel.onDismissDetail()
                    onNavigateToEdit(terrace.id)
                },
                onDelete = { viewModel.onDelete(terrace.id) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
