package com.greenopal.zargon.domain.map

import androidx.compose.ui.graphics.Color

/**
 * Tile types from QBASIC crossroad procedure (ZARGON.BAS:914-959)
 */
enum class TileType(
    val code: String,
    val displayColor: Color,
    val isWalkable: Boolean,
    val encounterRate: Float = 0f // 0-1, chance of encounter per step
) {
    // Trees
    TREE("T", Color(0xFF00AA00), isWalkable = false),
    TREE2("t", Color(0xFF00AA00), isWalkable = false),

    // Rocks
    ROCK("R", Color(0xFF555555), isWalkable = false),
    ROCK2("r", Color(0xFF555555), isWalkable = false),

    // Water
    WATER("w", Color(0xFF0000AA), isWalkable = false),
    SHALLOW_WATER("4", Color(0xFF5555FF), isWalkable = true, encounterRate = 0.05f),

    // Ground
    GRASS("1", Color(0xFF55FF55), isWalkable = true, encounterRate = 0.1f),
    SAND("2", Color(0xFFFFFF55), isWalkable = true, encounterRate = 0.08f),
    FLOOR("0", Color(0xFFAAAAAA), isWalkable = true, encounterRate = 0.05f),
    FLOOR_DECORATED("D", Color(0xFFAAAAAA), isWalkable = true, encounterRate = 0.05f),

    // Special
    GRAVE("G", Color(0xFF555555), isWalkable = false),
    HUT("h", Color(0xFFAA5500), isWalkable = true, encounterRate = 0f),  // Brown - NPCs
    WEAPON_SHOP("W", Color(0xFF8B4513), isWalkable = true, encounterRate = 0f),  // Saddle Brown - Shop
    HEALER("H", Color(0xFFFF69B4), isWalkable = true, encounterRate = 0f),  // Hot Pink - Healer
    CASTLE("C", Color(0xFF4B0082), isWalkable = true, encounterRate = 0f);  // Indigo - Castle

    companion object {
        fun fromCode(code: String): TileType {
            // Handle "a" to "4" conversion from QBASIC
            val normalizedCode = if (code == "a") "4" else code
            return values().find { it.code == normalizedCode } ?: GRASS
        }
    }
}

/**
 * Represents a parsed map
 */
data class GameMap(
    val tiles: List<List<TileType>>, // [y][x] - 10 rows x 20 columns
    val hutPosition: Pair<Int, Int>? = null, // (x, y)
    val spawnPosition: Pair<Int, Int> = Pair(10, 5) // (x, y)
) {
    val width: Int = 20
    val height: Int = 10

    /**
     * Get tile at position
     */
    fun getTile(x: Int, y: Int): TileType? {
        return if (y in tiles.indices && x in tiles[y].indices) {
            tiles[y][x]
        } else {
            null
        }
    }

    /**
     * Check if position is walkable
     */
    fun isWalkable(x: Int, y: Int): Boolean {
        val tile = getTile(x, y) ?: return false
        return tile.isWalkable
    }

    /**
     * Get encounter rate at position
     */
    fun getEncounterRate(x: Int, y: Int): Float {
        val tile = getTile(x, y) ?: return 0f
        return tile.encounterRate
    }
}
