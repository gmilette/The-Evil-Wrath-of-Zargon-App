package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import kotlin.random.Random

class BattleSimulator(
    private val random: Random = Random.Default
) {
    sealed class BattleOutcome {
        object PlayerVictory : BattleOutcome()
        object PlayerDefeat : BattleOutcome()
    }

    data class BattleLog(
        val outcome: BattleOutcome,
        val turnsElapsed: Int,
        val playerDamageDealt: Int,
        val playerDamageTaken: Int,
        val playerHPRemaining: Int,
        val monsterHPRemaining: Int
    )

    fun simulateBattle(
        character: CharacterStats,
        monster: MonsterStats
    ): BattleLog {
        var currentPlayerHP = character.currentDP
        var currentMonsterHP = monster.currentHP
        var turns = 0
        var totalPlayerDamageDealt = 0
        var totalPlayerDamageTaken = 0

        while (currentPlayerHP > 0 && currentMonsterHP > 0) {
            turns++

            val playerDamage = character.totalAP
            currentMonsterHP -= playerDamage
            totalPlayerDamageDealt += playerDamage

            if (currentMonsterHP <= 0) break

            val monsterDamage = calculateMonsterDamage(monster, character)
            currentPlayerHP -= monsterDamage
            totalPlayerDamageTaken += monsterDamage
        }

        val outcome = when {
            currentPlayerHP <= 0 -> BattleOutcome.PlayerDefeat
            currentMonsterHP <= 0 -> BattleOutcome.PlayerVictory
            else -> throw IllegalStateException("Battle ended without resolution")
        }

        return BattleLog(
            outcome = outcome,
            turnsElapsed = turns,
            playerDamageDealt = totalPlayerDamageDealt,
            playerDamageTaken = totalPlayerDamageTaken,
            playerHPRemaining = maxOf(0, currentPlayerHP),
            monsterHPRemaining = maxOf(0, currentMonsterHP)
        )
    }

    private fun calculateMonsterDamage(monster: MonsterStats, character: CharacterStats): Int {
        val randomFactor = if (monster.scalingFactor > 0) {
            random.nextInt(0, monster.scalingFactor + 1)
        } else {
            0
        }
        val rawDamage = monster.attackPower - character.totalDefense + randomFactor
        return maxOf(1, rawDamage)
    }
}
