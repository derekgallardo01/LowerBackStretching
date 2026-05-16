package com.lowerbackstretching.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/**
 * Horizontal row of [FilterChip]s. One option is "selected" at a time.
 * `display` converts an option to user-facing text (e.g. kebab to spaces).
 */
@Composable
fun ChipsRow(
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    display: (String) -> String = { it.replace('-', ' ') },
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options, key = { it }) { option ->
            FilterChip(
                selected = selected == option,
                onClick = { onSelect(option) },
                label = { Text(display(option)) },
            )
        }
    }
}
