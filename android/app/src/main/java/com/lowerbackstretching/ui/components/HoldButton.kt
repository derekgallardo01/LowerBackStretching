package com.lowerbackstretching.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lowerbackstretching.notifications.Haptics
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A press-and-hold action button. Short tap is a no-op (so accidental
 * swipes don't fire destructive operations); holding past [holdMillis]
 * fires [onTriggered] once, with a haptic. A `CircularProgressIndicator`
 * fills around the icon while the user holds.
 *
 * Designed for player Previous/Next where a single tap mid-stretch was
 * too easy to mis-trigger.
 */
@Composable
fun HoldButton(
    onTriggered: () -> Unit,
    contentDescription: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    holdMillis: Long = 600L,
    size: Dp = 48.dp,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val progress = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .pointerInput(holdMillis) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    var fireJob: Job? = null
                    val animJob = scope.launch {
                        progress.snapTo(0f)
                        progress.animateTo(
                            targetValue = 1f,
                            animationSpec = tween(
                                durationMillis = holdMillis.toInt(),
                                easing = LinearEasing,
                            ),
                        )
                    }
                    fireJob = scope.launch {
                        delay(holdMillis)
                        onTriggered()
                        Haptics.short(context)
                    }
                    waitForUpOrCancellation()
                    // Cancel both jobs on release.
                    fireJob.cancel()
                    animJob.cancel()
                    scope.launch {
                        progress.animateTo(0f, tween(durationMillis = 150))
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (progress.value > 0f) {
            CircularProgressIndicator(
                progress = { progress.value },
                modifier = Modifier.size(size),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp,
            )
        }
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(size * 0.6f),
        )
    }
}
