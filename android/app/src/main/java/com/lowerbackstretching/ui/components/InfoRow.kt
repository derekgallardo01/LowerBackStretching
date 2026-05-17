package com.lowerbackstretching.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A standard list-card with a title, a subtitle (typically meta info like
 * `"5 stretches · 3 min"`), an optional body paragraph, and an optional
 * trailing icon/checkmark.
 *
 * Used by Programs, Stretches, Routines, Sessions, and Program-day rows.
 *
 * When [onLongClick] is provided the card uses [combinedClickable] so
 * both gestures land. Otherwise it falls back to Card's regular onClick
 * (which gives the standard ripple).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InfoRow(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    body: String? = null,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(14.dp)
    val cardModifier = modifier.fillMaxWidth()

    when {
        onClick != null && onLongClick != null -> {
            Card(
                modifier = cardModifier.combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
                shape = shape,
                colors = CardDefaults.cardColors(),
            ) {
                InfoRowContent(title, subtitle, body, trailing)
            }
        }
        onClick != null -> {
            Card(modifier = cardModifier, shape = shape, onClick = onClick) {
                InfoRowContent(title, subtitle, body, trailing)
            }
        }
        else -> {
            Card(modifier = cardModifier, shape = shape) {
                InfoRowContent(title, subtitle, body, trailing)
            }
        }
    }
}

@Composable
private fun InfoRowContent(
    title: String,
    subtitle: String,
    body: String?,
    trailing: @Composable (() -> Unit)?,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.padding(end = 8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
            if (body != null) {
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        if (trailing != null) trailing()
    }
}
