package com.lowerbackstretching.ui.settings.cards

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun AboutCard() {
    SettingsCard(verticalSpacing = 0.dp) {
        Text("About", style = MaterialTheme.typography.titleMedium)
        Text(
            "Lower Back Stretching · v0.1",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
    }
}
