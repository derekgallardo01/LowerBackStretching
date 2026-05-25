package com.lowerbackstretching.ui.player

import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.LocalPictureInPictureHost
import com.lowerbackstretching.core.DurationUnit
import com.lowerbackstretching.core.bodyZonesForTags
import com.lowerbackstretching.core.formatDuration
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.anatomy.BodySilhouette
import com.lowerbackstretching.ui.components.HoldButton
import com.lowerbackstretching.ui.components.MilestoneModal
import com.lowerbackstretching.ui.components.YouTubePlayerView
import com.lowerbackstretching.ui.pain.PainCheckInDialog

/**
 * The shared player UI used by all three [PlayerScreen] entry points.
 * Reads `vm.state` and renders the current stretch, progress bar, and
 * controls. Delegates the PiP layout to [PipPlayerLayout] and the
 * completion screen to [FinishedView]. Holds the screen on and locks
 * portrait orientation while visible.
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
    val painPrompt by vm.painPrompt.collectAsState()
    val finishedSession by vm.finishedSession.collectAsState()
    val milestone by vm.milestone.collectAsState()
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
                startSeconds = current.videoStartSeconds,
                remainingSeconds = snapshot.remainingSeconds,
                progress = snapshot.routineProgress,
                durationUnit = unit,
            )
        } else {
            FinishedView(
                modifier = Modifier.fillMaxSize(),
                onDone = onFinished,
                finishedSession = finishedSession,
            )
        }
        return
    }

    when (val prompt = painPrompt) {
        PainPromptState.PreSession -> PainCheckInDialog(
            title = "How's your back right now?",
            onSubmit = { level, tag -> vm.onPrePromptSubmit(level, tag) },
            onSkip = { vm.onPrePromptSkip() },
        )
        is PainPromptState.PostSession -> PainCheckInDialog(
            title = "How does it feel now?",
            onSubmit = { level, tag -> vm.onPostPromptSubmit(prompt.sessionId, level, tag) },
            onSkip = { vm.onPostPromptSkip() },
        )
        PainPromptState.Hidden -> Unit
    }

    milestone?.let { days ->
        MilestoneModal(days = days, onDismiss = { vm.dismissMilestone() })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        }
    ) { inner ->
        val snapshot = state ?: return@Scaffold
        val current = snapshot.current ?: return@Scaffold

        if (snapshot.finished) {
            FinishedView(
                modifier = Modifier.padding(inner).fillMaxSize(),
                onDone = onFinished,
                finishedSession = finishedSession,
            )
            return@Scaffold
        }

        Column(
            modifier = Modifier.padding(inner).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            YouTubePlayerView(
                videoId = current.youtubeId,
                startSeconds = current.videoStartSeconds,
                modifier = Modifier.fillMaxWidth(),
            )

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

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = formatDuration(snapshot.remainingSeconds, unit),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.semantics {
                        contentDescription = "${snapshot.remainingSeconds} seconds remaining"
                    },
                )
                Text(
                    text = "Stretch ${snapshot.index + 1} of ${snapshot.stretches.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                )
            }

            val animatedStretchProgress by animateFloatAsState(
                targetValue = snapshot.progress.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 950),
                label = "stretchProgress",
            )
            LinearProgressIndicator(
                progress = { animatedStretchProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp),
            )
            ThinProgressBar(progress = snapshot.routineProgress)

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HoldButton(
                    onTriggered = vm::previous,
                    contentDescription = "Hold to go back",
                    icon = Icons.Filled.SkipPrevious,
                )
                Spacer(Modifier.width(24.dp))
                FilledIconButton(
                    onClick = vm::togglePlay,
                    modifier = Modifier
                        .size(80.dp)
                        .semantics {
                            contentDescription = if (snapshot.running) "Pause" else "Resume"
                        },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                ) {
                    Icon(
                        imageVector = if (snapshot.running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Spacer(Modifier.width(24.dp))
                HoldButton(
                    onTriggered = vm::next,
                    contentDescription = "Hold to skip ahead",
                    icon = Icons.Filled.SkipNext,
                )
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
