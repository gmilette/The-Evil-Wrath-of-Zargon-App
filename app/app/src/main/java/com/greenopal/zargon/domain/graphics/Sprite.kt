package com.greenopal.zargon.domain.graphics

import androidx.compose.ui.graphics.Color

/**
 * Represents a parsed sprite from bomb.sht or other sources
 */
data class Sprite(
    val name: String,
    val width: Int,
    val height: Int,
    val pixels: List<List<Color>>  // [y][x] - row-major order
) {
    /**
     * Get pixel color at (x, y), or transparent if out of bounds
     */
    fun getPixel(x: Int, y: Int): Color {
        return if (y in pixels.indices && x in pixels[y].indices) {
            pixels[y][x]
        } else {
            Color.Transparent
        }
    }
}
