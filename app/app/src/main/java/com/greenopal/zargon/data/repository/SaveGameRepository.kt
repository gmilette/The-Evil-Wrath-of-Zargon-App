package com.greenopal.zargon.data.repository

import android.content.Context
import com.greenopal.zargon.data.models.GameState
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
     * Save game to a slot (1-3)
     */
    fun saveGame(gameState: GameState, slot: Int = 1): Boolean {
        return try {
            val jsonString = json.encodeToString(gameState)
            sharedPrefs.edit()
                .putString("save_slot_$slot", jsonString)
                .putLong("save_time_$slot", System.currentTimeMillis())
                .apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Load game from a slot
     */
    fun loadGame(slot: Int = 1): GameState? {
        return try {
            val jsonString = sharedPrefs.getString("save_slot_$slot", null) ?: return null
            json.decodeFromString<GameState>(jsonString)
        } catch (e: Exception) {
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
        return (1..3).map { slot ->
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
