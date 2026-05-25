package com.lowerbackstretching.ui.safety

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * The onboarding page that lists red-flag symptoms and asks the user
 * to attest. Read-only list — no per-item checkboxes — with one
 * inline TextButton: `One or more applies` which calls [onOneApplies]
 * to open the advisory overlay.
 *
 * The "None of these apply" action is the pager's footer button,
 * relabeled by `OnboardingScreen` while this page is the current
 * pager item. The screen also records `redFlagScreeningCompletedAt`
 * when the user advances past this page either way.
 */
@Composable
internal fun SafetyCheckPage(onOneApplies: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.HealthAndSafety,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Before we start — a quick safety check",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Stretching helps most back pain, but some symptoms need a doctor's eyes first.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(16.dp))
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        "Do any of these apply to you?",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    redFlags.forEach { flag ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                "·  ",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            Text(
                                flag.short,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = onOneApplies,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("One or more applies")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
