package com.greenopal.zargon.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class PrestigeBonus(
    val displayName: String,
    val description: String
) {
    XP_BOOST("XP Boost", "+10% XP per enemy"),
    STARTING_GOLD("Starting Gold", "+100 starting gold"),
    GOLD_BOOST("Gold Boost", "+10% gold per enemy"),
    GREAT_WEAPONS("Great Weapons", "Weapons 150% more effective"),
    GREATER_ARMOR("Greater Armor", "Armor 100% more effective"),
    MASTER_SPELLBOOK("Master Spellbook", "All spells 50% more effective")
}

@Serializable
data class PrestigeData(
    val completedChallenges: Set<String> = emptySet(),
    val unlockedBonuses: Set<PrestigeBonus> = emptySet(),
    val activeBonuses: Set<PrestigeBonus> = emptySet(),
    val totalCompletions: Int = 0
) {
    fun isBonusActive(bonus: PrestigeBonus): Boolean = bonus in activeBonuses
    fun isBonusUnlocked(bonus: PrestigeBonus): Boolean = bonus in unlockedBonuses

    fun getCompletionPercentage(): Float {
        val totalPossible = Challenge.values().size
        return (completedChallenges.size.toFloat() / totalPossible) * 100
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
    val deathCount: Int
)
