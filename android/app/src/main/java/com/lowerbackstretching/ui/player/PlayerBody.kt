package com.lowerbackstretching.ui.player

import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.LocalPictureInPictureHost
import com.lowerbackstretching.data.DurationUnit
import com.lowerbackstretching.core.bodyZonesForTags
import com.lowerbackstretching.data.formatDuration
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.anatomy.BodySilhouette
import com.lowerbackstretching.ui.components.YouTubePlayerView

/**
 * The shared player UI used by all three [PlayerScreen] entry points.
 * Reads `vm.state` and renders the current stretch, progress bar, and
 * controls. Renders [FinishedView] once the routine completes. Holds
 * the screen on and locks portrait orientation while visible.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PlayerBody(
    title: String,
    onFinished: () -> Unit,
    onBack: () -> Unit,
    vm: PlayerViewModel,
    appVm: AppViewModel = viewModel(),
) {
    val state by vm.state.collectAsState()
    val unit by appVm.prefs.durationUnit.collectAsState(initial = DurationUnit.SECONDS)
    val pipHost = LocalPictureInPictureHost.current
    val inPip by pipHost.inPip.collectAsState()
    KeepScreenOnAndLockPortrait()
    DisposableEffect(vm, pipHost) {
        pipHost.pipEligible.value = true
        onDispose {
            pipHost.pipEligible.value = false
            vm.stop()
        }
    }

    if (inPip) {
        val snapshot = state
        val current = snapshot?.current
        if (snapshot != null && current != null && !snapshot.finished) {
            PipPlayerLayout(
                videoId = current.youtubeId,
                remainingSeconds = snapshot.remainingSeconds,
                progress = snapshot.routineProgress,
            )
        } else {
            FinishedView(modifier = Modifier.fillMaxSize(), onDone = onFinished)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        }
    ) { inner ->
        val snapshot = state ?: return@Scaffold
        val current = snapshot.current ?: return@Scaffold

        if (snapshot.finished) {
            FinishedView(modifier = Modifier.padding(inner).fillMaxSize(), onDone = onFinished)
            return@Scaffold
        }

        Column(
            modifier = Modifier.padding(inner).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            YouTubePlayerView(videoId = current.youtubeId, modifier = Modifier.fillMaxWidth())

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(current.name, style = MaterialTheme.typography.headlineMedium)
                    Text(current.description, style = MaterialTheme.typography.bodyMedium)
                }
                val zones = remember(current.bodyParts) { bodyZonesForTags(current.bodyParts) }
                if (zones.isNotEmpty()) {
                    Box(modifier = Modifier.width(64.dp)) {
                        BodySilhouette(
                            modifier = Modifier.fillMaxWidth(),
                            highlightedZones = zones,
                        )
                    }
                }
            }

            current.whatYouShouldFeel?.let { WhatYouShouldFeelOverlay(it) }

            LinearProgressIndicator(
                progress = { snapshot.progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp),
            )
            Text(
                "${formatDuration(snapshot.remainingSeconds, unit)} · ${snapshot.index + 1} of ${snapshot.stretches.size}",
                style = MaterialTheme.typography.labelLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            ThinProgressBar(progress = snapshot.routineProgress)

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = vm::previous) {
                    Icon(Icons.Filled.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(36.dp))
                }
                Button(onClick = vm::togglePlay) {
                    Text(if (snapshot.running) "Pause" else "Resume")
                }
                IconButton(onClick = vm::next) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Next", modifier = Modifier.size(36.dp))
                }
            }
        }
    }
}

/**
 * While composed: window keeps the screen on and the activity is locked
 * to portrait. Cleared on dispose so other screens behave normally.
 */
@Composable
private fun KeepScreenOnAndLockPortrait() {
    val context = LocalContext.current
    DisposableEffect(context) {
        val activity = context as? ComponentActivity
        val window = activity?.window
        val priorOrientation = activity?.requestedOrientation
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            if (priorOrientation != null) {
                activity.requestedOrientation = priorOrientation
            }
        }
    }
}

/**
 * Compact layout shown while the activity is in Picture-in-Picture
 * mode. PiP windows are too small for controls — the user taps the
 * PiP to expand back.
 */
@Composable
private fun PipPlayerLayout(videoId: String, remainingSeconds: Int, progress: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        YouTubePlayerView(videoId = videoId, modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            Text(
                "${remainingSeconds}s",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp),
            )
            ThinProgressBar(progress = progress)
        }
    }
}

@Composable
private fun ThinProgressBar(progress: Float) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().height(3.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
    )
}

@Composable
private fun WhatYouShouldFeelOverlay(text: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(Icons.Filled.Spa, contentDescription = null)
            Column {
                Text("What you should feel", style = MaterialTheme.typography.labelMedium)
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun FinishedView(modifier: Modifier, onDone: () -> Unit) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Nice work.", style = MaterialTheme.typography.headlineLarge)
            Text("Session logged.", style = MaterialTheme.typography.bodyLarge)
            Button(onClick = onDone) { Text("Done") }
        }
    }
}
