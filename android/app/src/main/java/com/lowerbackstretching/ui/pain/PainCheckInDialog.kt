package com.lowerbackstretching.ui.pain

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.lowerbackstretching.core.BodyZone
import com.lowerbackstretching.ui.anatomy.BodySilhouette

/**
 * Modal dialog used for both pre- and post-session pain capture. The
 * user reports an integer 0..10 pain level and optionally taps a body
 * zone on the silhouette (tap again to clear). Both [onSubmit] and
 * [onSkip] dismiss the dialog; the caller is responsible for hiding it
 * via its prompt-state flow.
 */
@Composable
fun PainCheckInDialog(
    title: String,
    onSubmit: (painLevel: Int, bodyLocationTag: String?) -> Unit,
    onSkip: () -> Unit,
) {
    var level by remember { mutableIntStateOf(3) }
    var selectedZone by remember { mutableStateOf<BodyZone?>(null) }

    AlertDialog(
        onDismissRequest = onSkip,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "0 = no pain · 10 = severe",
                    style = MaterialTheme.typography.bodySmall,
                )
                Slider(
                    value = level.toFloat(),
                    onValueChange = { level = it.toInt().coerceIn(0, 10) },
                    valueRange = 0f..10f,
                    steps = 9,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = "Pain level $level out of 10" },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("0", style = MaterialTheme.typography.labelSmall)
                    Text(
                        "$level",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text("10", style = MaterialTheme.typography.labelSmall)
                }
                Text(
                    "Where (optional)",
                    style = MaterialTheme.typography.labelMedium,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(modifier = Modifier.width(120.dp)) {
                        BodySilhouette(
                            modifier = Modifier.fillMaxWidth(),
                            onZoneTap = { zone ->
                                selectedZone = if (selectedZone == zone) null else zone
                            },
                            highlightedZones = selectedZone?.let { setOf(it) } ?: emptySet(),
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = selectedZone?.displayName ?: "Tap a zone (or skip)",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                                .padding(12.dp)
                                .semantics {
                                    contentDescription = "Selected pain location: " +
                                        (selectedZone?.displayName ?: "none")
                                },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSubmit(level, selectedZone?.bodyPartTag) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onSkip) { Text("Skip") }
        },
    )
}
