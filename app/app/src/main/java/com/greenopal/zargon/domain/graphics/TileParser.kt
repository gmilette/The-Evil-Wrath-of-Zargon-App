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

        // Read the QBASIC GET array format bitmap data
        val bitmapData = ByteArray(dataLength)
        buffer.get(bitmapData)

        // Decode the EGA planar bitmap
        val pixels = decodeEGABitmap(bitmapData, width, height)

        return Sprite(tileId, width, height, pixels)
    }

    /**
     * EGA 64-color palette mapping from ZARGON.BAS displayTile procedure (lines 1134-1137)
     * PALETTE 0, 0: PALETTE 1, 4: PALETTE 2, 48: PALETTE 3, 2
     * PALETTE 4, 6: PALETTE 5, 54: PALETTE 6, 10: PALETTE 7, 38
     * PALETTE 8, 46: PALETTE 9, 5: PALETTE 10, 25: PALETTE 11, 7
     * PALETTE 12, 57: PALETTE 13, 63: PALETTE 14, 9: PALETTE 15, 59
     */
    private val egaPaletteMap = intArrayOf(0, 4, 48, 2, 6, 54, 10, 38, 46, 5, 25, 7, 57, 63, 9, 59)

    /**
     * Convert EGA 64-color value to RGB
     * EGA 64-color format uses 6-bit color: bits 543210 = RGBRGB (2 bits per channel)
     */
    private fun ega64ToRgb(egaColor: Int): Color {
        // Extract RGB components (2 bits each, scaled to 0-3)
        val r = ((egaColor shr 4) and 2) or ((egaColor shr 0) and 1)
        val g = ((egaColor shr 3) and 2) or ((egaColor shr 1) and 1)
        val b = ((egaColor shr 2) and 2) or ((egaColor shr 2) and 1)

        // Scale from 0-3 to 0-255
        return Color(r * 85, g * 85, b * 85)
    }

    /**
     * Get color from palette index
     */
    private fun getPaletteColor(index: Int): Color {
        if (index < 0 || index >= egaPaletteMap.size) {
            return Color.Black
        }
        return ega64ToRgb(egaPaletteMap[index])
    }

    /**
     * Decode QBASIC GET array format bitmap for EGA Screen 9
     *
     * Format:
     * - First 2 integers (4 bytes): width, height (redundant, already known)
     * - Remaining data: 4-plane EGA bitmap data
     *
     * The bitmap is stored in planar format with 4 bit planes (16 colors).
     * Each scan line has all 4 planes stored sequentially.
     */
    private fun decodeEGABitmap(data: ByteArray, width: Int, height: Int): List<List<Color>> {
        // Parse as 16-bit little-endian integers
        val intArray = mutableListOf<Int>()
        var i = 0
        while (i < data.size - 1) {
            val low = data[i].toInt() and 0xFF
            val high = data[i + 1].toInt() and 0xFF
            intArray.add(low or (high shl 8))
            i += 2
        }

        // Skip header (first 2 integers are width and height)
        val bytesPerRow = (width + 7) / 8  // Round up to nearest byte

        // Create pixel array
        val pixels = List(height) { MutableList(width) { Color.Black } }

        var dataIdx = 2  // Skip width/height header

        // Decode each scan line
        for (y in 0 until height) {
            // Read 4 bit planes for this row
            val planeData = Array(4) { ByteArray(bytesPerRow) }

            for (plane in 0 until 4) {
                // Read bytes for this plane's scan line
                for (byteIdx in 0 until bytesPerRow) {
                    if (dataIdx < intArray.size) {
                        planeData[plane][byteIdx] = if (byteIdx % 2 == 0) {
                            // Low byte of integer
                            (intArray[dataIdx] and 0xFF).toByte()
                        } else {
                            // High byte of integer
                            ((intArray[dataIdx] shr 8) and 0xFF).toByte()
                        }

                        // Advance to next integer after reading high byte
                        if (byteIdx % 2 == 1) {
                            dataIdx++
                        }
                    }
                }

                // If odd number of bytes, advance to next integer
                if (bytesPerRow % 2 == 1) {
                    dataIdx++
                }
            }

            // Convert planar data to pixels
            for (x in 0 until width) {
                val byteIdx = x / 8
                val bitIdx = 7 - (x % 8)

                // Combine bits from all 4 planes to get color index
                var colorIndex = 0
                for (plane in 0 until 4) {
                    if (byteIdx < planeData[plane].size) {
                        val bit = (planeData[plane][byteIdx].toInt() shr bitIdx) and 1
                        colorIndex = colorIndex or (bit shl plane)
                    }
                }

                pixels[y][x] = getPaletteColor(colorIndex)
            }
        }

        return pixels
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
