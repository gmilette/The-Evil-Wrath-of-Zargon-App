package com.greenopal.zargon.data.models

/**
 * Items that can be found on maps by searching
 * Based on QBASIC found() SUB (ZARGON.BAS:1454-1485)
 */
data class MapItem(
    val worldX: Int,      // Map world coordinate (1-4)
    val worldY: Int,      // Map quadrant coordinate (1-4)
    val spotX: Int,       // X coordinate on map (cx in QBASIC)
    val spotY: Int,       // Y coordinate on map (cy in QBASIC)
    val item: Item        // The item to be found
)

/**
 * Repository of all findable items in the game
 * Based on QBASIC found() SUB which checks mapx$ + mapy$ to determine item
 */
object MapItems {
    private val allItems = listOf(
        // Item 1: Dynamite - Map 44 (World 4, Quadrant 4)
        // QBASIC Line 1463-1465
        // Location: map44.lvl line 135 = tile 134 = position (14, 6)
        MapItem(
            worldX = 4,
            worldY = 4,
            spotX = 14,
            spotY = 6,
            item = Item(
                name = "dynamite",
                description = "Used to blast through rocks",
                type = ItemType.KEY_ITEM
            )
        ),

        // Item 2: Dead Wood - Map 14 (World 1, Quadrant 4)
        // QBASIC Line 1466-1468
        MapItem(
            worldX = 1,
            worldY = 4,
            spotX = 3,
            spotY = 8,
            item = Item(
                name = "dead wood",
                description = "Dried wood for ship construction",
                type = ItemType.KEY_ITEM
            )
        ),

        // Item 4: Rutter - Map 24 (World 2, Quadrant 4)
        // QBASIC Line 1469-1471
        MapItem(
            worldX = 2,
            worldY = 4,
            spotX = 9,
            spotY = 1,
            item = Item(
                name = "rutter",
                description = "Navigation tool for ship",
                type = ItemType.KEY_ITEM
            )
        ),

        // Item 5: Cloth - Map 13 (World 1, Quadrant 3)
        // QBASIC Line 1472-1474, coordinates from map13.lvl
        // Updated to (7, 6) - moved down one to walkable grass tile
        MapItem(
            worldX = 1,
            worldY = 3,
            spotX = 7,
            spotY = 6,
            item = Item(
                name = "cloth",
                description = "Sail material for ship",
                type = ItemType.KEY_ITEM
            )
        ),

        // Item 6: Wood - Map 22 (World 2, Quadrant 2)
        // QBASIC Line 1475-1477, coordinates from map22.lvl
        MapItem(
            worldX = 2,
            worldY = 2,
            spotX = 10,
            spotY = 4,
            item = Item(
                name = "wood",
                description = "Strong wood for ship hull",
                type = ItemType.KEY_ITEM
            )
        )
    )

    /**
     * Get item at specific location, if any
     */
    fun getItemAt(worldX: Int, worldY: Int, spotX: Int, spotY: Int): Item? {
        return allItems.find {
            it.worldX == worldX &&
            it.worldY == worldY &&
            it.spotX == spotX &&
            it.spotY == spotY
        }?.item
    }

    /**
     * Check if there's an item at this location
     */
    fun hasItemAt(worldX: Int, worldY: Int, spotX: Int, spotY: Int): Boolean {
        return getItemAt(worldX, worldY, spotX, spotY) != null
    }

    /**
     * Get marker positions for items on the given world that haven't been collected yet
     */
    fun getMarkersForWorld(worldX: Int, worldY: Int, inventory: List<Item>): List<Pair<Int, Int>> {
        val inventoryNames = inventory.map { it.name.lowercase() }.toSet()
        return allItems
            .filter { it.worldX == worldX && it.worldY == worldY }
            .filter { it.item.name.lowercase() !in inventoryNames }
            .map { Pair(it.spotX, it.spotY) }
    }
}
