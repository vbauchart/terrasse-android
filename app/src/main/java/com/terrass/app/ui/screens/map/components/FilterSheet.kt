package com.terrass.app.ui.screens.map.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terrass.app.domain.model.ExposureType
import com.terrass.app.domain.model.FilterCriteria
import com.terrass.app.domain.model.FurnitureType
import com.terrass.app.domain.model.NoiseLevel
import com.terrass.app.domain.model.Orientation
import com.terrass.app.domain.model.PriceRange
import com.terrass.app.domain.model.ServiceQuality
import com.terrass.app.domain.model.TerraceSize
import com.terrass.app.domain.model.ViewQuality

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterSheet(
    filter: FilterCriteria,
    onFilterChange: (FilterCriteria) -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Filtres", style = MaterialTheme.typography.titleLarge)
            TextButton(onClick = onReset) { Text("Réinitialiser") }
        }

        Spacer(Modifier.height(8.dp))

        // Exposition
        SectionTitle("Exposition")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ExposureType.entries.forEach { item ->
                val selected = item in filter.exposureTypes
                FilterChip(
                    selected = selected,
                    onClick = {
                        val new = if (selected) filter.exposureTypes - item else filter.exposureTypes + item
                        onFilterChange(filter.copy(exposureTypes = new))
                    },
                    label = { Text(item.label) },
                )
            }
        }

        // Orientation
        SectionTitle("Orientation")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Orientation.entries.forEach { item ->
                val selected = item in filter.orientations
                FilterChip(
                    selected = selected,
                    onClick = {
                        val new = if (selected) filter.orientations - item else filter.orientations + item
                        onFilterChange(filter.copy(orientations = new))
                    },
                    label = { Text(item.label) },
                )
            }
        }

        // Confort
        SectionTitle("Confort")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BooleanChip("Couverte", filter.isCovered) { onFilterChange(filter.copy(isCovered = it)) }
            BooleanChip("Chauffée", filter.isHeated) { onFilterChange(filter.copy(isHeated = it)) }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FurnitureType.entries.forEach { item ->
                val selected = item in filter.furnitureTypes
                FilterChip(
                    selected = selected,
                    onClick = {
                        val new = if (selected) filter.furnitureTypes - item else filter.furnitureTypes + item
                        onFilterChange(filter.copy(furnitureTypes = new))
                    },
                    label = { Text(item.label) },
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TerraceSize.entries.forEach { item ->
                val selected = item in filter.sizes
                FilterChip(
                    selected = selected,
                    onClick = {
                        val new = if (selected) filter.sizes - item else filter.sizes + item
                        onFilterChange(filter.copy(sizes = new))
                    },
                    label = { Text(item.label) },
                )
            }
        }

        // Environnement
        SectionTitle("Environnement")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            NoiseLevel.entries.forEach { item ->
                val selected = item in filter.noiseLevels
                FilterChip(
                    selected = selected,
                    onClick = {
                        val new = if (selected) filter.noiseLevels - item else filter.noiseLevels + item
                        onFilterChange(filter.copy(noiseLevels = new))
                    },
                    label = { Text(item.label) },
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ViewQuality.entries.forEach { item ->
                val selected = item in filter.viewQualities
                FilterChip(
                    selected = selected,
                    onClick = {
                        val new = if (selected) filter.viewQualities - item else filter.viewQualities + item
                        onFilterChange(filter.copy(viewQualities = new))
                    },
                    label = { Text(item.label) },
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BooleanChip("Végétation", filter.hasVegetation) { onFilterChange(filter.copy(hasVegetation = it)) }
        }

        // Service & prix
        SectionTitle("Service & prix")
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ServiceQuality.entries.forEach { item ->
                val selected = item in filter.serviceQualities
                FilterChip(
                    selected = selected,
                    onClick = {
                        val new = if (selected) filter.serviceQualities - item else filter.serviceQualities + item
                        onFilterChange(filter.copy(serviceQualities = new))
                    },
                    label = { Text(item.label) },
                )
            }
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PriceRange.entries.forEach { item ->
                val selected = item in filter.priceRanges
                FilterChip(
                    selected = selected,
                    onClick = {
                        val new = if (selected) filter.priceRanges - item else filter.priceRanges + item
                        onFilterChange(filter.copy(priceRanges = new))
                    },
                    label = { Text(item.label) },
                )
            }
        }

        // Minimum positif
        SectionTitle("Note minimum")
        val currentMin = filter.minPositivePercent
        Row(verticalAlignment = Alignment.CenterVertically) {
            Slider(
                value = (currentMin ?: 0).toFloat(),
                onValueChange = { value ->
                    val intVal = value.toInt()
                    onFilterChange(filter.copy(minPositivePercent = if (intVal == 0) null else intVal))
                },
                valueRange = 0f..100f,
                steps = 9,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = if (currentMin != null) "$currentMin%" else "–",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp),
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
    )
}

@Composable
private fun BooleanChip(label: String, value: Boolean?, onChange: (Boolean?) -> Unit) {
    FilterChip(
        selected = value == true,
        onClick = { onChange(if (value == true) null else true) },
        label = { Text(label) },
    )
}
