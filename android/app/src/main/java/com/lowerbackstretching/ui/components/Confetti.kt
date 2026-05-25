package com.lowerbackstretching.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import com.lowerbackstretching.ui.theme.Cream
import com.lowerbackstretching.ui.theme.Sage80
import com.lowerbackstretching.ui.theme.Sand40
import kotlin.math.sin
import kotlin.random.Random

/**
 * Calm celebration confetti — sage/sand/cream particles drift down with
 * a gentle sine sway, fade out over ~2.5s, then the overlay stops drawing.
 * No looping, no neon colors, no sound. Triggered once via
 * `LaunchedEffect(Unit)` at the moment of celebration.
 */
@Composable
fun Confetti(
    modifier: Modifier = Modifier,
    durationMillis: Long = 2500L,
    particleCount: Int = 36,
) {
    val particles = remember {
        List(particleCount) { Particle.random() }
    }
    var elapsedMillis by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        val startNanos = withFrameNanos { it }
        var now = startNanos
        while (now - startNanos < durationMillis * 1_000_000L) {
            now = withFrameNanos { it }
            elapsedMillis = (now - startNanos) / 1_000_000L
        }
        elapsedMillis = durationMillis
    }

    if (elapsedMillis >= durationMillis) return

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            drawParticle(p, elapsedMillis, durationMillis, size)
        }
    }
}

private fun DrawScope.drawParticle(p: Particle, elapsedMillis: Long, durationMillis: Long, size: Size) {
    val t = elapsedMillis / 1_000f
    val progress = (elapsedMillis.toFloat() / durationMillis).coerceIn(0f, 1f)
    val fadeOutStart = 0.6f
    val alpha = when {
        progress < fadeOutStart -> p.alpha
        else -> p.alpha * ((1f - progress) / (1f - fadeOutStart)).coerceIn(0f, 1f)
    }

    val swayPixels = p.swayAmplitude * size.width
    val x = (p.startXFraction * size.width) + sin((t + p.phaseSeconds) * p.swayFreq) * swayPixels
    val y = (-p.startYOffset + p.fallSpeed * t) * size.height

    if (y > size.height + 40f) return

    rotate(degrees = p.rotationDegPerSec * t, pivot = Offset(x, y)) {
        drawRect(
            color = p.color.copy(alpha = alpha),
            topLeft = Offset(x - p.size / 2f, y - p.size / 2f),
            size = Size(p.size, p.size * p.aspect),
        )
    }
}

private data class Particle(
    val startXFraction: Float,
    val startYOffset: Float,
    val fallSpeed: Float,
    val swayFreq: Float,
    val swayAmplitude: Float,
    val phaseSeconds: Float,
    val rotationDegPerSec: Float,
    val size: Float,
    val aspect: Float,
    val color: Color,
    val alpha: Float,
) {
    companion object {
        private val palette = listOf(Sage80, Sand40, Cream)

        fun random(): Particle = Particle(
            startXFraction = Random.nextFloat(),
            startYOffset = Random.nextFloat() * 0.4f + 0.05f,
            fallSpeed = 0.18f + Random.nextFloat() * 0.18f,
            swayFreq = 0.6f + Random.nextFloat() * 1.4f,
            swayAmplitude = 0.04f + Random.nextFloat() * 0.06f,
            phaseSeconds = Random.nextFloat() * 6f,
            rotationDegPerSec = (Random.nextFloat() - 0.5f) * 180f,
            size = 8f + Random.nextFloat() * 10f,
            aspect = 0.4f + Random.nextFloat() * 0.6f,
            color = palette.random(),
            alpha = 0.5f + Random.nextFloat() * 0.3f,
        )
    }
}
