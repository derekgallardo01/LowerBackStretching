package com.lowerbackstretching.wear.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.lowerbackstretching.core.DurationUnit
import com.lowerbackstretching.core.formatDuration
import com.lowerbackstretching.core.model.WatchRoutine
import com.lowerbackstretching.wear.WatchContent

@Composable
fun WearRoutineListScreen(
    onSelectRoutine: (WatchRoutine) -> Unit,
) {
    val context = LocalContext.current
    val routinesFlow = remember(context) { WatchContent.observeAllRoutines(context) }
    val routines by routinesFlow.collectAsState(initial = listOf(WatchContent.loadDefaultRoutine(context)))
    val listState = rememberScalingLazyListState()

    Scaffold(
        timeText = { TimeText() },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) },
    ) {
        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            item {
                Text(
                    text = "Routines",
                    style = MaterialTheme.typography.title3,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }

            items(routines, key = { it.id.ifEmpty { it.name } }) { routine ->
                Chip(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectRoutine(routine) },
                    label = {
                        Text(
                            text = routine.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.button,
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = com.lowerbackstretching.core.stretchCountSubtitle(
                                routine.stretches.size,
                                routine.totalDurationSeconds,
                            ),
                            style = MaterialTheme.typography.caption2,
                        )
                    },
                    colors = ChipDefaults.primaryChipColors(),
                )
            }
        }
    }
}
