package com.greenopal.zargon.domain.graphics

import android.content.Context
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses sprites from bomb.sht ASCII format.
 *
 * Format:
 * - Line 1: width (integer)
 * - Line 2: height (integer)
 * - Line 3: name (quoted string)
 * - Next height lines: space-separated color indices (0-15)
 */
@Singleton
class SpriteParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val spriteCache = mutableMapOf<String, Sprite>()

    /**
     * Parse all sprites from bomb.sht file in assets
     */
    fun parseAllSprites(filename: String = "bomb.sht"): Map<String, Sprite> {
        if (spriteCache.isNotEmpty()) {
            return spriteCache
        }

        try {
            val inputStream = context.assets.open(filename)
            val reader = BufferedReader(InputStreamReader(inputStream))
            val sprites = mutableMapOf<String, Sprite>()

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val width = line?.trim()?.toIntOrNull() ?: continue

                val height = reader.readLine()?.trim()?.toIntOrNull() ?: continue

                val nameLine = reader.readLine()?.trim() ?: continue
                val name = nameLine.removeSurrounding("\"")

                // Read pixel data
                val pixels = mutableListOf<List<Color>>()
                for (y in 0 until height) {
                    val pixelLine = reader.readLine()?.trim() ?: break
                    val colorIndices = pixelLine.split(Regex("\\s+"))
                        .mapNotNull { it.toIntOrNull() }

                    val rowColors = colorIndices.map { index ->
                        EGAPalette.getColor(index)
                    }
                    pixels.add(rowColors)
                }

                if (pixels.size == height) {
                    val sprite = Sprite(name, width, height, pixels)
                    sprites[name] = sprite
                    spriteCache[name] = sprite
                }
            }

            reader.close()
            return sprites
        } catch (e: Exception) {
            // If file not found, return empty map
            return emptyMap()
        }
    }

    /**
     * Get a specific sprite by name
     */
    fun getSprite(name: String): Sprite? {
        if (spriteCache.isEmpty()) {
            parseAllSprites()
        }
        return spriteCache[name]
    }

    /**
     * Create a simple placeholder sprite for testing
     */
    fun createPlaceholderSprite(name: String, width: Int = 30, height: Int = 30): Sprite {
        val pixels = List(height) { y ->
            List(width) { x ->
                // Create a simple pattern
                when {
                    x == 0 || x == width - 1 || y == 0 || y == height - 1 -> Color.White
                    (x + y) % 2 == 0 -> Color(0xFF00AA00)
                    else -> Color(0xFF00AAAA)
                }
            }
        }
        return Sprite(name, width, height, pixels)
    }

    /**
     * Create a bat sprite - brown creature with wing-like shape
     */
    fun createBatSprite(): Sprite {
        val width = 36
        val height = 36
        val brown = Color(0xFF8B4513)
        val darkBrown = Color(0xFF654321)
        val black = Color(0xFF000000)
        val red = Color(0xFFFF0000)

        val pixels = List(height) { y ->
            List(width) { x ->
                val centerX = width / 2
                val centerY = height / 2
                val dx = x - centerX
                val dy = y - centerY

                when {
                    // Eyes (red dots)
                    (dx == -4 || dx == 4) && dy == -6 -> red
                    // Body (oval shape)
                    (dx * dx / 64 + dy * dy / 100) <= 1 -> brown
                    // Wings (triangular shapes on sides)
                    (x < centerX - 6 && y > centerY - 8 && y < centerY + 8 && (x - (centerX - 18)) > (dy.absoluteValue() - 8)) -> darkBrown
                    (x > centerX + 6 && y > centerY - 8 && y < centerY + 8 && ((centerX + 18) - x) > (dy.absoluteValue() - 8)) -> darkBrown
                    // Outline
                    (dx * dx / 64 + dy * dy / 100) <= 1.2 && (dx * dx / 64 + dy * dy / 100) > 1 -> black
                    else -> Color.Transparent
                }
            }
        }
        return Sprite("bat", width, height, pixels)
    }

    /**
     * Create a babble sprite - blue blob creature
     */
    fun createBabbleSprite(): Sprite {
        val width = 36
        val height = 36
        val blue = Color(0xFF4444FF)
        val lightBlue = Color(0xFF6666FF)
        val darkBlue = Color(0xFF0000AA)
        val white = Color(0xFFFFFFFF)

        val pixels = List(height) { y ->
            List(width) { x ->
                val centerX = width / 2
                val centerY = height / 2 + 2
                val dx = x - centerX
                val dy = y - centerY

                when {
                    // Eyes (white circles)
                    (dx == -5 || dx == 5) && (dy == -4 || dy == -3) -> white
                    (dx == -4 || dx == 6) && dy == -3 -> white
                    // Pupils
                    (dx == -5 || dx == 5) && dy == -3 -> darkBlue
                    // Body (blob shape - wider at bottom)
                    (dx * dx / 100 + (dy + 4) * (dy + 4) / 64) <= 1 -> blue
                    // Highlight
                    (dx + 6) * (dx + 6) + (dy - 8) * (dy - 8) <= 20 -> lightBlue
                    // Outline
                    (dx * dx / 100 + (dy + 4) * (dy + 4) / 64) <= 1.15 && (dx * dx / 100 + (dy + 4) * (dy + 4) / 64) > 1 -> darkBlue
                    else -> Color.Transparent
                }
            }
        }
        return Sprite("babble", width, height, pixels)
    }

    /**
     * Create a spook sprite - ghostly white/gray creature
     */
    fun createSpookSprite(): Sprite {
        val width = 36
        val height = 36
        val white = Color(0xFFEEEEEE)
        val gray = Color(0xFFAAAAAA)
        val darkGray = Color(0xFF666666)
        val black = Color(0xFF000000)

        val pixels = List(height) { y ->
            List(width) { x ->
                val centerX = width / 2
                val centerY = height / 2
                val dx = x - centerX
                val dy = y - centerY

                when {
                    // Eyes (black holes)
                    ((dx == -5 || dx == -4 || dx == 5 || dx == 4) && (dy == -4 || dy == -3)) -> black
                    // Mouth (wavy)
                    (dy == 2 && dx in -6..6 && (dx + 6) % 3 == 0) -> black
                    // Body (sheet-like with wavy bottom)
                    (dx * dx / 100 + (dy - 2) * (dy - 2) / 121) <= 1 && dy < 8 -> white
                    // Wavy bottom edge
                    (dy in 8..10 && ((dx + 10) % 4 < 2)) -> white
                    // Shadow/outline
                    (dx * dx / 100 + (dy - 2) * (dy - 2) / 121) <= 1.1 && (dx * dx / 100 + (dy - 2) * (dy - 2) / 121) > 1 && dy < 8 -> gray
                    else -> Color.Transparent
                }
            }
        }
        return Sprite("spook", width, height, pixels)
    }

    /**
     * Create a slime sprite - green gooey creature
     */
    fun createSlimeSprite(): Sprite {
        val width = 36
        val height = 36
        val green = Color(0xFF00FF00)
        val darkGreen = Color(0xFF00AA00)
        val lightGreen = Color(0xFF88FF88)
        val black = Color(0xFF000000)

        val pixels = List(height) { y ->
            List(width) { x ->
                val centerX = width / 2
                val centerY = height / 2 + 4
                val dx = x - centerX
                val dy = y - centerY

                when {
                    // Eyes (small black dots)
                    ((dx == -4 || dx == 4) && (dy == -6 || dy == -5)) -> black
                    // Body (puddle shape - very wide at bottom)
                    (dx * dx / 144 + (dy + 2) * (dy + 2) / 64) <= 1 -> green
                    // Highlight (shiny spot)
                    ((dx + 5) * (dx + 5) + (dy - 6) * (dy - 6)) <= 15 -> lightGreen
                    // Drips
                    ((dx == -8 || dx == 0 || dx == 8) && dy in 10..12) -> green
                    // Outline
                    (dx * dx / 144 + (dy + 2) * (dy + 2) / 64) <= 1.08 && (dx * dx / 144 + (dy + 2) * (dy + 2) / 64) > 1 -> darkGreen
                    else -> Color.Transparent
                }
            }
        }
        return Sprite("slime", width, height, pixels)
    }

    private fun Int.absoluteValue(): Int = if (this < 0) -this else this

    /**
     * Clear the cache
     */
    fun clearCache() {
        spriteCache.clear()
    }
}
