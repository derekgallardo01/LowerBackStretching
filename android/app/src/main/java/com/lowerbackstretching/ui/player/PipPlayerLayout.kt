package com.lowerbackstretching.ui.player

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lowerbackstretching.core.DurationUnit
import com.lowerbackstretching.core.formatDuration
import com.lowerbackstretching.ui.components.YouTubePlayerView

/**
 * Compact layout shown while the activity is in Picture-in-Picture
 * mode. PiP windows are too small for controls — the user taps the
 * PiP to expand back.
 */
@Composable
internal fun PipPlayerLayout(
    videoId: String,
    startSeconds: Int,
    remainingSeconds: Int,
    progress: Float,
    durationUnit: DurationUnit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        YouTubePlayerView(
            videoId = videoId,
            startSeconds = startSeconds,
            modifier = Modifier.fillMaxSize(),
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
        ) {
            Text(
                formatDuration(remainingSeconds, durationUnit),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp),
            )
            ThinProgressBar(progress = progress)
        }
    }
}

/** Slim accent-color progress bar; shared by the main player and the PiP layout. */
@Composable
internal fun ThinProgressBar(progress: Float) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().height(3.dp),
        color = MaterialTheme.colorScheme.secondary,
        trackColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f),
    )
}
