package com.greenopal.zargon.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Medieval theme colors - shared across the app
val Gold = Color(0xFFD4AF37)
val DarkStone = Color(0xFF3A3A3A)
val MidStone = Color(0xFF5B5B5B)
val Parchment = Color(0xFFD8C8A0)
val EmberOrange = Color(0xFFFF9A3C)

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    secondary = Parchment,
    tertiary = EmberOrange,
    background = DarkStone,
    surface = MidStone,
    error = EmberOrange,
    onPrimary = DarkStone,
    onSecondary = DarkStone,
    onTertiary = DarkStone,
    onBackground = Parchment,
    onSurface = Gold,
)

@Composable
fun ZargonTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
