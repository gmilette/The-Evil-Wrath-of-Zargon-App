package com.greenopal.zargon.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val MedievalColorScheme = darkColorScheme(
    // ── Primary actions: gold-bordered buttons, selected states ──
    primary          = Gold,
    onPrimary        = StoneDark,
    primaryContainer = PanelMid,
    onPrimaryContainer = GoldBright,

    // ── Secondary: parchment-toned elements ──
    secondary          = Parchment,
    onSecondary        = StoneDark,
    secondaryContainer = PanelBg,
    onSecondaryContainer = ParchmentDim,

    // ── Tertiary: ember/leave/exit actions ──
    tertiary          = Ember,
    onTertiary        = Parchment,
    tertiaryContainer = EmberDark,
    onTertiaryContainer = EmberBright,

    // ── Background & surfaces ──
    background = StoneBg,
    onBackground = Parchment,

    surface        = PanelBg,
    onSurface      = Gold,
    surfaceVariant = PanelMid,
    onSurfaceVariant = ParchmentDim,

    // ── Error ──
    error   = HpRed,
    onError = Parchment,

    // ── Outlines ──
    outline        = GoldDark,
    outlineVariant = GoldDeep,
)

@Composable
fun ZargonTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MedievalColorScheme,
        typography  = Typography,
        content     = content,
    )
}
