package com.greenopal.zargon.domain.challenges

import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.DifficultyLevel
import com.greenopal.zargon.data.models.EquipmentMode
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.data.models.TimedChallenge
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrestigeSystem @Inject constructor() {

    fun calculatePrestigeRewards(
        config: ChallengeConfig,
        currentPrestige: PrestigeData
    ): PrestigeData {
        if (config.getChallengeId() in currentPrestige.completedChallenges) {
            return currentPrestige
        }

        var newPrestige = currentPrestige.copy(
            completedChallenges = currentPrestige.completedChallenges + config.getChallengeId(),
            totalCompletions = currentPrestige.totalCompletions + 1
        )

        // Award permanent AP bonus (one-time, on first challenge completion)
        if (!currentPrestige.hasAPBonus) {
            newPrestige = newPrestige.copy(
                hasAPBonus = true
            )
        }

        // Award permanent DP bonus (one-time, on first challenge completion)
        if (!currentPrestige.hasDPBonus) {
            newPrestige = newPrestige.copy(
                hasDPBonus = true
            )
        }

        if (config.timedChallenge != TimedChallenge.NONE) {
            newPrestige = newPrestige.copy(
                startingGoldBonus = minOf(newPrestige.startingGoldBonus + 10, 100)
            )
        }

        if (config.difficulty == DifficultyLevel.INSANE) {
            newPrestige = newPrestige.copy(
                xpMultiplierBonus = minOf(newPrestige.xpMultiplierBonus + 0.05f, 0.5f)
            )
        }

        return newPrestige
    }

    fun applyPrestigeBonusesToCharacter(
        baseStats: CharacterStats,
        prestige: PrestigeData
    ): CharacterStats {
        return baseStats.copy(
            gold = baseStats.gold + prestige.startingGoldBonus
        )
    }

    fun getXPMultiplier(prestige: PrestigeData): Float {
        return 1.0f + prestige.xpMultiplierBonus
    }
}
