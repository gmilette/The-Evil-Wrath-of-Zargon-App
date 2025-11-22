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
     * Based on QBASIC lines 3194-3201
     */
    fun checkAndAdvanceStory(gameState: GameState): GameState {
        val status = gameState.storyStatus
        var newStatus = status

        // QBASIC line 3198: IF items(7) = "trapped soul" AND storystatus = 4 THEN storystatus = 4.3
        if (gameState.hasItem("trapped soul") && status == 4.0f) {
            newStatus = 4.3f
        }

        // QBASIC line 3199: IF items(7) = "trapped soul(used)" AND storystatus = 4.3 THEN storystatus = 5
        // Note: This is handled by Necromancer dialog's ResurrectBoatman action

        // QBASIC line 3200: IF items(8) = "ship" AND storystatus = 5 THEN storystatus = 5.5
        if (gameState.hasItem("ship") && status == 5.0f) {
            newStatus = 5.5f
        }

        // QBASIC line 3195: IF items(3) = "boat list" AND storystatus = 3 THEN storystatus = 3.2
        // Note: "boat list" renamed to "boat plans" in Android version
        if (gameState.hasItem("boat plans") && status == 3.0f) {
            newStatus = 3.2f
        }

        // QBASIC line 3196: IF items(4) <> "" AND items(5) <> "" AND items(6) <> "" AND storystatus = 3.2 THEN storystatus = 3.8
        val hasRutter = gameState.hasItem("rutter")
        val hasCloth = gameState.hasItem("cloth")
        val hasWood = gameState.hasItem("wood")
        if (hasRutter && hasCloth && hasWood && status == 3.2f) {
            newStatus = 3.8f
        }

        // QBASIC line 3197: IF items(3) = "boat plans" AND storystatus = 3.8 THEN storystatus = 4
        if (gameState.hasItem("boat plans") && status == 3.8f) {
            newStatus = 4.0f
        }

        // Return updated game state if status changed
        return if (newStatus != status) {
            android.util.Log.d("StoryProgressionChecker", "Auto-advancing story from $status to $newStatus")
            gameState.copy(storyStatus = newStatus)
        } else {
            gameState
        }
    }

    /**
     * Specific check for when trapped soul is obtained
     * This ensures story advances immediately when soul is picked up
     */
    fun checkTrappedSoulProgress(gameState: GameState): GameState {
        // If player just got trapped soul and is at status 4.0, advance to 4.3
        if (gameState.hasItem("trapped soul") && gameState.storyStatus == 4.0f) {
            android.util.Log.d("StoryProgressionChecker", "Trapped soul obtained - advancing from 4.0 to 4.3")
            return gameState.copy(storyStatus = 4.3f)
        }
        return gameState
    }
}
