package com.greenopal.zargon.data.repository

import android.content.Context
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.domain.story.StoryProgressionChecker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles saving and loading game state
 * Based on QBASIC savgam/opengam procedures (ZARGON.BAS:~2800)
 */
@Singleton
class SaveGameRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val sharedPrefs = context.getSharedPreferences("zargon_saves", Context.MODE_PRIVATE)

    /**
     * Save game to a slot (1-4)
     */
    fun saveGame(gameState: GameState, slot: Int = 1): Boolean {
        if (gameState.challengeConfig?.isPermadeath == true) {
            android.util.Log.d("SaveGameRepository", "Save blocked - Permanent Death mode active")
            return false
        }

        return try {
            android.util.Log.d("SaveGameRepository", "Saving game to slot $slot:")
            android.util.Log.d("SaveGameRepository", "  Story Status: ${gameState.storyStatus}")
            android.util.Log.d("SaveGameRepository", "  Position: Map ${gameState.worldX}${gameState.worldY} at (${gameState.characterX}, ${gameState.characterY})")
            android.util.Log.d("SaveGameRepository", "  HP: ${gameState.character.currentHP}/${gameState.character.maxHP}, MP: ${gameState.character.currentMP}/${gameState.character.maxMP}")
            android.util.Log.d("SaveGameRepository", "  Inventory (${gameState.inventory.size} items): ${gameState.inventory.map { it.name }}")

            val jsonString = json.encodeToString(gameState)
            sharedPrefs.edit()
                .putString("save_slot_$slot", jsonString)
                .putLong("save_time_$slot", System.currentTimeMillis())
                .apply()

            android.util.Log.d("SaveGameRepository", "Game saved successfully")
            true
        } catch (e: Exception) {
            android.util.Log.e("SaveGameRepository", "Failed to save game", e)
            e.printStackTrace()
            false
        }
    }

    /**
     * Load game from a slot
     */
    fun loadGame(slot: Int = 1): GameState? {
        return try {
            android.util.Log.d("SaveGameRepository", "Loading game from slot $slot")
            val jsonString = sharedPrefs.getString("save_slot_$slot", null) ?: return null
            val loadedState = json.decodeFromString<GameState>(jsonString)

            android.util.Log.d("SaveGameRepository", "Game loaded successfully:")
            android.util.Log.d("SaveGameRepository", "  Story Status (saved): ${loadedState.storyStatus}")
            android.util.Log.d("SaveGameRepository", "  Position: Map ${loadedState.worldX}${loadedState.worldY} at (${loadedState.characterX}, ${loadedState.characterY})")
            android.util.Log.d("SaveGameRepository", "  HP: ${loadedState.character.currentHP}/${loadedState.character.maxHP}, MP: ${loadedState.character.currentMP}/${loadedState.character.maxMP}")
            android.util.Log.d("SaveGameRepository", "  Inventory (${loadedState.inventory.size} items): ${loadedState.inventory.map { it.name }}")

            // Guard against corrupted saves where maxHP ended up below currentHP.
            val character = loadedState.character
            val migratedCharacter = if (character.maxHP < character.currentHP) {
                android.util.Log.w("SaveGameRepository", "Migrating save: maxHP(${character.maxHP}) < currentHP(${character.currentHP}), fixing")
                character.copy(maxHP = character.currentHP)
            } else {
                character
            }
            val afterHpMigration = if (migratedCharacter !== character) loadedState.copy(character = migratedCharacter) else loadedState

            // Reconstruct discoveredItems for saves created before this field existed.
            // Items consumed during story progression won't be in the current inventory,
            // so infer them from storyStatus.
            val status = afterHpMigration.storyStatus
            val storyInferredItems = buildSet {
                afterHpMigration.inventory.forEach { add(it.name.lowercase()) }
                if (status >= 2.5f) add("dynamite")           // used to rescue boatman
                if (status >= 3.8f) {                          // all 4 materials given to boatman
                    add("wood"); add("dead wood"); add("cloth"); add("rutter")
                }
                if (status >= 4.0f) add("boat plans")          // received from boatman
                if (status >= 5.0f) add("trapped soul")        // given to necromancer
                if (status >= 5.5f) add("ship")                // built by boatman
            }
            val mergedDiscovered = afterHpMigration.discoveredItems + storyInferredItems
            val migratedState = if (mergedDiscovered != afterHpMigration.discoveredItems) {
                android.util.Log.d("SaveGameRepository", "Reconstructed discoveredItems: $mergedDiscovered")
                afterHpMigration.copy(discoveredItems = mergedDiscovered)
            } else {
                afterHpMigration
            }

            // Auto-advance story based on inventory (in case items were obtained out of order)
            val gameState = StoryProgressionChecker.checkAndAdvanceStory(migratedState)

            if (gameState.storyStatus != loadedState.storyStatus) {
                android.util.Log.d("SaveGameRepository", "  Story Status (corrected): ${gameState.storyStatus}")
            }

            gameState
        } catch (e: Exception) {
            android.util.Log.e("SaveGameRepository", "Failed to load game from slot $slot", e)
            e.printStackTrace()
            null
        }
    }

    /**
     * Check if save exists in slot
     */
    fun hasSave(slot: Int): Boolean {
        return sharedPrefs.contains("save_slot_$slot")
    }

    /**
     * Get save timestamp
     */
    fun getSaveTime(slot: Int): Long {
        return sharedPrefs.getLong("save_time_$slot", 0L)
    }

    /**
     * Delete save from slot
     */
    fun deleteSave(slot: Int) {
        sharedPrefs.edit()
            .remove("save_slot_$slot")
            .remove("save_time_$slot")
            .apply()
    }

    /**
     * Get all save slots with metadata
     */
    fun getAllSaves(): List<SaveSlotInfo> {
        return (1..4).map { slot ->
            SaveSlotInfo(
                slot = slot,
                exists = hasSave(slot),
                timestamp = getSaveTime(slot),
                gameState = if (hasSave(slot)) loadGame(slot) else null
            )
        }
    }
}

data class SaveSlotInfo(
    val slot: Int,
    val exists: Boolean,
    val timestamp: Long,
    val gameState: GameState?
)
