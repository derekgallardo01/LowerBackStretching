package com.lowerbackstretching.ui.safety

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Full-screen advisory shown when the user attests on the onboarding
 * safety check that "one or more applies," or when they review the
 * screening from Settings. Informational, not blocking — we can't stop
 * a user from continuing, but we explain why a clinician should look
 * first and surface a clear "I've already seen a doctor" path.
 *
 * Behaviour:
 * - [onSeenDoctor]: user confirms they've been cleared by a clinician.
 * - [onContinueAnyway]: user dismisses without confirmation.
 * - When opened from Settings (review mode), pass the same callback to
 *   both — see `AppNav` wiring; the screen still reads correctly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedFlagAdvisoryScreen(
    onSeenDoctor: () -> Unit,
    onContinueAnyway: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety check") },
                navigationIcon = {
                    IconButton(onClick = onContinueAnyway) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.size(96.dp),
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.HealthAndSafety,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(48.dp),
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "Please see a doctor before stretching.",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "What you described could be a sign of something that needs " +
                    "medical attention. Stretching can help most back pain, " +
                    "but symptoms like numbness, leg pain, or loss of control " +
                    "should be evaluated first.",
                style = MaterialTheme.typography.bodyLarge,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "This isn't a diagnosis — just a reminder. If you've already " +
                    "been cleared by a clinician, you can continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = onSeenDoctor,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("I've already seen a doctor")
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = onContinueAnyway,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Continue anyway")
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
