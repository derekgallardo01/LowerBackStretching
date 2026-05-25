package com.lowerbackstretching.ui.pain

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
import com.lowerbackstretching.core.BodyZone
import com.lowerbackstretching.core.PainContext
import com.lowerbackstretching.core.SessionPainDelta
import com.lowerbackstretching.data.db.PainLogEntity
import java.text.DateFormat
import java.util.Date

@Composable
internal fun LatestPainCard(latest: PainLogEntity) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(formatTime(latest.recordedAtEpochMillis), style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = latest.context.label(),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "${latest.painLevel}/10",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            latest.bodyLocationTag?.let { tag ->
                Text(
                    text = "Where: ${displayName(tag)}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
internal fun SessionDeltaRow(delta: SessionPainDelta, recordedAtEpochMillis: Long, locationTag: String?) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(formatTime(recordedAtEpochMillis), style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = locationTag?.let { displayName(it) } ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                )
                val d = delta.delta
                Text(
                    text = buildString {
                        append(delta.pre?.toString() ?: "?")
                        append(" → ")
                        append(delta.post.toString())
                        if (d != null) {
                            append("   (")
                            append(if (d > 0) "+$d" else d.toString())
                            append(")")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (d != null && d < 0) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
internal fun PainHistoryRow(entry: PainLogEntity) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(formatTime(entry.recordedAtEpochMillis), style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = listOfNotNull(
                        entry.context.label(),
                        entry.bodyLocationTag?.let { displayName(it) },
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                )
                Text(
                    text = "${entry.painLevel}/10",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun String.label(): String = when (this) {
    PainContext.PRE_SESSION -> "Before session"
    PainContext.POST_SESSION -> "After session"
    else -> this
}

private fun displayName(bodyPartTag: String): String =
    BodyZone.entries.firstOrNull { it.bodyPartTag == bodyPartTag }?.displayName
        ?: bodyPartTag.replace('-', ' ')

private fun formatTime(epochMillis: Long): String =
    DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(epochMillis))
