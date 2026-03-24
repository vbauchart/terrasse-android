package com.terrass.app.ui.screens.map

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    onNavigateToAdd: (lat: Double, lng: Double) -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
    onMenuClick: () -> Unit = {},
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

    val bottomSheetState = rememberStandardBottomSheetState(
        initialValue = SheetValue.PartiallyExpanded,
    )
    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = bottomSheetState,
    )

    val filterCount = uiState.filter.activeCount
    val terraceCount = uiState.terraces.size

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Terrasse") },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    // Bouton filtre avec badge
                    IconButton(onClick = {
                        val newMode = if (uiState.sheetMode == SheetMode.FILTER) SheetMode.LIST else SheetMode.FILTER
                        viewModel.onSheetModeChange(newMode)
                    }) {
                        BadgedBox(
                            badge = {
                                if (filterCount > 0) {
                                    Badge { Text("$filterCount") }
                                }
                            },
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = "Filtres",
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        sheetPeekHeight = 56.dp,
        sheetContent = {
            // Peek header
            Text(
                text = if (uiState.sheetMode == SheetMode.FILTER) {
                    "Filtres" + if (filterCount > 0) " ($filterCount actifs)" else ""
                } else {
                    "$terraceCount terrasse${if (terraceCount != 1) "s" else ""}"
                },
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
            )

            when (uiState.sheetMode) {
                SheetMode.LIST -> {
                    TerraceListContent(
                        terraces = uiState.terraces,
                        onTerraceClick = { terrace -> viewModel.onTerraceSelected(terrace) },
                    )
                }
                SheetMode.FILTER -> {
                    FilterSheet(
                        filter = uiState.filter,
                        onFilterChange = { viewModel.onFilterChange(it) },
                        onReset = { viewModel.onResetFilter() },
                    )
                }
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                center = uiState.center,
                zoom = uiState.zoom,
                terraces = uiState.terraces,
                userLocation = uiState.userLocation,
                onMarkerClick = { terraceId -> viewModel.onMarkerClick(terraceId) },
                onMapLongClick = { geoPoint ->
                    onNavigateToAdd(geoPoint.latitude, geoPoint.longitude)
                },
            )

            // FAB "+"
            FloatingActionButton(
                onClick = {
                    val loc = uiState.userLocation ?: uiState.center
                    onNavigateToAdd(loc.latitude, loc.longitude)
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = "Ajouter une terrasse")
            }

            // Bouton recentrer
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
        }
    }

    // Bottom sheet de détail (modal, au-dessus de tout)
    uiState.selectedTerrace?.let { terrace ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { viewModel.onDismissDetail() },
            sheetState = sheetState,
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
