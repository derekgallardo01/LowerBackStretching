package com.lowerbackstretching.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * One-time celebration shown when the user reaches a major streak
 * milestone (7 / 30 / 100 / 365 days). Distinct from achievement
 * unlocks, which are quieter and accumulate; milestones are big
 * inflection moments and earn a full dialog.
 *
 * Dismiss-on-tap-outside disabled — the user has to acknowledge with
 * the Continue button, which keeps the moment feeling deliberate
 * without trapping them.
 */
@Composable
fun MilestoneModal(
    days: Int,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = false),
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(96.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Filled.Park,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp),
                        )
                    }
                }
                Text(
                    text = "$days-day streak",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = milestoneBody(days),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                )
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text("Continue")
                }
            }
        }
    }
}

private fun milestoneBody(days: Int): String = when (days) {
    7 -> "A whole week of showing up. That's how habits start."
    30 -> "A month. Your back is genuinely stronger than it was."
    100 -> "A hundred days. This isn't a phase anymore — it's who you are."
    365 -> "A full year. Quietly, you became someone who takes care of their back."
    else -> "Another day, another deposit."
}
