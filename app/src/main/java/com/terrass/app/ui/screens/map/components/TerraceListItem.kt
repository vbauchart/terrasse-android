package com.terrass.app.ui.screens.map.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.terrass.app.domain.model.Terrace
import com.terrass.app.ui.components.common.AttributeChip

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TerraceListItem(
    terrace: Terrace,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = terrace.name,
                style = MaterialTheme.typography.titleSmall,
            )

            val chips = buildList {
                terrace.sunExposure.sunTimes.forEach { add(it.label) }
                terrace.environment.noiseLevel?.let { add(it.label) }
                terrace.service.priceRange?.let { add(it.label) }
            }
            if (chips.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    chips.forEach { label -> AttributeChip(label) }
                }
            }
        }

        val pct = terrace.votePercentage
        Text(
            text = if (pct < 0) "–" else "$pct%",
            style = MaterialTheme.typography.titleMedium,
            color = when {
                pct < 0 -> MaterialTheme.colorScheme.outline
                pct >= 60 -> MaterialTheme.colorScheme.primary
                pct >= 40 -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.error
            },
            modifier = Modifier.padding(start = 12.dp, top = 2.dp),
        )
    }
}
