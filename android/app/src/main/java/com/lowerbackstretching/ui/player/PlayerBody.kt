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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lowerbackstretching.LocalPictureInPictureHost
import com.lowerbackstretching.R
import com.lowerbackstretching.core.DurationUnit
import com.lowerbackstretching.core.bodyZonesForTags
import com.lowerbackstretching.core.formatDuration
import com.lowerbackstretching.ui.AppViewModel
import com.lowerbackstretching.ui.anatomy.BodySilhouette
import com.lowerbackstretching.ui.components.MilestoneModal
import com.lowerbackstretching.ui.components.StretchAnimation3DView
import com.lowerbackstretching.ui.components.StretchAnimationView
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
    // semantics {} blocks are not @Composable, so resolve labels up front.
    val switchTo2D = stringResource(R.string.player_switch_to_2d)
    val switchTo3D = stringResource(R.string.player_switch_to_3d)
    val previousLabel = stringResource(R.string.player_previous)
    val nextLabel = stringResource(R.string.player_next)
    val pauseLabel = stringResource(R.string.player_pause)
    val resumeLabel = stringResource(R.string.player_resume)
    val markCompleteLabel = stringResource(R.string.player_mark_complete)
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
                animation = current.animation,
                youtubeId = current.youtubeId,
                videoUrl = current.videoUrl,
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
            title = stringResource(R.string.pain_prompt_pre),
            onSubmit = { level, tag -> vm.onPrePromptSubmit(level, tag) },
            onSkip = { vm.onPrePromptSkip() },
        )
        is PainPromptState.PostSession -> PainCheckInDialog(
            title = stringResource(R.string.pain_prompt_post),
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
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { inner ->
        val snapshot = state ?: return@Scaffold
        val current = snapshot.current ?: return@Scaffold
        val stretchPositionLabel = stringResource(
            R.string.player_stretch_position_named,
            snapshot.index + 1,
            snapshot.stretches.size,
            current.name,
        )
        val remainingLabel = pluralStringResource(
            R.plurals.player_seconds_remaining,
            snapshot.remainingSeconds,
            snapshot.remainingSeconds,
        )

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
            var show3D by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                if (show3D) {
                    StretchAnimation3DView(
                        animation = current.animation,
                        youtubeId = current.youtubeId,
                        videoUrl = current.videoUrl,
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    StretchAnimationView(
                        animation = current.animation,
                        youtubeId = current.youtubeId,
                        videoUrl = current.videoUrl,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                FilledIconButton(
                    onClick = { show3D = !show3D },
                    // 48.dp, not 36: an explicit .size() overrides IconButton's
                    // built-in minimum touch target, so the smaller value was
                    // shipping a control below the 48dp accessibility floor.
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(48.dp)
                        .semantics {
                            contentDescription = if (show3D) switchTo2D else switchTo3D
                        },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                ) {
                    Text(
                        text = if (show3D) "2D" else "3D",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

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
                        contentDescription = remainingLabel
                    },
                )
                // Announce stretch changes, not the countdown. A live region on
                // the timer would speak over the user every single second; this
                // fires once per transition, which is the event that actually
                // needs announcing when you're looking at the floor rather than
                // the screen. The remaining time stays readable on demand via the
                // timer's own contentDescription above.
                Text(
                    text = stringResource(
                        R.string.player_stretch_position,
                        snapshot.index + 1,
                        snapshot.stretches.size,
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Polite
                        contentDescription = stretchPositionLabel
                    },
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
                IconButton(
                    onClick = vm::previous,
                    modifier = Modifier
                        .size(56.dp)
                        .semantics { contentDescription = previousLabel },
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                    )
                }
                Spacer(Modifier.width(24.dp))
                FilledIconButton(
                    onClick = vm::togglePlay,
                    modifier = Modifier
                        .size(80.dp)
                        .semantics {
                            contentDescription = if (snapshot.running) pauseLabel else resumeLabel
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
                IconButton(
                    onClick = vm::next,
                    modifier = Modifier
                        .size(56.dp)
                        .semantics { contentDescription = nextLabel },
                ) {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                    )
                }
            }

            Button(
                onClick = vm::next,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = markCompleteLabel },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                ),
            ) {
                Icon(Icons.Filled.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (snapshot.index == snapshot.stretches.size - 1) {
                        stringResource(R.string.player_finish_routine)
                    } else {
                        markCompleteLabel
                    },
                )
            }
        }
    }
}

/**
 * While composed: window keeps the screen on and the activity is locked
 * to portrait. Cleared on dispose so other screens behave normally.
 *
 * Lint's SourceLockedOrientationActivity warning is suppressed deliberately. It
 * is right in general — locking orientation hurts tablets and foldables — but
 * this is a full-screen timed exercise the user follows while lying on the
 * floor, and a rotation mid-stretch reconfigures the activity and interrupts the
 * routine. The lock is scoped to this one screen and restored on dispose; every
 * other screen rotates freely.
 */
@Suppress("SourceLockedOrientationActivity")
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
                Text(stringResource(R.string.player_what_you_should_feel), style = MaterialTheme.typography.labelMedium)
                Text(text, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
