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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terrass.app.domain.model.Terrace
import com.terrass.app.ui.components.common.VoteIndicator

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TerraceDetailSheet(
    terrace: Terrace,
    onVoteUp: () -> Unit,
    onVoteDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Supprimer la terrasse ?") },
            text = { Text("Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text("Supprimer", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Annuler") }
            },
        )
    }

    Column(modifier = modifier.padding(16.dp)) {
        // Header: nom + actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = terrace.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Modifier")
            }
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer",
                    tint = MaterialTheme.colorScheme.error)
            }
        }

        terrace.address?.let {
            Text(it, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(12.dp))

        // Votes
        VoteIndicator(
            thumbsUp = terrace.thumbsUp,
            thumbsDown = terrace.thumbsDown,
            onVoteUp = onVoteUp,
            onVoteDown = onVoteDown,
        )

        Spacer(Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(Modifier.height(12.dp))

        // Attributs en chips
        AttributeSection("Exposition") {
            terrace.sunExposure.orientation?.let { chip(it.label) }
            terrace.sunExposure.exposure?.let { chip(it.label) }
        }

        AttributeSection("Confort") {
            if (terrace.comfort.isCovered) chip("Couverte")
            if (terrace.comfort.isHeated) chip("Chauffée")
            terrace.comfort.furnitureType?.let { chip(it.label) }
            terrace.comfort.size?.let { chip(it.label) }
        }

        AttributeSection("Environnement") {
            terrace.environment.roadProximity?.let { chip("Route: ${it.label}") }
            terrace.environment.noiseLevel?.let { chip(it.label) }
            terrace.environment.viewQuality?.let { chip("Vue: ${it.label}") }
            if (terrace.environment.hasVegetation) chip("Végétation")
        }

        AttributeSection("Service") {
            terrace.service.quality?.let { chip(it.label) }
            terrace.service.priceRange?.let { chip(it.label) }
            terrace.service.cuisineType?.let { chip(it) }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AttributeSection(
    title: String,
    content: ChipCollector.() -> Unit,
) {
    val chips = ChipCollector().apply(content).chips
    if (chips.isEmpty()) return

    Text(title, style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        chips.forEach { label ->
            AssistChip(onClick = {}, label = { Text(label) })
        }
    }
}

private class ChipCollector {
    val chips = mutableListOf<String>()
    fun chip(label: String) { chips.add(label) }
}
