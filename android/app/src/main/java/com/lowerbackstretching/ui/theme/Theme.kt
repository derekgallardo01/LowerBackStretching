package com.lowerbackstretching.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Sage40,
    onPrimary = Cream,
    primaryContainer = Sage80,
    onPrimaryContainer = Charcoal,
    secondary = Sand40,
    background = Cream,
    onBackground = Charcoal,
    surface = Cream,
    onSurface = Charcoal,
)

private val DarkColors = darkColorScheme(
    primary = Sage80,
    onPrimary = Charcoal,
    primaryContainer = Sage40,
    onPrimaryContainer = Cream,
    secondary = Sand90,
    background = Charcoal,
    onBackground = Cream,
    surface = Charcoal,
    onSurface = Cream,
)

@Composable
fun AppTheme(
    dark: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (dark) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
