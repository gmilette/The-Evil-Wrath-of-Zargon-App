package com.greenopal.zargon.domain.challenges

import com.greenopal.zargon.data.models.Challenge
import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.PrestigeBonus
import com.greenopal.zargon.data.models.PrestigeData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrestigeSystem @Inject constructor() {

    fun calculatePrestigeRewards(
        config: ChallengeConfig,
        currentPrestige: PrestigeData
    ): PrestigeData {
        val challengeId = config.getChallengeId()
        if (challengeId in currentPrestige.completedChallenges) {
            return currentPrestige
        }

        var newPrestige = currentPrestige.copy(
            completedChallenges = currentPrestige.completedChallenges + challengeId,
            totalCompletions = currentPrestige.totalCompletions + 1
        )

        val reward = getRewardForConfig(config)
        if (reward != null && reward !in newPrestige.unlockedBonuses) {
            newPrestige = newPrestige.copy(
                unlockedBonuses = newPrestige.unlockedBonuses + reward,
                activeBonuses = newPrestige.activeBonuses + reward
            )
        }

        return newPrestige
    }

    fun getRewardForConfig(config: ChallengeConfig): PrestigeBonus? {
        val challenges = config.challenges
        return when {
            Challenge.IMPOSSIBLE_MISSION in challenges -> PrestigeBonus.XP_BOOST
            Challenge.MAGE_QUEST in challenges -> PrestigeBonus.XP_BOOST
            Challenge.WARRIOR_MODE in challenges -> PrestigeBonus.XP_BOOST
            Challenge.ONE_DEATH in challenges -> PrestigeBonus.STARTING_GOLD
            Challenge.STRONG_ENEMIES in challenges || Challenge.STRONGER_ENEMIES in challenges -> PrestigeBonus.GOLD_BOOST
            Challenge.WEAK_WEAPONS in challenges -> PrestigeBonus.GREAT_WEAPONS
            Challenge.WEAK_ARMOR in challenges -> PrestigeBonus.GREATER_ARMOR
            Challenge.NO_MAGIC in challenges -> PrestigeBonus.MASTER_SPELLBOOK
            else -> null
        }
    }

    fun applyPrestigeBonusesToCharacter(
        baseStats: CharacterStats,
        prestige: PrestigeData
    ): CharacterStats {
        var stats = baseStats
        if (prestige.isBonusActive(PrestigeBonus.STARTING_GOLD)) {
            stats = stats.copy(gold = stats.gold + 100)
        }
        return stats
    }

    fun getXPMultiplier(prestige: PrestigeData): Float {
        val xpChallengeNames = listOf("IMPOSSIBLE_MISSION", "MAGE_QUEST", "WARRIOR_MODE")
        val completedXPChallenges = xpChallengeNames.count { challengeName ->
            prestige.completedChallenges.any { completedId -> challengeName in completedId }
        }
        return if (completedXPChallenges == 0) 1.0f
               else 1.0f + (completedXPChallenges * 0.25f)
    }

    fun getGoldMultiplier(prestige: PrestigeData): Float {
        return if (prestige.isBonusActive(PrestigeBonus.GOLD_BOOST)) 1.25f else 1.0f
    }
}
