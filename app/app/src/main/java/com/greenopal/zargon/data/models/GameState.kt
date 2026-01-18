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

    // Track all items ever discovered (persists even after items are given away/consumed)
    val discoveredItems: Set<String> = emptySet(),

    // Story progression (QBASIC: storystatus, ranges 1.0 to 5.5)
    val storyStatus: Float = 1.0f,

    // Special flags
    val inShip: Boolean = false,  // inship in QBASIC

    // Progression (QBASIC: nextlev)
    val nextLevelXP: Int = 30,  // XP needed for next level

    // Game meta
    val saveSlot: Int = 1,
    val playtime: Long = 0L,  // milliseconds

    // Challenge configuration (null for legacy/normal games)
    val challengeConfig: ChallengeConfig? = null,

    // Timer tracking (for timed challenges)
    val challengeStartTime: Long? = null,  // System.currentTimeMillis() at game start
    val totalPauseTime: Long = 0L,  // Accumulated pause time to subtract

    // Statistics tracking for challenge completion
    val monstersDefeated: Int = 0,
    val deathCount: Int = 0
) {
    /**
     * Add item to inventory (max 10 items)
     * Prevents duplicate items - only one of each item allowed
     * Also tracks item in discoveredItems for quest progress
     */
    fun addItem(item: Item): GameState {
        // Check if item already exists (case-insensitive)
        val alreadyHasItem = inventory.any { it.name.equals(item.name, ignoreCase = true) }

        // Track item as discovered (lowercase for consistency)
        val updatedDiscovered = discoveredItems + item.name.lowercase()

        return if (alreadyHasItem) {
            // Already have this item, don't add duplicate
            // But still track as discovered
            copy(discoveredItems = updatedDiscovered)
        } else if (inventory.size < 10) {
            // Add item to inventory and track as discovered
            copy(
                inventory = inventory + item,
                discoveredItems = updatedDiscovered
            )
        } else {
            // Inventory full - still track as discovered
            copy(discoveredItems = updatedDiscovered)
        }
    }

    /**
     * Remove item from inventory
     */
    fun removeItem(item: Item): GameState {
        return copy(inventory = inventory - item)
    }

    /**
     * Check if player has a specific item in current inventory
     */
    fun hasItem(itemName: String): Boolean {
        return inventory.any { it.name.equals(itemName, ignoreCase = true) }
    }

    /**
     * Check if player has ever discovered a specific item (for quest progress)
     * Items remain discovered even after being given away or consumed
     */
    fun hasDiscovered(itemName: String): Boolean {
        return discoveredItems.contains(itemName.lowercase())
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
