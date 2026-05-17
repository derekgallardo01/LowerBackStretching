package com.lowerbackstretching.ui.flexibility

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lowerbackstretching.core.FlexibilityDelta
import com.lowerbackstretching.data.db.FlexibilityTestEntity
import java.text.DateFormat
import java.util.Date

/**
 * History rendering for the flexibility self-test: the latest snapshot
 * shown with per-metric deltas, plus a compact one-liner per older
 * entry. Kept in its own file because the input form upstream is a
 * distinct concern.
 */

@Composable
internal fun LatestCard(latest: FlexibilityTestEntity, delta: FlexibilityDelta) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(formatTime(latest.recordedAtEpochMillis), style = MaterialTheme.typography.titleMedium)
            MeasurementRow("Sit & reach", latest.sitAndReachCm, delta.sitAndReachCm)
            MeasurementRow("Toe touch", latest.toeTouchCm, delta.toeTouchCm)
            MeasurementRow("Shoulder reach", latest.shoulderReachCm, delta.shoulderReachCm)
        }
    }
}

@Composable
private fun MeasurementRow(label: String, value: Float?, delta: Float?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(
            buildString {
                append(value?.let { "%.1f cm".format(it) } ?: "—")
                if (delta != null) append("   (${formatDelta(delta)} cm)")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = if ((delta ?: 0f) > 0) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
internal fun HistoryRow(entry: FlexibilityTestEntity) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(formatTime(entry.recordedAtEpochMillis), style = MaterialTheme.typography.titleSmall)
            Text(
                listOfNotNull(
                    entry.sitAndReachCm?.let { "Sit&reach %.1f".format(it) },
                    entry.toeTouchCm?.let { "Toes %.1f".format(it) },
                    entry.shoulderReachCm?.let { "Shoulder %.1f".format(it) },
                ).joinToString(" · ").ifEmpty { "(no measurements)" },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

private fun formatTime(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))

private fun formatDelta(d: Float): String =
    if (d >= 0) "+%.1f".format(d) else "%.1f".format(d)
