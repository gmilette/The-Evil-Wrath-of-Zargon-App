package com.greenopal.zargon.domain.graphics

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.greenopal.zargon.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Optimized bitmap cache for tile rendering
 * Loads tiles from drawable resources and caches them for fast display
 */
@Singleton
class TileBitmapCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val bitmapCache = mutableMapOf<String, Bitmap>()
    private val defaultTileSize = 32  // pixels

    /**
     * Map tile IDs to drawable resource IDs
     */
    private val tileResourceMap = mapOf(
        // Primary tile IDs
        "Grass" to R.drawable.grass,
        "Sand" to R.drawable.sand,
        "Rock-1" to R.drawable.rock_1,
        "Rock-2" to R.drawable.rock_2,
        "Trees1" to R.drawable.trees1,
        "Trees2" to R.drawable.trees2,
        "Water" to R.drawable.water,
        "Gravestone" to R.drawable.gravestone,

        // Building/location sprites from bomb.sht
        "huts" to R.drawable.huts,
        "Huts" to R.drawable.huts,
        "cast" to R.drawable.cast,
        "Cast" to R.drawable.cast,
        "flor" to R.drawable.flor,
        "Flor" to R.drawable.flor,

        // Common aliases for backward compatibility
        "GRASS" to R.drawable.grass,
        "SAND" to R.drawable.sand,
        "ROCK" to R.drawable.rock_1,
        "Rock" to R.drawable.rock_1,
        "ROCK2" to R.drawable.rock_2,
        "TREE" to R.drawable.trees1,
        "Trees" to R.drawable.trees1,
        "TREE2" to R.drawable.trees2,
        "WATER" to R.drawable.water,
        "GRAVE" to R.drawable.gravestone,

        // Character sprites
        "dude_back1" to R.drawable.dude_back1,
        "dude_back2" to R.drawable.dude_back2,
        "dude_front1" to R.drawable.dude_front1,
        "dude_front2" to R.drawable.dude_front2,
        "dude_sidel" to R.drawable.dude_sidel,
        "dude_sider" to R.drawable.dude_sider,

        // UI icons
        "spell" to R.drawable.spell,
        "attack_icon" to R.drawable.attack_icon,
        "run_icon" to R.drawable.run_icon
    )

    /**
     * Get or create a bitmap for a tile type
     */
    fun getBitmap(tileId: String, size: Int = defaultTileSize): Bitmap? {
        val cacheKey = "$tileId-$size"

        // Return cached bitmap if available
        if (bitmapCache.containsKey(cacheKey)) {
            return bitmapCache[cacheKey]
        }

        // Get drawable resource ID for this tile
        val resourceId = tileResourceMap[tileId] ?: return null

        // Load bitmap from drawable resources
        val bitmap = loadBitmapFromResource(resourceId, size)
        if (bitmap != null) {
            bitmapCache[cacheKey] = bitmap
        }

        return bitmap
    }

    /**
     * Load a bitmap from drawable resources and scale to target size
     */
    private fun loadBitmapFromResource(resourceId: Int, targetSize: Int): Bitmap? {
        return try {
            // Load original bitmap
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val originalBitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

            // Scale to target size
            if (originalBitmap != null && (originalBitmap.width != targetSize || originalBitmap.height != targetSize)) {
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, targetSize, targetSize, false)
                if (scaledBitmap != originalBitmap) {
                    originalBitmap.recycle()
                }
                scaledBitmap
            } else {
                originalBitmap
            }
        } catch (e: Exception) {
            android.util.Log.e("TileBitmapCache", "Failed to load tile resource $resourceId", e)
            null
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

    /**
     * Create a Sprite from a drawable resource
     * Useful for loading character sprites as Sprite objects
     */
    fun createSpriteFromDrawable(tileId: String): Sprite? {
        val resourceId = tileResourceMap[tileId] ?: return null

        return try {
            // Load bitmap from resources
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options) ?: return null

            // Convert bitmap to pixel array
            val width = bitmap.width
            val height = bitmap.height
            val pixels = List(height) { y ->
                List(width) { x ->
                    val pixelColor = bitmap.getPixel(x, y)
                    Color(pixelColor)
                }
            }

            Sprite(tileId, width, height, pixels)
        } catch (e: Exception) {
            android.util.Log.e("TileBitmapCache", "Failed to create sprite from drawable $tileId", e)
            null
        }
    }
}
