package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import com.greenopal.zargon.domain.battle.MonsterSelector
import com.greenopal.zargon.domain.challenges.ChallengeModifiers
import com.greenopal.zargon.domain.progression.RewardSystem
import kotlin.random.Random

object SimulationHelpers {

    private val challengeModifiers = ChallengeModifiers()
    private val monsterSelector = MonsterSelector(challengeModifiers)
    val rewardSystem = RewardSystem()

    fun selectRandomMonster(playerLevel: Int, random: Random): MonsterType {
        return monsterSelector.selectRandomMonsterType(playerLevel, random)
    }

    fun createMonsterWithRandomScaling(
        type: MonsterType,
        playerLevel: Int,
        random: Random,
        config: ChallengeConfig? = null
    ): MonsterStats {
        val base = monsterSelector.createScaledMonster(type, playerLevel, random)
        return if (config != null) challengeModifiers.applyToMonster(base, config) else base
    }
}
