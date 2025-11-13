package com.greenopal.zargon.data.models

import kotlinx.serialization.Serializable

/**
 * Overall game state.
 * Maps to QBASIC COMMON SHARED variables for position, story, etc.
 */
@Serializable
data class GameState(
    val character: CharacterStats = CharacterStats(),

    // Map position (QBASIC: mapx, mapy, cx, cy)
    val worldX: Int = 1,        // mapx - which world (1-4)
    val worldY: Int = 1,        // mapy - which quadrant (1-4)
    val characterX: Int = 10,   // cx - position within map
    val characterY: Int = 5,    // cy - position within map

    // Inventory (QBASIC: items() array, max 10)
    val inventory: List<Item> = emptyList(),

    // Story progression (QBASIC: storystatus, ranges 1.0 to 5.5)
    val storyStatus: Float = 1.0f,

    // Special flags
    val inShip: Boolean = false,  // inship in QBASIC

    // Game meta
    val saveSlot: Int = 1,
    val playtime: Long = 0L  // milliseconds
) {
    /**
     * Add item to inventory (max 10 items)
     */
    fun addItem(item: Item): GameState {
        return if (inventory.size < 10) {
            copy(inventory = inventory + item)
        } else {
            this  // Inventory full
        }
    }

    /**
     * Remove item from inventory
     */
    fun removeItem(item: Item): GameState {
        return copy(inventory = inventory - item)
    }

    /**
     * Check if player has a specific item
     */
    fun hasItem(itemName: String): Boolean {
        return inventory.any { it.name.equals(itemName, ignoreCase = true) }
    }

    /**
     * Update character position
     */
    fun moveTo(x: Int, y: Int): GameState {
        return copy(characterX = x, characterY = y)
    }

    /**
     * Change map/world
     */
    fun changeMap(worldX: Int, worldY: Int, spawnX: Int = characterX, spawnY: Int = characterY): GameState {
        return copy(
            worldX = worldX,
            worldY = worldY,
            characterX = spawnX,
            characterY = spawnY
        )
    }

    /**
     * Update character stats
     */
    fun updateCharacter(newStats: CharacterStats): GameState {
        return copy(character = newStats)
    }

    /**
     * Progress story
     */
    fun updateStory(newStatus: Float): GameState {
        return copy(storyStatus = newStatus)
    }
}
