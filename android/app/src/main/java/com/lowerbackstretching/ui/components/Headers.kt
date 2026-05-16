package com.lowerbackstretching.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Big screen-level title (e.g. "Stretches", "Calendar"). */
@Composable
fun ScreenHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier,
    )
}

/** Section divider inside a screen (e.g. "Recent sessions", "My routines"). */
@Composable
fun SectionHeader(text: String, topPadding: Dp = 8.dp) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(top = topPadding),
    )
}
