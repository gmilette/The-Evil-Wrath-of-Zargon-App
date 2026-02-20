package com.greenopal.zargon.data.repository

import android.content.Context
import com.greenopal.zargon.data.models.ChallengeResult
import com.greenopal.zargon.data.models.PrestigeData
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrestigeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val prestigePrefs = context.getSharedPreferences("zargon_prestige", Context.MODE_PRIVATE)
    private val _prestigeFlow = MutableStateFlow(PrestigeData())

    init {
        _prestigeFlow.value = loadPrestige()
    }

    fun loadPrestigeFlow(): Flow<PrestigeData> = _prestigeFlow

    fun savePrestige(prestige: PrestigeData) {
        try {
            val jsonString = json.encodeToString(prestige)
            prestigePrefs.edit()
                .putString("prestige_data", jsonString)
                .commit()  // Use commit() instead of apply() for synchronous write
            _prestigeFlow.value = prestige
        } catch (e: Exception) {
            android.util.Log.e("PrestigeRepository", "Failed to save prestige", e)
        }
    }

    fun loadPrestige(): PrestigeData {
        return try {
            val jsonString = prestigePrefs.getString("prestige_data", null)
            if (jsonString != null) {
                json.decodeFromString(jsonString)
            } else {
                PrestigeData()
            }
        } catch (e: Exception) {
            android.util.Log.e("PrestigeRepository", "Failed to load prestige", e)
            PrestigeData()
        }
    }

    fun saveChallengeResult(result: ChallengeResult) {
        try {
            val existingResults = loadChallengeHistory()
            val updatedResults = existingResults + result
            val jsonString = json.encodeToString(updatedResults)
            prestigePrefs.edit()
                .putString("challenge_history", jsonString)
                .commit()  // Use commit() instead of apply() for synchronous write
        } catch (e: Exception) {
            android.util.Log.e("PrestigeRepository", "Failed to save challenge result", e)
        }
    }

    fun loadChallengeHistory(): List<ChallengeResult> {
        return try {
            val jsonString = prestigePrefs.getString("challenge_history", null)
            if (jsonString != null) {
                json.decodeFromString(jsonString)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
