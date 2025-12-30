package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import com.greenopal.zargon.ui.screens.Weapon
import com.greenopal.zargon.ui.screens.Armor
import org.junit.Test
import kotlin.random.Random

class DemoWinRates {

    @Test
    fun `demo win rates for level 5 character`() {
        println("=== DEMO: Win Rates for Level 5 Character ===")
        println("Equipment: Long Sword (+5 AP) + Chain Mail (+28 Defense)")
        println()

        val simulator = BattleSimulator(Random(12345))
        val character = createCharacter(5, Weapon.LONG_SWORD, Armor.CHAIN_MAIL)

        println("Player Stats: Level ${character.level}, AP=${character.totalAP}, HP=${character.currentDP}, Defense=${character.totalDefense}")
        println()

        val allMonsters = MonsterType.values()

        for (monsterType in allMonsters) {
            println("Monster: $monsterType (Base: AP=${monsterType.baseAP}, HP=${monsterType.baseDP})")
            println("Scale | Monster Stats    | Win Rate | Avg Turns")
            println("------|------------------|----------|----------")

            for (scale in 1..10) {
                val monster = createMonster(monsterType, scale)
                val results = (1..100).map {
                    simulator.simulateBattle(character, monster)
                }

                val wins = results.count { it.outcome == BattleSimulator.BattleOutcome.PlayerVictory }
                val winRate = wins / 100.0
                val avgTurns = results.filter { it.outcome == BattleSimulator.BattleOutcome.PlayerVictory }
                    .map { it.turnsElapsed }
                    .average()

                val turnsStr = if (avgTurns.isNaN()) "N/A" else String.format("%.1f", avgTurns)
                val statsStr = "AP=${monster.attackPower.toString().padEnd(3)} HP=${monster.currentHP.toString().padEnd(3)}"

                println("  ${scale.toString().padEnd(3)} | $statsStr | ${String.format("%6.0f", winRate * 100)}%  | $turnsStr")
            }
            println()
        }
    }

    @Test
    fun `demo win rates across levels vs NECRO`() {
        println("=== DEMO: Win Rates Across Levels vs NECRO ===")
        println("Equipment: Long Sword (+5 AP) + Chain Mail (+28 Defense)")
        println()

        val simulator = BattleSimulator(Random(12345))

        for (scale in listOf(1, 2, 5, 10)) {
            val monster = createMonster(MonsterType.NECRO, scale)
            println("NECRO x$scale (AP=${monster.attackPower}, HP=${monster.currentHP})")
            println("Level | Player Stats     | Win Rate | Avg Turns")
            println("------|------------------|----------|----------")

            for (level in listOf(1, 3, 5, 7, 10, 15)) {
                val character = createCharacter(level, Weapon.LONG_SWORD, Armor.CHAIN_MAIL)
                val results = (1..100).map {
                    simulator.simulateBattle(character, monster)
                }

                val wins = results.count { it.outcome == BattleSimulator.BattleOutcome.PlayerVictory }
                val winRate = wins / 100.0
                val avgTurns = results.filter { it.outcome == BattleSimulator.BattleOutcome.PlayerVictory }
                    .map { it.turnsElapsed }
                    .average()

                val turnsStr = if (avgTurns.isNaN()) "N/A" else String.format("%.1f", avgTurns)
                val statsStr = "AP=${character.totalAP.toString().padEnd(2)} HP=${character.currentDP.toString().padEnd(3)}"

                println("  ${level.toString().padEnd(3)} | $statsStr       | ${String.format("%6.0f", winRate * 100)}%  | $turnsStr")
            }
            println()
        }
    }

    private fun createCharacter(level: Int, weapon: Weapon, armor: Armor): CharacterStats {
        val baseAP = 5 + (level - 1) * 3
        val baseDP = 20 + (level - 1) * 6
        val baseMP = 10 + (level - 1) * 4

        return CharacterStats(
            baseAP = baseAP,
            baseDP = baseDP,
            currentDP = baseDP,
            baseMP = baseMP,
            currentMP = baseMP,
            level = level,
            weaponBonus = weapon.attackBonus,
            armorBonus = armor.defenseBonus
        )
    }

    private fun createMonster(type: MonsterType, scalingFactor: Int): MonsterStats {
        return MonsterStats(
            type = type,
            attackPower = type.baseAP * scalingFactor,
            currentHP = type.baseDP * scalingFactor,
            maxHP = type.baseDP * scalingFactor,
            scalingFactor = scalingFactor
        )
    }
}
