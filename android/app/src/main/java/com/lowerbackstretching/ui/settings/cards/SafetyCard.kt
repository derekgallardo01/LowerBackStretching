package com.lowerbackstretching.ui.settings.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import com.lowerbackstretching.ui.components.pressScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Surfaces the same red-flag advisory the user saw during onboarding,
 * so they can review symptom guidance any time without rewalking the
 * pager. Tapping the card navigates to `Dest.safetyAdvisory`.
 */
@Composable
fun SafetyCard(onOpen: () -> Unit) {
    SettingsCard(verticalSpacing = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .pressScale()
                .clickable(onClick = onOpen),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.HealthAndSafety,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text("Safety check", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Symptoms to watch for before stretching",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}
