package com.greenopal.zargon.domain.map

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parses map files from .lvl format
 * Based on crossroad procedure (ZARGON.BAS:890)
 */
@Singleton
class MapParser @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mapCache = mutableMapOf<String, GameMap>()

    /**
     * Parse a map file
     * @param worldX Map X coordinate (1-4)
     * @param worldY Map Y coordinate (1-4)
     * @return Parsed GameMap
     */
    fun parseMap(worldX: Int, worldY: Int): GameMap {
        val cacheKey = "map$worldX$worldY"

        // Check cache
        mapCache[cacheKey]?.let { return it }

        val filename = "map$worldX$worldY.lvl"

        try {
            val inputStream = context.assets.open(filename)
            val reader = BufferedReader(InputStreamReader(inputStream))

            // Read 200 tile codes (20 x 10 grid)
            val tiles = mutableListOf<List<TileType>>()
            var tileIndex = 0

            for (y in 0 until 10) {
                val row = mutableListOf<TileType>()
                for (x in 0 until 20) {
                    val tileLine = reader.readLine()?.trim()?.removeSurrounding("\"") ?: "1"
                    val tileType = TileType.fromCode(tileLine)
                    row.add(tileType)
                    tileIndex++
                }
                tiles.add(row)
            }

            // Read hut position (optional, may be 0)
            val hutX = reader.readLine()?.trim()?.toIntOrNull() ?: 0
            val hutY = reader.readLine()?.trim()?.toIntOrNull() ?: 0

            // Read spawn position
            val spawnX = reader.readLine()?.trim()?.toIntOrNull() ?: 10
            val spawnY = reader.readLine()?.trim()?.toIntOrNull() ?: 5

            reader.close()

            val hutPosition = if (hutX > 0 && hutY > 0) Pair(hutX, hutY) else null

            val map = GameMap(
                tiles = tiles,
                hutPosition = hutPosition,
                spawnPosition = Pair(spawnX, spawnY)
            )

            // Debug logging for map24 (healer map) to verify tile parsing
            if (worldX == 2 && worldY == 4) {
                android.util.Log.d("MapParser", "=== Map 24 (Healer Area) Debug ===")
                android.util.Log.d("MapParser", "Healer should be at (10, 7)")
                for (y in 6..8) {
                    for (x in 9..11) {
                        val tile = tiles[y][x]
                        android.util.Log.d("MapParser", "Tile at ($x, $y): ${tile.name}, code=${tile.code}, walkable=${tile.isWalkable}")
                    }
                }
            }

            // Cache the map
            mapCache[cacheKey] = map

            return map
        } catch (e: Exception) {
            // Return default grass map if file not found
            return createDefaultMap()
        }
    }

    /**
     * Create a default map (all grass) for testing
     */
    private fun createDefaultMap(): GameMap {
        val tiles = List(10) { List(20) { TileType.GRASS } }
        return GameMap(
            tiles = tiles,
            spawnPosition = Pair(10, 5)
        )
    }

    /**
     * Clear the cache
     */
    fun clearCache() {
        mapCache.clear()
    }
}
