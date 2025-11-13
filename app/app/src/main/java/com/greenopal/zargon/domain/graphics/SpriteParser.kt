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
     * Clear the cache
     */
    fun clearCache() {
        spriteCache.clear()
    }
}
