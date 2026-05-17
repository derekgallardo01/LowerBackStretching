package com.lowerbackstretching.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Renders 1–3 filled dots for a difficulty string. Unknown values render
 * one dot. Accessibility label still reads the difficulty name so screen
 * readers don't lose the information.
 */
@Composable
fun DifficultyDots(difficulty: String, modifier: Modifier = Modifier) {
    val filled = filledCount(difficulty)
    val label = difficulty.replaceFirstChar(Char::titlecase)
    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier.semantics { contentDescription = label },
    ) {
        repeat(3) { i ->
            val isFilled = i < filled
            val color = if (isFilled) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
            Row(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color)
                    .then(
                        if (!isFilled) Modifier.border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                            shape = CircleShape,
                        ) else Modifier
                    ),
                content = {},
            )
        }
    }
}

internal fun filledCount(difficulty: String): Int = when (difficulty.lowercase()) {
    "easy" -> 1
    "medium" -> 2
    "hard" -> 3
    else -> 1
}
