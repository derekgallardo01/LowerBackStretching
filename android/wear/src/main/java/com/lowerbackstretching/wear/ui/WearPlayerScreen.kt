package com.lowerbackstretching.wear.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import com.lowerbackstretching.core.player.PlayerEngine
import com.lowerbackstretching.wear.WatchContent
import com.lowerbackstretching.wear.WearHaptics
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.drop

@Composable
fun WearPlayerScreen() {
    val context = LocalContext.current
    val routine = remember { WatchContent.loadRoutine(context) }
    val engine = remember { PlayerEngine(routine.stretches) }
    val snapshot by engine.state.collectAsState()

    // 1Hz tick driving the engine.
    LaunchedEffect(engine) {
        while (true) {
            delay(1000)
            engine.tick()
        }
    }

    // Haptic on each index advance.
    LaunchedEffect(engine) {
        engine.state
            .distinctUntilChangedBy { it.index }
            .drop(1)
            .collect { s ->
                if (!s.finished) WearHaptics.short(context)
            }
    }

    // Haptic on finish.
    LaunchedEffect(engine) {
        engine.finishedEvents.collect { WearHaptics.finish(context) }
    }

    Scaffold(timeText = { TimeText() }) {
        if (snapshot.finished) {
            FinishedView()
            return@Scaffold
        }
        val current = snapshot.current ?: return@Scaffold

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                current.name,
                style = MaterialTheme.typography.title3,
                textAlign = TextAlign.Center,
                maxLines = 2,
            )
            Text(
                "${snapshot.remainingSeconds}s",
                style = MaterialTheme.typography.display2,
            )
            Text(
                "${snapshot.index + 1} of ${snapshot.stretches.size}",
                style = MaterialTheme.typography.caption2,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Button(
                    onClick = engine::previous,
                    enabled = snapshot.index > 0,
                    colors = ButtonDefaults.secondaryButtonColors(),
                ) { Text("◀") }
                Button(onClick = engine::togglePlay) {
                    Text(if (snapshot.running) "II" else "▶")
                }
                Button(
                    onClick = { engine.next() },
                    colors = ButtonDefaults.secondaryButtonColors(),
                ) { Text("▶▶") }
            }
        }
    }
}

@Composable
private fun FinishedView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Nice work.", style = MaterialTheme.typography.title2)
            Text("Done.", style = MaterialTheme.typography.body1)
        }
    }
}
