package com.terrass.app.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp

@Composable
fun VoteIndicator(
    thumbsUp: Int,
    thumbsDown: Int,
    onVoteUp: () -> Unit,
    onVoteDown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val total = thumbsUp + thumbsDown
    val percentText = if (total == 0) {
        "Pas encore noté"
    } else {
        val pct = (thumbsUp * 100) / total
        "$pct% positif · $total avis"
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        FilledIconButton(
            onClick = onVoteUp,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
            modifier = Modifier.size(40.dp),
        ) {
            Icon(Icons.Default.ThumbUp, contentDescription = "Pouce en haut", modifier = Modifier.size(20.dp))
        }

        FilledIconButton(
            onClick = onVoteDown,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
            modifier = Modifier.size(40.dp),
        ) {
            Icon(
                Icons.Default.ThumbUp,
                contentDescription = "Pouce en bas",
                modifier = Modifier.size(20.dp).scale(scaleX = 1f, scaleY = -1f),
            )
        }

        Spacer(Modifier.width(8.dp))

        Text(
            text = percentText,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
