package com.terrass.app.ui.screens.map.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terrass.app.domain.model.Terrace

@Composable
fun TerraceListContent(
    terraces: List<Terrace>,
    onTerraceClick: (Terrace) -> Unit,
    modifier: Modifier = Modifier,
    hasActiveFilters: Boolean = false,
) {
    if (terraces.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (hasActiveFilters) "Aucun résultat pour ces filtres" else "Aucune terrasse",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    } else {
        LazyColumn(modifier = modifier.fillMaxWidth()) {
            items(terraces, key = { it.id }) { terrace ->
                TerraceListItem(
                    terrace = terrace,
                    onClick = { onTerraceClick(terrace) },
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}
