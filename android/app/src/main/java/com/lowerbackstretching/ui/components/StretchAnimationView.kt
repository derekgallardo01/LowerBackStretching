package com.lowerbackstretching.ui.components

import android.content.Intent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.lowerbackstretching.core.SKELETON_BONES
import com.lowerbackstretching.core.interpolatedPose
import com.lowerbackstretching.core.model.StretchAnimationSpec

/**
 * The player surface that replaces the YouTube embed. If [animation] is
 * present we render a looping stick-figure that demonstrates the stretch;
 * otherwise we show a static placeholder. In both cases, if [youtubeId]
 * points to a real video we surface a small "Watch demo on YouTube" link
 * that opens the video in the YouTube app or browser — embedded playback
 * was removed because YouTube's mid-2025 embedder-verification tightening
 * (Error 152) made WebView-based embeds unreliable across devices.
 */
@Composable
fun StretchAnimationView(
    animation: StretchAnimationSpec?,
    youtubeId: String,
    videoUrl: String? = null,
    modifier: Modifier = Modifier,
) {
    val surface = MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = modifier
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(12.dp))
            .background(surface),
    ) {
        if (!videoUrl.isNullOrBlank()) {
            StretchVideoPlayer(
                videoUrl = videoUrl,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (animation != null && animation.poses.isNotEmpty()) {
            AnimatedFigure(
                spec = animation,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxSize(),
            )
            if (hasWatchableVideo(youtubeId)) {
                WatchOnYouTubeChip(
                    youtubeId = youtubeId,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                )
            }
        } else {
            NoAnimationPlaceholder(youtubeId = youtubeId)
        }
    }
}

@Composable
private fun AnimatedFigure(
    spec: StretchAnimationSpec,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "stretch")
    val durationMs = (spec.loopSeconds * 1000).toInt().coerceAtLeast(250)
    val t by transition.animateFloat(
        initialValue = 0f,
        targetValue = spec.poses.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "poseT",
    )
    val pose = interpolatedPose(spec.poses, t)
    Canvas(modifier = modifier) {
        drawFigure(pose, color)
    }
}

private fun DrawScope.drawFigure(
    joints: Map<String, Pair<Float, Float>>,
    color: Color,
) {
    val w = size.width
    val h = size.height
    val strokeWidth = (size.minDimension * 0.018f).coerceAtLeast(3f)
    val headRadius = size.minDimension * 0.05f
    val absolute = joints.mapValues { (_, p) -> Offset(p.first * w, p.second * h) }

    for ((aName, bName) in SKELETON_BONES) {
        val a = absolute[aName] ?: continue
        val b = absolute[bName] ?: continue
        drawLine(
            color = color,
            start = a,
            end = b,
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
    absolute["head"]?.let { c ->
        drawCircle(color = color, radius = headRadius, center = c, style = Stroke(width = strokeWidth))
    }
}

@Composable
private fun NoAnimationPlaceholder(youtubeId: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.SelfImprovement,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Follow the description and timer below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (hasWatchableVideo(youtubeId)) {
            Spacer(Modifier.height(8.dp))
            WatchOnYouTubeChip(youtubeId = youtubeId)
        }
    }
}

@Composable
private fun WatchOnYouTubeChip(
    youtubeId: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        TextButton(
            onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    "https://www.youtube.com/watch?v=$youtubeId".toUri(),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            },
            modifier = Modifier.padding(horizontal = 4.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.PlayCircleOutline,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Watch demo",
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

private fun hasWatchableVideo(youtubeId: String): Boolean =
    youtubeId.isNotBlank() && !youtubeId.startsWith("PLACEHOLDER_")
