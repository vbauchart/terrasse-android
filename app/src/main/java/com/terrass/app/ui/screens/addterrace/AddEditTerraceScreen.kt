package com.terrass.app.ui.screens.addterrace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.terrass.app.domain.model.NoiseLevel
import com.terrass.app.domain.model.PlaceResult
import com.terrass.app.domain.model.SunTime
import com.terrass.app.domain.model.PriceRange
import com.terrass.app.domain.model.RoadProximity
import com.terrass.app.domain.model.ServiceQuality
import com.terrass.app.domain.model.TerraceSize
import com.terrass.app.domain.model.ViewQuality
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditTerraceScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditTerraceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSearchSheetVisible by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

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
            Spacer(Modifier.height(8.dp))

            // Bouton recherche établissement
            OutlinedButton(
                onClick = { isSearchSheetVisible = true },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Rechercher l'établissement")
            }

            Spacer(Modifier.height(12.dp))

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

            // --- Ensoleillement ---
            SectionTitle("Ensoleillement")
            MultiChipGroup("Ensoleillée", SunTime.entries, uiState.sunTimes, { it.label }) {
                viewModel.toggleSunTime(it)
            }

            Spacer(Modifier.height(16.dp))

            // --- Confort ---
            SectionTitle("Confort & équipement")
            CheckboxRow("Couverte", uiState.isCovered, viewModel::updateCovered)
            CheckboxRow("Chauffée", uiState.isHeated, viewModel::updateHeated)
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

    if (isSearchSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                isSearchSheetVisible = false
                viewModel.updateSearchQuery("")
            },
            sheetState = sheetState,
        ) {
            SearchSheetContent(
                query = uiState.searchQuery,
                results = uiState.searchResults,
                isSearching = uiState.isSearching,
                searchError = uiState.searchError,
                onQueryChange = viewModel::updateSearchQuery,
                onResultClick = { place ->
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        isSearchSheetVisible = false
                        viewModel.applyPlaceResult(place)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchSheetContent(
    query: String,
    results: List<PlaceResult>,
    isSearching: Boolean,
    searchError: String?,
    onQueryChange: (String) -> Unit,
    onResultClick: (PlaceResult) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            label = { Text("Rechercher un établissement") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Effacer")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )

        Spacer(Modifier.height(8.dp))

        when {
            isSearching -> {
                Row(
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
            searchError != null -> {
                Text(
                    text = searchError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
            results.isEmpty() && query.isNotBlank() -> {
                Text(
                    text = "Aucun résultat",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp),
                )
            }
            else -> {
                LazyColumn {
                    items(results) { place ->
                        ListItem(
                            headlineContent = { Text(place.name) },
                            supportingContent = { Text(place.displayName) },
                            modifier = Modifier.clickable { onResultClick(place) },
                        )
                        HorizontalDivider()
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun <T> MultiChipGroup(
    label: String,
    options: List<T>,
    selected: Set<T>,
    labelOf: (T) -> String,
    onToggle: (T) -> Unit,
) {
    Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
    FlowRow(modifier = Modifier.padding(vertical = 4.dp)) {
        options.forEach { option ->
            FilterChip(
                selected = option in selected,
                onClick = { onToggle(option) },
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
