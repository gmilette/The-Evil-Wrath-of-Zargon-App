package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import com.greenopal.zargon.ui.screens.Weapon
import com.greenopal.zargon.ui.screens.Armor
import org.junit.Test
import java.io.File
import kotlin.random.Random

class RunAndSaveResults {

    @Test
    fun `generate win rate report`() {
        val outputFile = File("simulation_results.txt")
        val output = StringBuilder()

        output.appendLine("=== ZARGON WIN RATE ANALYSIS ===")
        output.appendLine("Generated: ${java.time.LocalDateTime.now()}")
        output.appendLine()
        output.appendLine("Testing: Level 5 Character with Long Sword + Chain Mail")
        output.appendLine()

        val simulator = BattleSimulator(Random(12345))
        val character = createCharacter(5, Weapon.LONG_SWORD, Armor.CHAIN_MAIL)

        output.appendLine("Player Stats: Level ${character.level}, AP=${character.totalAP}, HP=${character.currentDP}, Defense=${character.totalDefense}")
        output.appendLine()
        output.appendLine("=== WIN RATES BY MONSTER AND SCALE (100 battles each) ===")
        output.appendLine()

        val allMonsters = MonsterType.values().sortedBy { it.name }

        for (monsterType in allMonsters) {
            output.appendLine("Monster: $monsterType (Base: AP=${monsterType.baseAP}, HP=${monsterType.baseDP})")
            output.appendLine("Scale | Monster Stats    | Win Rate | Avg Turns | Avg HP Left")
            output.appendLine("------|------------------|----------|-----------|------------")

            for (scale in 1..10) {
                val monster = createMonster(monsterType, scale)
                val results = (1..100).map {
                    simulator.simulateBattle(character, monster)
                }

                val wins = results.count { it.outcome == BattleSimulator.BattleOutcome.PlayerVictory }
                val winRate = wins / 100.0
                val winningBattles = results.filter { it.outcome == BattleSimulator.BattleOutcome.PlayerVictory }
                val avgTurns = winningBattles.map { it.turnsElapsed }.average()
                val avgHPLeft = winningBattles.map { it.playerHPRemaining }.average()

                val turnsStr = if (avgTurns.isNaN()) "  N/A" else String.format("%5.1f", avgTurns)
                val hpStr = if (avgHPLeft.isNaN()) " N/A" else String.format("%4.0f", avgHPLeft)
                val statsStr = "AP=${monster.attackPower.toString().padEnd(3)} HP=${monster.currentHP.toString().padEnd(3)}"

                output.appendLine("  ${scale.toString().padEnd(3)} | $statsStr | ${String.format("%6.0f", winRate * 100)}%  | $turnsStr     | $hpStr")
            }
            output.appendLine()
        }

        outputFile.writeText(output.toString())
        println("Results written to: ${outputFile.absolutePath}")
        println()
        println(output.toString())
    }

    @Test
    fun `generate level progression report vs key monsters`() {
        val outputFile = File("level_progression_results.txt")
        val output = StringBuilder()

        output.appendLine("=== LEVEL PROGRESSION ANALYSIS ===")
        output.appendLine("Equipment: Long Sword + Chain Mail")
        output.appendLine()

        val simulator = BattleSimulator(Random(12345))
        val keyMonsters = listOf(
            MonsterType.SLIME to listOf(1, 3, 5, 7, 10),
            MonsterType.NECRO to listOf(1, 2, 3, 5, 10),
            MonsterType.SKANDER_SNAKE to listOf(1, 2, 3, 5, 10),
            MonsterType.ZARGON to listOf(1, 2, 3)
        )

        for ((monsterType, scales) in keyMonsters) {
            for (scale in scales) {
                val monster = createMonster(monsterType, scale)
                output.appendLine("$monsterType x$scale (AP=${monster.attackPower}, HP=${monster.currentHP})")
                output.appendLine("Level | Player Stats      | Win Rate | Avg Turns")
                output.appendLine("------|-------------------|----------|----------")

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

                    val turnsStr = if (avgTurns.isNaN()) " N/A" else String.format("%4.1f", avgTurns)
                    val statsStr = "AP=${character.totalAP.toString().padEnd(2)} HP=${character.currentDP.toString().padEnd(3)} Def=${character.totalDefense}"

                    output.appendLine("  ${level.toString().padEnd(3)} | $statsStr | ${String.format("%6.0f", winRate * 100)}%  | $turnsStr")
                }
                output.appendLine()
            }
        }

        outputFile.writeText(output.toString())
        println("Results written to: ${outputFile.absolutePath}")
        println()
        println(output.toString())
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
