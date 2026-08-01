package com.lowerbackstretching.ui.settings.cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.R
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.components.pressScale
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Surfaces the same red-flag advisory the user saw during onboarding,
 * so they can review symptom guidance any time without rewalking the
 * pager. Tapping the card navigates to `Dest.safetyAdvisory`.
 *
 * Also reports when the screening was last completed — `Prefs` records
 * that timestamp as the user advances past the onboarding safety page,
 * and this is where it's read back.
 */
@Composable
fun SafetyCard(
    onOpen: () -> Unit,
    vm: AppViewModel = viewModel(),
) {
    val completedAt by vm.prefs.redFlagScreeningCompletedAt.collectAsState(initial = 0L)
    val reviewedFormat = stringResource(R.string.safety_reviewed_on)
    val completedLabel = remember(completedAt) {
        completedAt.takeIf { it > 0L }?.let {
            reviewedFormat.format(
                Instant
                    .ofEpochMilli(it)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
            )
        }
    }

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
                Text(stringResource(R.string.settings_safety_check), style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.settings_safety_check_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                )
                completedLabel?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            )
        }
    }
}
