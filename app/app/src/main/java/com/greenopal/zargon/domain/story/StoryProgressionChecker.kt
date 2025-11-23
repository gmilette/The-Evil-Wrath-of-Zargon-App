package com.greenopal.zargon.domain.story

import com.greenopal.zargon.data.models.GameState

/**
 * Automatic story progression checker
 * Based on storycheck function (ZARGON.BAS:3194-3201)
 *
 * This runs after various events to automatically advance the story
 * based on items in the player's inventory
 */
object StoryProgressionChecker {

    /**
     * Check and update story status based on inventory
     * Based on QBASIC lines 3194-3201, but made inventory-dependent
     * rather than strictly sequential.
     *
     * Story progression is now based on what items you have, not the order you got them.
     */
    fun checkAndAdvanceStory(gameState: GameState): GameState {
        val status = gameState.storyStatus
        var newStatus = status

        // Check inventory items
        val hasBoatPlans = gameState.hasItem("boat plans")
        val hasRutter = gameState.hasItem("rutter")
        val hasCloth = gameState.hasItem("cloth")
        val hasWood = gameState.hasItem("wood")
        val hasAllBoatMaterials = hasRutter && hasCloth && hasWood
        val hasTrappedSoul = gameState.hasItem("trapped soul")
        val hasShip = gameState.hasItem("ship")

        // Determine appropriate story level based on inventory
        // Work backwards from highest to lowest to find the right level

        // If you have ship, you should be at 5.5 (unless you've progressed further)
        if (hasShip && status < 5.5f) {
            newStatus = 5.5f
        }
        // If you have trapped soul, you should be at least 4.3 (ready for necromancer)
        else if (hasTrappedSoul && status < 4.3f) {
            newStatus = 4.3f
        }
        // If you have boat plans + all materials, you should be at least 4.0 (need necromancer)
        else if (hasBoatPlans && hasAllBoatMaterials && status < 4.0f) {
            newStatus = 4.0f
        }
        // If you have all boat materials (but no plans yet), you should be at least 3.8
        else if (hasAllBoatMaterials && status < 3.8f) {
            newStatus = 3.8f
        }
        // If you have boat plans (but not all materials), you should be at least 3.2
        else if (hasBoatPlans && status < 3.2f) {
            newStatus = 3.2f
        }

        // Return updated game state if status changed
        return if (newStatus != status) {
            android.util.Log.d("StoryProgressionChecker", "Auto-advancing story from $status to $newStatus based on inventory")
            gameState.copy(storyStatus = newStatus)
        } else {
            gameState
        }
    }

    /**
     * Specific check for when trapped soul is obtained
     * Now redundant with inventory-based checking, but kept for backwards compatibility
     */
    fun checkTrappedSoulProgress(gameState: GameState): GameState {
        return checkAndAdvanceStory(gameState)
    }
}
