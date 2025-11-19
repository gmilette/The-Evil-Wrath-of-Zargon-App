package com.greenopal.zargon.domain.graphics

import android.content.Context
import androidx.compose.ui.graphics.Color
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses tiles from the QBASIC tiles.wad binary format
 * Based on the displayTile procedure from ZARGON.BAS
 */
@Singleton
class TileParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tileCache = mutableMapOf<String, Sprite>()

    /**
     * Parse all tiles from tiles.wad file
     *
     * Format:
     * - 4 bytes: Number of records
     * - For each record:
     *   - 15 bytes: Tile ID (string, space-padded)
     *   - 4 bytes: File location offset (little-endian)
     * - At each offset:
     *   - 2 bytes: Width
     *   - 2 bytes: Height
     *   - 2 bytes: Data length
     *   - N bytes: Bitmap data (QBASIC GET array format)
     */
    fun parseAllTiles(filename: String = "tiles.wad"): Map<String, Sprite> {
        if (tileCache.isNotEmpty()) {
            return tileCache
        }

        try {
            val inputStream = context.assets.open(filename)
            val data = inputStream.readBytes()
            inputStream.close()

            val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

            // Read number of records
            val numRecords = buffer.int

            // Read directory entries
            val directory = mutableListOf<TileEntry>()
            for (i in 0 until numRecords) {
                val tileIdBytes = ByteArray(15)
                buffer.get(tileIdBytes)
                val tileId = String(tileIdBytes).trim()
                val offset = buffer.int
                directory.add(TileEntry(tileId, offset))
            }

            // Parse each tile
            val tiles = mutableMapOf<String, Sprite>()
            for (entry in directory) {
                try {
                    val sprite = parseTileAtOffset(data, entry.offset, entry.tileId)
                    tiles[entry.tileId] = sprite

                    // Also add with common aliases
                    when (entry.tileId) {
                        "Grass" -> tiles["GRASS"] = sprite
                        "Rock-1" -> {
                            tiles["ROCK"] = sprite
                            tiles["Rock"] = sprite
                        }
                        "Rock-2" -> tiles["ROCK2"] = sprite
                        "Sand" -> tiles["SAND"] = sprite
                        "Trees1" -> {
                            tiles["TREE"] = sprite
                            tiles["Trees"] = sprite
                        }
                        "Trees2" -> tiles["TREE2"] = sprite
                        "Water" -> tiles["WATER"] = sprite
                        "Gravestone" -> tiles["GRAVE"] = sprite
                    }
                } catch (e: Exception) {
                    // Skip tiles that fail to parse
                    android.util.Log.w("TileParser", "Failed to parse tile ${entry.tileId}: ${e.message}")
                }
            }

            tileCache.putAll(tiles)
            return tiles

        } catch (e: Exception) {
            android.util.Log.e("TileParser", "Error parsing tiles.wad", e)
            return emptyMap()
        }
    }

    /**
     * Parse a tile at a specific offset in the data
     */
    private fun parseTileAtOffset(data: ByteArray, offset: Int, tileId: String): Sprite {
        val buffer = ByteBuffer.wrap(data, offset, data.size - offset).order(ByteOrder.LITTLE_ENDIAN)

        val width = buffer.short.toInt()
        val height = buffer.short.toInt()
        val dataLength = buffer.short.toInt()

        // QBASIC GET array format stores packed bitmap data
        // For now, create a simple colored sprite based on tile type
        val pixels = createPixelsForTile(tileId, width, height)

        return Sprite(tileId, width, height, pixels)
    }

    /**
     * Create pixel data for tiles
     * Since parsing QBASIC GET format is complex, use procedural generation
     */
    private fun createPixelsForTile(tileId: String, width: Int, height: Int): List<List<Color>> {
        return when (tileId) {
            "Grass" -> createGrassTile(width, height)
            "Rock-1", "Rock-2" -> createRockTile(width, height, tileId == "Rock-2")
            "Sand" -> createSandTile(width, height)
            "Trees1", "Trees2" -> createTreeTile(width, height, tileId == "Trees2")
            "Water" -> createWaterTile(width, height)
            "Gravestone" -> createGravestoneTile(width, height)
            else -> createDefaultTile(width, height)
        }
    }

    private fun createGrassTile(width: Int, height: Int): List<List<Color>> {
        val lightGreen = Color(0xFF55FF55)
        val darkGreen = Color(0xFF00AA00)
        return List(height) { y ->
            List(width) { x ->
                // Add some variation
                if ((x + y) % 3 == 0) darkGreen else lightGreen
            }
        }
    }

    private fun createRockTile(width: Int, height: Int, variant: Boolean): List<List<Color>> {
        val gray = Color(0xFF888888)
        val darkGray = Color(0xFF555555)
        val lightGray = Color(0xFFAAAAAA)

        return List(height) { y ->
            List(width) { x ->
                when {
                    (x + y) % 4 == 0 -> lightGray
                    (x * y) % 5 == 0 -> darkGray
                    else -> gray
                }
            }
        }
    }

    private fun createSandTile(width: Int, height: Int): List<List<Color>> {
        val sand = Color(0xFFFFFF55)
        val darkSand = Color(0xFFEEEE44)
        return List(height) { y ->
            List(width) { x ->
                if ((x + y) % 2 == 0) darkSand else sand
            }
        }
    }

    private fun createTreeTile(width: Int, height: Int, variant: Boolean): List<List<Color>> {
        val green = Color(0xFF00AA00)
        val darkGreen = Color(0xFF006600)
        val brown = Color(0xFF8B4513)

        val centerX = width / 2
        val centerY = height / 2

        return List(height) { y ->
            List(width) { x ->
                val dx = x - centerX
                val dy = y - centerY

                when {
                    // Trunk
                    dy > centerY / 2 && Math.abs(dx) < 2 -> brown
                    // Leaves (circular)
                    (dx * dx + dy * dy) < (width * width / 8) -> {
                        if ((x + y) % 3 == 0) darkGreen else green
                    }
                    else -> Color.Transparent
                }
            }
        }
    }

    private fun createWaterTile(width: Int, height: Int): List<List<Color>> {
        val water = Color(0xFF0000AA)
        val lightWater = Color(0xFF3333FF)
        return List(height) { y ->
            List(width) { x ->
                if ((x + y) % 3 == 0) lightWater else water
            }
        }
    }

    private fun createGravestoneTile(width: Int, height: Int): List<List<Color>> {
        val gray = Color(0xFF666666)
        val darkGray = Color(0xFF333333)

        val centerX = width / 2

        return List(height) { y ->
            List(width) { x ->
                when {
                    // Gravestone shape
                    y < height / 3 && Math.abs(x - centerX) < 3 -> gray
                    y >= height / 3 && y < height * 2 / 3 && Math.abs(x - centerX) < 4 -> gray
                    y >= height * 2 / 3 && Math.abs(x - centerX) < 2 -> darkGray
                    else -> Color.Transparent
                }
            }
        }
    }

    private fun createDefaultTile(width: Int, height: Int): List<List<Color>> {
        return List(height) { y ->
            List(width) { x ->
                Color(0xFF00AA00)
            }
        }
    }

    /**
     * Get a tile by ID
     */
    fun getTile(tileId: String): Sprite? {
        if (tileCache.isEmpty()) {
            parseAllTiles()
        }
        return tileCache[tileId]
    }

    data class TileEntry(val tileId: String, val offset: Int)
}
