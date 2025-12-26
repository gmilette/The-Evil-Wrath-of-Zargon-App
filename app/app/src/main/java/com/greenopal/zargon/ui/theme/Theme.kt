package com.greenopal.zargon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Gold = Color(0xFFD4AF37)
private val DarkStone = Color(0xFF3A3A3A)
private val MidStone = Color(0xFF5B5B5B)
private val Parchment = Color(0xFFD8C8A0)
private val EmberOrange = Color(0xFFFF9A3C)

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

private val LightColorScheme = lightColorScheme(
    primary = Gold,
    secondary = Parchment,
    tertiary = EmberOrange,
    background = Parchment,
    surface = Color.White,
    error = EmberOrange,
    onPrimary = DarkStone,
    onSecondary = DarkStone,
    onTertiary = DarkStone,
    onBackground = DarkStone,
    onSurface = DarkStone,
)

@Composable
fun ZargonTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
