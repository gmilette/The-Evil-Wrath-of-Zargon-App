package com.greenopal.zargon.domain.graphics

import androidx.compose.ui.graphics.Color

/**
 * EGA (Enhanced Graphics Adapter) 16-color palette
 * Maps QBASIC color indices (0-15) to Android Color values
 */
object EGAPalette {
    val colors = mapOf(
        0 to Color(0xFF000000),  // Black
        1 to Color(0xFF0000AA),  // Dark Blue
        2 to Color(0xFF00AA00),  // Dark Green
        3 to Color(0xFF00AAAA),  // Dark Cyan
        4 to Color(0xFFAA0000),  // Dark Red
        5 to Color(0xFFAA00AA),  // Dark Magenta
        6 to Color(0xFFAA5500),  // Brown
        7 to Color(0xFFAAAAAA),  // Light Gray
        8 to Color(0xFF555555),  // Dark Gray
        9 to Color(0xFF5555FF),  // Bright Blue
        10 to Color(0xFF55FF55), // Bright Green
        11 to Color(0xFF55FFFF), // Bright Cyan
        12 to Color(0xFFFF5555), // Bright Red
        13 to Color(0xFFFF55FF), // Bright Magenta
        14 to Color(0xFFFFFF55), // Yellow
        15 to Color(0xFFFFFFFF)  // White
    )

    /**
     * Get color by index, defaulting to black for invalid indices
     */
    fun getColor(index: Int): Color {
        return colors[index] ?: Color.Black
    }

    /**
     * Transparent color (used for value 0 in some contexts)
     */
    val transparent = Color.Transparent
}
