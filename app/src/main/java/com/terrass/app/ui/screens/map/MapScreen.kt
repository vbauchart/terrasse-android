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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.terrass.app.R
import com.terrass.app.ui.components.map.OsmMapView
import com.terrass.app.ui.screens.map.components.TerraceDetailSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onNavigateToAdd: (lat: Double, lng: Double) -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
    onMenuClick: () -> Unit = {},
    viewModel: MapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Terrasse")
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_logo),
                        contentDescription = "Logo Terrasse",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(32.dp)
                            .padding(end = 8.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
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
                Icon(Icons.Default.LocationOn, contentDescription = "Centrer sur ma position")
            }
        }
    }

    // Bottom sheet de détail
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
