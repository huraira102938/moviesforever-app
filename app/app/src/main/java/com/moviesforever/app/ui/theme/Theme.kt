package com.moviesforever.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = Black,
    primaryContainer = GoldDeep,
    onPrimaryContainer = GoldLight,
    secondary = GoldLight,
    onSecondary = Black,
    secondaryContainer = GoldDark,
    onSecondaryContainer = TextPrimary,
    background = Black,
    onBackground = TextPrimary,
    surface = BlackSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurface,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = DarkSurface,
    surfaceContainerHigh = DarkElevated,
    error = Error,
    onError = Color.White,
    outline = TextMuted,
    outlineVariant = DarkElevated
)

private val Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(24.dp)
)

@Composable
fun MoviesForeverTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}
