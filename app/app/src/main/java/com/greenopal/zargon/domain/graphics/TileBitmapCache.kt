package com.greenopal.zargon.domain.graphics

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optimized bitmap cache for tile rendering
 * Pre-renders textured tiles into Android Bitmaps for fast display
 */
@Singleton
class TileBitmapCache @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tileParser: TileParser
) {
    private val bitmapCache = mutableMapOf<String, Bitmap>()
    private val defaultTileSize = 32  // pixels

    /**
     * Get or create a bitmap for a tile type
     */
    fun getBitmap(tileId: String, size: Int = defaultTileSize): Bitmap? {
        val cacheKey = "$tileId-$size"

        // Return cached bitmap if available
        if (bitmapCache.containsKey(cacheKey)) {
            return bitmapCache[cacheKey]
        }

        // Get tile sprite from parser (with textures)
        val sprite = tileParser.getTile(tileId) ?: return null

        // Create bitmap from sprite pixels
        val bitmap = createBitmapFromSprite(sprite, size)
        bitmapCache[cacheKey] = bitmap

        return bitmap
    }

    /**
     * Create an Android Bitmap from a Sprite's pixel data
     */
    private fun createBitmapFromSprite(sprite: Sprite, targetSize: Int): Bitmap {
        // Create bitmap with sprite dimensions
        val bitmap = Bitmap.createBitmap(sprite.width, sprite.height, Bitmap.Config.ARGB_8888)

        // Set pixels from sprite data
        for (y in sprite.pixels.indices) {
            for (x in sprite.pixels[y].indices) {
                val color = sprite.pixels[y][x]
                bitmap.setPixel(x, y, color.toArgb())
            }
        }

        // Scale to target size if needed
        return if (targetSize != sprite.width || targetSize != sprite.height) {
            Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, false)
        } else {
            bitmap
        }
    }

    /**
     * Pre-load common tiles for better performance
     */
    fun preloadCommonTiles(size: Int = defaultTileSize) {
        val commonTiles = listOf(
            "Grass", "GRASS",
            "Sand", "SAND",
            "Rock-1", "ROCK",
            "Rock-2", "ROCK2",
            "Trees1", "TREE",
            "Trees2", "TREE2",
            "Water", "WATER"
        )

        commonTiles.forEach { tileId ->
            getBitmap(tileId, size)
        }
    }

    /**
     * Clear the cache to free memory
     */
    fun clearCache() {
        bitmapCache.values.forEach { it.recycle() }
        bitmapCache.clear()
    }
}
