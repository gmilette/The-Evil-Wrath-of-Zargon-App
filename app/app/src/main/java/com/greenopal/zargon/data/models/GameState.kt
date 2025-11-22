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
    // Starting on Map 24 between the Weapon Shop and Healer
    val worldX: Int = 2,        // mapx - which world (1-4)
    val worldY: Int = 4,        // mapy - which quadrant (1-4)
    val characterX: Int = 6,    // cx - position within map (between shop at x=2 and healer at x=10)
    val characterY: Int = 7,    // cy - position within map (on the shop/healer row)

    // Inventory (QBASIC: items() array, max 10)
    val inventory: List<Item> = emptyList(),

    // Story progression (QBASIC: storystatus, ranges 1.0 to 5.5)
    val storyStatus: Float = 1.0f,

    // Special flags
    val inShip: Boolean = false,  // inship in QBASIC

    // Progression (QBASIC: nextlev)
    val nextLevelXP: Int = 30,  // XP needed for next level

    // Game meta
    val saveSlot: Int = 1,
    val playtime: Long = 0L  // milliseconds
) {
    /**
     * Add item to inventory (max 10 items)
     * Prevents duplicate items - only one of each item allowed
     */
    fun addItem(item: Item): GameState {
        // Check if item already exists (case-insensitive)
        val alreadyHasItem = inventory.any { it.name.equals(item.name, ignoreCase = true) }

        return if (alreadyHasItem) {
            // Already have this item, don't add duplicate
            this
        } else if (inventory.size < 10) {
            // Add item to inventory
            copy(inventory = inventory + item)
        } else {
            // Inventory full
            this
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
