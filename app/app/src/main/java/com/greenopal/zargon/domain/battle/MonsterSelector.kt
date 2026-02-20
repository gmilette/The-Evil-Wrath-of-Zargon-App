package com.greenopal.zargon.domain.battle

import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import com.greenopal.zargon.domain.challenges.ChallengeModifiers
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class MonsterSelector @Inject constructor(
    private val challengeModifiers: ChallengeModifiers
) {

    fun selectMonster(gameState: GameState): MonsterStats {
        val playerLevel = gameState.character.level

        if (gameState.worldX == 3 && gameState.worldY == 2) {
            if (gameState.characterX in 13..16 && gameState.characterY in 4..6) {
                val baseZargon = MonsterStats(
                    type = MonsterType.ZARGON,
                    attackPower = 60,
                    currentHP = 300,
                    maxHP = 300,
                    scalingFactor = 1
                )
                return applyModifiersAndLabel(baseZargon, gameState)
            }
        }

        if (gameState.inShip) {
            val baseKraken = MonsterStats(
                type = MonsterType.KRAKEN,
                attackPower = MonsterType.KRAKEN.baseAP,
                currentHP = MonsterType.KRAKEN.baseDP,
                maxHP = MonsterType.KRAKEN.baseDP,
                scalingFactor = 1
            )
            return applyModifiersAndLabel(baseKraken, gameState)
        }

        if (gameState.worldX == 4 && gameState.worldY == 2 &&
            gameState.storyStatus >= 3.0f &&
            gameState.characterX == 3 && gameState.characterY == 2) {
            val baseNecro = MonsterStats(
                type = MonsterType.NECRO,
                attackPower = 45,
                currentHP = 30,
                maxHP = 30,
                scalingFactor = 1
            )
            return applyModifiersAndLabel(baseNecro, gameState)
        }

        var monsterType: MonsterType
        do {
            val roll = Random.nextInt(1, 22)

            monsterType = when (roll) {
                in 1..4 -> MonsterType.BAT
                in 5..7 -> MonsterType.BABBLE
                in 8..9 -> MonsterType.SPOOK
                in 13..16 -> MonsterType.SLIME
                in 10..12 -> {
                    if (playerLevel >= 2) MonsterType.BELETH
                    else continue
                }
                in 17..19 -> {
                    if (playerLevel >= 5) MonsterType.SKANDER_SNAKE
                    else continue
                }
                in 20..21 -> {
                    if (playerLevel >= 6) MonsterType.NECRO
                    else continue
                }
                else -> continue
            }
            break
        } while (true)

        val baseMonster = createScaledMonster(monsterType, playerLevel)
        return applyModifiersAndLabel(baseMonster, gameState)
    }

    fun selectRandomMonsterType(playerLevel: Int, random: Random = Random): MonsterType {
        while (true) {
            val roll = random.nextInt(1, 22)
            val type = when (roll) {
                in 1..4 -> MonsterType.BAT
                in 5..7 -> MonsterType.BABBLE
                in 8..9 -> MonsterType.SPOOK
                in 13..16 -> MonsterType.SLIME
                in 10..12 -> if (playerLevel >= 2) MonsterType.BELETH else continue
                in 17..19 -> if (playerLevel >= 5) MonsterType.SKANDER_SNAKE else continue
                in 20..21 -> if (playerLevel >= 6) MonsterType.NECRO else continue
                else -> continue
            }
            return type
        }
    }

    fun createScaledMonster(
        type: MonsterType,
        playerLevel: Int,
        random: Random = Random
    ): MonsterStats {
        var scalingFactor = 1
        var prefix = ""
        for (i in 1 until playerLevel) {
            val whatlev = random.nextInt(1, 4)
            if (whatlev >= 2) {
                prefix += "Great "
                scalingFactor++
            }
        }

        val scaledAP = type.baseAP * scalingFactor
        val scaledDP = type.baseDP * scalingFactor

        val displayName = if (prefix.isNotEmpty()) {
            prefix + type.displayName
        } else {
            null
        }

        return MonsterStats(
            type = type,
            attackPower = scaledAP,
            currentHP = scaledDP,
            maxHP = scaledDP,
            scalingFactor = scalingFactor,
            displayName = displayName
        )
    }

    private fun applyModifiersAndLabel(monster: MonsterStats, gameState: GameState): MonsterStats {
        val config = gameState.challengeConfig ?: return monster
        val modifiedMonster = challengeModifiers.applyToMonster(monster, config)
        val label = challengeModifiers.getMonsterDisplayName(
            modifiedMonster.displayName ?: modifiedMonster.type.displayName,
            config
        )
        return if (label != (modifiedMonster.displayName ?: modifiedMonster.type.displayName)) {
            modifiedMonster.copy(displayName = label)
        } else {
            modifiedMonster
        }
    }
}
