package com.terrass.app.ui.screens.addterrace

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.terrass.app.domain.model.ExposureType
import com.terrass.app.domain.model.FurnitureType
import com.terrass.app.domain.model.NoiseLevel
import com.terrass.app.domain.model.Orientation
import com.terrass.app.domain.model.PriceRange
import com.terrass.app.domain.model.RoadProximity
import com.terrass.app.domain.model.ServiceQuality
import com.terrass.app.domain.model.TerraceSize
import com.terrass.app.domain.model.ViewQuality

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTerraceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditTerraceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AddTerraceEvent.SaveSuccess -> onNavigateBack()
                is AddTerraceEvent.SaveError -> { /* TODO: show snackbar */ }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Modifier la terrasse" else "Ajouter une terrasse") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            // Coordonnées
            Text(
                "Position : %.4f, %.4f".format(uiState.latitude, uiState.longitude),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(16.dp))

            // Nom
            OutlinedTextField(
                value = uiState.name,
                onValueChange = viewModel::updateName,
                label = { Text("Nom de l'établissement *") },
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(24.dp))

            // --- Exposition au soleil ---
            SectionTitle("Exposition au soleil")
            ChipGroup("Orientation", Orientation.entries, uiState.orientation, { it.label }) {
                viewModel.updateOrientation(if (it == uiState.orientation) null else it)
            }
            ChipGroup("Exposition", ExposureType.entries, uiState.exposure, { it.label }) {
                viewModel.updateExposure(if (it == uiState.exposure) null else it)
            }

            Spacer(Modifier.height(16.dp))

            // --- Confort ---
            SectionTitle("Confort & équipement")
            CheckboxRow("Couverte", uiState.isCovered, viewModel::updateCovered)
            CheckboxRow("Chauffée", uiState.isHeated, viewModel::updateHeated)
            ChipGroup("Mobilier", FurnitureType.entries, uiState.furnitureType, { it.label }) {
                viewModel.updateFurnitureType(if (it == uiState.furnitureType) null else it)
            }
            ChipGroup("Taille", TerraceSize.entries, uiState.size, { it.label }) {
                viewModel.updateSize(if (it == uiState.size) null else it)
            }

            Spacer(Modifier.height(16.dp))

            // --- Environnement ---
            SectionTitle("Environnement")
            ChipGroup("Proximité route", RoadProximity.entries, uiState.roadProximity, { it.label }) {
                viewModel.updateRoadProximity(if (it == uiState.roadProximity) null else it)
            }
            ChipGroup("Bruit", NoiseLevel.entries, uiState.noiseLevel, { it.label }) {
                viewModel.updateNoiseLevel(if (it == uiState.noiseLevel) null else it)
            }
            ChipGroup("Vue", ViewQuality.entries, uiState.viewQuality, { it.label }) {
                viewModel.updateViewQuality(if (it == uiState.viewQuality) null else it)
            }
            CheckboxRow("Végétation", uiState.hasVegetation, viewModel::updateVegetation)

            Spacer(Modifier.height(16.dp))

            // --- Service ---
            SectionTitle("Service & prix")
            ChipGroup("Qualité", ServiceQuality.entries, uiState.serviceQuality, { it.label }) {
                viewModel.updateServiceQuality(if (it == uiState.serviceQuality) null else it)
            }
            ChipGroup("Prix", PriceRange.entries, uiState.priceRange, { it.label }) {
                viewModel.updatePriceRange(if (it == uiState.priceRange) null else it)
            }
            OutlinedTextField(
                value = uiState.cuisineType,
                onValueChange = viewModel::updateCuisineType,
                label = { Text("Type de cuisine") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(Modifier.height(24.dp))

            // Bouton sauvegarder
            Button(
                onClick = viewModel::save,
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator()
                } else {
                    Text("Enregistrer")
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> ChipGroup(
    label: String,
    options: List<T>,
    selected: T?,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit,
) {
    Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
    FlowRow(modifier = Modifier.padding(vertical = 4.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option == selected,
                onClick = { onSelect(option) },
                label = { Text(labelOf(option)) },
                modifier = Modifier.padding(end = 4.dp),
            )
        }
    }
}

@Composable
private fun CheckboxRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
