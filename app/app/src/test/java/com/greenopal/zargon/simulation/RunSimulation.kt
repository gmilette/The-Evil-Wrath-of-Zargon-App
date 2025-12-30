package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import com.greenopal.zargon.simulation.reports.ReportGenerator
import com.greenopal.zargon.ui.screens.Weapon
import com.greenopal.zargon.ui.screens.Armor
import kotlin.random.Random

fun main() {
    println("Zargon Battle Balance Simulator")
    println("================================")
    println()

    val simulator = BattleSimulator(Random(12345))

    println("Quick test: Level 5 + Long Sword + Chain Mail vs NECRO x2")
    val character = createCharacter(5, Weapon.LONG_SWORD, Armor.CHAIN_MAIL)
    val monster = createMonster(MonsterType.NECRO, 2)

    println("Player: AP=${character.totalAP}, HP=${character.currentDP}, Defense=${character.totalDefense}")
    println("Monster: AP=${monster.attackPower}, HP=${monster.currentHP}, Scale=${monster.scalingFactor}")
    println()

    val results = (1..1000).map {
        simulator.simulateBattle(character, monster)
    }

    val wins = results.count { it.outcome == BattleSimulator.BattleOutcome.PlayerVictory }
    val avgTurns = results.filter { it.outcome == BattleSimulator.BattleOutcome.PlayerVictory }
        .map { it.turnsElapsed }.average()

    println("Results from 1000 battles:")
    println("  Wins: $wins (${wins / 10.0}%)")
    println("  Losses: ${1000 - wins} (${(1000 - wins) / 10.0}%)")
    println("  Avg turns to victory: ${"%.1f".format(avgTurns)}")
    println()
    println("Running full simulation...")
    println()

    val config = SimulationConfig(
        iterationsPerScenario = 1000,
        playerLevels = (1..15).toList(),
        monsterScalingFactors = (1..10).toList(),
        randomSeed = 12345L
    )

    val summary = runFullSimulation(config)
    val reportGenerator = ReportGenerator()

    reportGenerator.generateConsoleReport(summary)

    val csvPath = "balance_simulation.csv"
    val mdPath = "balance_simulation.md"
    reportGenerator.generateCSV(summary, csvPath)
    reportGenerator.generateMarkdown(summary, mdPath)

    println()
    println("Reports generated:")
    println("  - $csvPath")
    println("  - $mdPath")
}

private fun runFullSimulation(config: SimulationConfig): SimulationSummary {
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

                        if (completedScenarios % 500 == 0) {
                            println("Progress: $completedScenarios / $totalScenarios (${(completedScenarios * 100 / totalScenarios)}%)")
                        }
                    }
                }
            }
        }
    }

    return SimulationSummary(
        totalScenarios = results.size,
        totalBattles = results.size * config.iterationsPerScenario,
        executionTimeMs = System.currentTimeMillis() - startTime,
        results = results
    )
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
