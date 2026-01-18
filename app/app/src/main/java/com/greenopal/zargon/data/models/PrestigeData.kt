package com.greenopal.zargon.data.models

import kotlinx.serialization.Serializable

@Serializable
data class PrestigeData(
    val completedChallenges: Set<String> = emptySet(),  // Challenge IDs
    val hasAPBonus: Boolean = false,    // Permanent +5 AP (earned once)
    val hasDPBonus: Boolean = false,    // Permanent +5 DP (earned once)
    val startingGoldBonus: Int = 0,     // From completing speedrun challenges
    val xpMultiplierBonus: Float = 0f,  // From completing insane challenges
    val totalCompletions: Int = 0
) {
    val permanentAPBonus: Int get() = if (hasAPBonus) 5 else 0
    val permanentDPBonus: Int get() = if (hasDPBonus) 5 else 0

    fun isComplete(): Boolean {
        // Calculate total possible unique combinations
        val totalCombinations = DifficultyLevel.values().size *
            EquipmentMode.values().size *
            EquipmentMode.values().size *
            2 *  // permanentDeath boolean
            TimedChallenge.values().size
        return completedChallenges.size >= totalCombinations
    }

    fun getCompletionPercentage(): Float {
        val totalCombinations = 3 * 4 * 4 * 2 * 3  // 288 total combinations
        return (completedChallenges.size.toFloat() / totalCombinations) * 100
    }
}

@Serializable
data class ChallengeResult(
    val challengeId: String,
    val completedAt: Long,
    val finalStats: ChallengeCompletionStats,
    val timeElapsedMs: Long
)

@Serializable
data class ChallengeCompletionStats(
    val finalLevel: Int,
    val totalGoldEarned: Int,
    val monstersDefeated: Int,
    val deathCount: Int  // 0 for successful runs, 1+ for non-permadeath restarts
)
