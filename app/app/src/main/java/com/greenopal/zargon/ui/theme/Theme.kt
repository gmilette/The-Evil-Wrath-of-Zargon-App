package com.greenopal.zargon.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// EGA-inspired retro color palette
private val RetroGreen = Color(0xFF00AA00)
private val RetroBlue = Color(0xFF0000AA)
private val RetroCyan = Color(0xFF00AAAA)
private val RetroYellow = Color(0xFFFFFF55)
private val RetroRed = Color(0xFFAA0000)
private val RetroBrown = Color(0xFFAA5500)
private val RetroGray = Color(0xFFAAAAAA)
private val RetroDarkGray = Color(0xFF555555)

private val DarkColorScheme = darkColorScheme(
    primary = RetroGreen,
    secondary = RetroCyan,
    tertiary = RetroYellow,
    background = Color.Black,
    surface = RetroDarkGray,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = RetroGray,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = RetroBlue,
    secondary = RetroCyan,
    tertiary = RetroBrown,
    background = RetroGray,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
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
