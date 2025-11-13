package com.greenopal.zargon.domain.battle

import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Selects random monsters for battles based on player level and game state.
 * Based on SelectMonsta procedure (ZARGON.BAS:3083)
 */
@Singleton
class MonsterSelector @Inject constructor() {

    /**
     * Select a monster for battle
     *
     * @param gameState Current game state (for level, position, story status)
     * @return MonsterStats with appropriate scaling
     */
    fun selectMonster(gameState: GameState): MonsterStats {
        val playerLevel = gameState.character.level

        // Special case: Kraken when in ship
        if (gameState.inShip) {
            return createMonster(MonsterType.KRAKEN, playerLevel, scalingFactor = 1)
        }

        // Special case: Boss Necro at specific location
        // QBASIC: IF mapx = 4 AND mapy = 2 AND storystatus = 4 AND cx = 3 AND cy = 2
        if (gameState.worldX == 4 && gameState.worldY == 2 &&
            gameState.storyStatus == 4.0f &&
            gameState.characterX == 3 && gameState.characterY == 2) {
            return MonsterStats(
                type = MonsterType.NECRO,
                attackPower = 45,
                currentHP = 30,
                maxHP = 30,
                scalingFactor = 1
            )
        }

        // Regular monster selection with level gating
        var monsterType: MonsterType
        do {
            val roll = Random.nextInt(1, 22) // 1-21 inclusive

            monsterType = when (roll) {
                in 1..4 -> MonsterType.BAT
                in 5..7 -> MonsterType.BABBLE
                in 8..9 -> MonsterType.SPOOK
                in 13..16 -> MonsterType.SLIME
                in 10..12 -> {
                    // Beleth requires level 2+
                    if (playerLevel >= 2) MonsterType.BELETH
                    else continue // Re-roll
                }
                in 17..19 -> {
                    // SkanderSnake requires level 5+
                    if (playerLevel >= 5) MonsterType.SKANDER_SNAKE
                    else continue // Re-roll
                }
                in 20..21 -> {
                    // Necro requires level 6+
                    if (playerLevel >= 6) MonsterType.NECRO
                    else continue // Re-roll
                }
                else -> continue // Invalid roll, re-roll
            }
            break
        } while (true)

        // Calculate scaling factor (howmuchbigger in QBASIC)
        // For each level above 1, 50% chance to increase scaling
        var scalingFactor = 1
        var prefix = ""
        for (i in 1 until playerLevel) {
            val whatlev = Random.nextInt(1, 3) // 1-2
            if (whatlev == 2) {
                prefix += "Great "
                scalingFactor++
            }
        }

        return createMonster(monsterType, playerLevel, scalingFactor, prefix)
    }

    /**
     * Create a monster with scaling applied
     */
    private fun createMonster(
        type: MonsterType,
        playerLevel: Int,
        scalingFactor: Int,
        prefix: String = ""
    ): MonsterStats {
        val scaledAP = type.baseAP * scalingFactor
        val scaledDP = type.baseDP * scalingFactor

        return MonsterStats(
            type = type,
            attackPower = scaledAP,
            currentHP = scaledDP,
            maxHP = scaledDP,
            scalingFactor = scalingFactor
        ).copy(
            type = type.copy(displayName = prefix + type.displayName)
        )
    }

    /**
     * Extension to copy MonsterType with new display name
     */
    private fun MonsterType.copy(displayName: String): MonsterType {
        // Create a wrapper that preserves all properties but changes display name
        return object : MonsterType(
            displayName = displayName,
            baseAP = this.baseAP,
            baseDP = this.baseDP,
            minLevel = this.minLevel,
            baseExperience = this.baseExperience,
            baseGold = this.baseGold
        ) {
            override fun toString() = displayName
        }
    }
}
