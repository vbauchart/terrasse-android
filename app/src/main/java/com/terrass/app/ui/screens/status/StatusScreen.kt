package com.terrass.app.ui.screens.status

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsNotFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.terrass.app.BuildConfig
import com.terrass.app.domain.model.SyncStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatusViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statut") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            val (gpsIcon, gpsColor, gpsLabel) = when (uiState.gpsStatus) {
                GpsStatus.NO_PERMISSION -> Triple(Icons.Default.GpsOff, MaterialTheme.colorScheme.error, "Permission refusée")
                GpsStatus.SEARCHING -> Triple(Icons.Default.GpsNotFixed, MaterialTheme.colorScheme.secondary, "Recherche en cours…")
                GpsStatus.ACTIVE -> Triple(Icons.Default.GpsFixed, Color(0xFF4CAF50), "Position disponible")
            }

            val (syncIcon, syncColor, syncLabel) = when (uiState.syncStatus) {
                SyncStatus.IDLE -> Triple(Icons.Default.Cloud, MaterialTheme.colorScheme.secondary, "Non démarré")
                SyncStatus.SYNCING -> Triple(Icons.Default.CloudSync, MaterialTheme.colorScheme.primary, "Synchronisation…")
                SyncStatus.UP_TO_DATE -> Triple(Icons.Default.Cloud, Color(0xFF4CAF50), "À jour")
                SyncStatus.OFFLINE -> Triple(Icons.Default.CloudOff, MaterialTheme.colorScheme.error, "Hors ligne")
            }

            StatusRow(
                icon = gpsIcon,
                iconTint = gpsColor,
                title = "GPS",
                subtitle = gpsLabel,
            )

            StatusRow(
                icon = syncIcon,
                iconTint = syncColor,
                title = "Synchronisation",
                subtitle = syncLabel,
                detail = BuildConfig.POCKETBASE_URL,
            )
        }
    }
}

@Composable
private fun StatusRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    detail: String? = null,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(32.dp),
            )
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = iconTint,
                )
                if (detail != null) {
                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
