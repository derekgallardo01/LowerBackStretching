package com.lowerbackstretching.ui.components

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import kotlin.math.roundToInt

/** A big number with a small label underneath. Used on Home and Calendar. */
@Composable
fun Stat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.headlineMedium)
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}

/**
 * Same shape as [Stat] but the number tweens from its previous value
 * over 800ms whenever [value] changes — so a freshly-bumped streak or
 * session count *feels* like an increment instead of a jump.
 */
@Composable
fun AnimatedStat(value: Int, label: String) {
    val animated by animateFloatAsState(
        targetValue = value.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        label = "animatedStat",
    )
    Stat(value = animated.roundToInt().toString(), label = label)
}
