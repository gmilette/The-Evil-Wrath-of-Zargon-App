package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import com.greenopal.zargon.ui.screens.Weapon
import com.greenopal.zargon.ui.screens.Armor
import org.junit.Test
import kotlin.random.Random

class QuickWinRateTest {

    @Test
    fun `show win rates by monster and scale`() {
        val config = SimulationConfig(
            iterationsPerScenario = 100,
            playerLevels = (1..15).toList(),
            monsterScalingFactors = (1..10).toList(),
            randomSeed = 12345L
        )

        println("Running quick win rate analysis...")
        println("Testing: Levels 1-15 vs all monsters at scales 1-10")
        println("Iterations per scenario: ${config.iterationsPerScenario}")
        println()

        val summary = runSimulation(config)

        println()
        println("=== WIN RATE BY MONSTER TYPE AND SCALING ===")
        println()

        val monsterTypes = summary.results.map { it.monsterType }.distinct().sorted()
        val scalingFactors = summary.results.map { it.monsterScalingFactor }.distinct().sorted()

        for (monsterType in monsterTypes) {
            println("Monster: $monsterType")
            println("Scale | Win Rate | Avg Turns | Sample Size")
            println("------|----------|-----------|------------")

            for (scale in scalingFactors) {
                val scenarios = summary.results.filter {
                    it.monsterType == monsterType && it.monsterScalingFactor == scale
                }

                if (scenarios.isNotEmpty()) {
                    val avgWinRate = scenarios.map { it.winRate }.average()
                    val avgTurns = scenarios.filter { it.avgTurnsToWin > 0 }.map { it.avgTurnsToWin }.average()
                    val totalBattles = scenarios.sumOf { it.iterations }

                    val turnsStr = if (avgTurns.isNaN()) "N/A" else String.format("%.1f", avgTurns)
                    println("  ${scale.toString().padEnd(3)} | ${String.format("%6.1f", avgWinRate * 100)}%  | ${turnsStr.padEnd(9)} | $totalBattles")
                }
            }
            println()
        }

        println("Total simulation time: ${summary.executionTimeMs / 1000.0}s")
    }

    private fun runSimulation(config: SimulationConfig): SimulationSummary {
        val random = config.randomSeed?.let { Random(it) } ?: Random.Default
        val simulator = BattleSimulator(random)
        val results = mutableListOf<ScenarioResult>()
        val startTime = System.currentTimeMillis()

        val totalScenarios = config.playerLevels.size * config.weapons.size * config.armors.size *
                config.monsterTypes.size * config.monsterScalingFactors.size

        var completedScenarios = 0

        for (level in config.playerLevels) {
            for (weapon in config.weapons) {
                for (armor in config.armors) {
                    for (monsterType in config.monsterTypes) {
                        for (scale in config.monsterScalingFactors) {
                            val character = createCharacter(level, weapon, armor)
                            val monster = createMonster(monsterType, scale)

                            val logs = (1..config.iterationsPerScenario).map {
                                simulator.simulateBattle(character, monster)
                            }

                            results.add(aggregateLogs(level, weapon, armor, monsterType, scale, logs))
                            completedScenarios++

                            if (completedScenarios % 1000 == 0) {
                                print(".")
                            }
                        }
                    }
                }
            }
        }

        println()

        return SimulationSummary(
            totalScenarios = results.size,
            totalBattles = results.size * config.iterationsPerScenario,
            executionTimeMs = System.currentTimeMillis() - startTime,
            results = results
        )
    }

    private fun createCharacter(level: Int, weapon: Weapon, armor: Armor): CharacterStats {
        val baseAP = 5 + (level - 1) * 3
        val baseDP = 20 + (level - 1) * 4
        val maxHP = 20 + (level - 1) * 5
        val baseMP = 10 + (level - 1) * 4

        return CharacterStats(
            baseAP = baseAP,
            baseDP = baseDP,
            maxHP = maxHP,
            currentHP = maxHP,
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

    private fun aggregateLogs(
        level: Int,
        weapon: Weapon,
        armor: Armor,
        monsterType: MonsterType,
        scale: Int,
        logs: List<BattleSimulator.BattleLog>
    ): ScenarioResult {
        val wins = logs.count { it.outcome == BattleSimulator.BattleOutcome.PlayerVictory }
        val losses = logs.count { it.outcome == BattleSimulator.BattleOutcome.PlayerDefeat }
        val winLogs = logs.filter { it.outcome == BattleSimulator.BattleOutcome.PlayerVictory }
        val loseLogs = logs.filter { it.outcome == BattleSimulator.BattleOutcome.PlayerDefeat }

        return ScenarioResult(
            playerLevel = level,
            weapon = weapon,
            armor = armor,
            monsterType = monsterType,
            monsterScalingFactor = scale,
            iterations = logs.size,
            wins = wins,
            losses = losses,
            winRate = wins.toDouble() / logs.size,
            avgTurnsToWin = if (winLogs.isNotEmpty()) winLogs.map { it.turnsElapsed }.average() else 0.0,
            avgTurnsToLose = if (loseLogs.isNotEmpty()) loseLogs.map { it.turnsElapsed }.average() else 0.0,
            avgPlayerHPRemaining = winLogs.map { it.playerHPRemaining }.average().takeIf { !it.isNaN() } ?: 0.0,
            avgDamageDealt = logs.map { it.playerDamageDealt }.average(),
            avgDamageTaken = logs.map { it.playerDamageTaken }.average()
        )
    }
}
