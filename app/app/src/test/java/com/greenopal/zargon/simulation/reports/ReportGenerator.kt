package com.greenopal.zargon.simulation.reports

import com.greenopal.zargon.simulation.SimulationSummary
import com.greenopal.zargon.simulation.ScenarioResult
import java.io.File

class ReportGenerator {

    fun generateConsoleReport(summary: SimulationSummary) {
        println("=== ZARGON BATTLE BALANCE SIMULATION ===")
        println("Total scenarios: ${summary.totalScenarios}")
        println("Total battles: ${summary.totalBattles}")
        println("Execution time: ${summary.executionTimeMs}ms (${summary.executionTimeMs / 1000.0}s)")
        println()

        println("=== WIN RATE BY MONSTER TYPE AND SCALING ===")
        println()

        val monsterTypes = summary.results.map { it.monsterType }.distinct().sorted()
        val scalingFactors = summary.results.map { it.monsterScalingFactor }.distinct().sorted()

        for (monsterType in monsterTypes) {
            println("Monster: $monsterType")
            println("Scale | Win Rate | Sample Size")
            println("------|----------|------------")

            for (scale in scalingFactors) {
                val scenarios = summary.results.filter {
                    it.monsterType == monsterType && it.monsterScalingFactor == scale
                }

                if (scenarios.isNotEmpty()) {
                    val avgWinRate = scenarios.map { it.winRate }.average()
                    val totalBattles = scenarios.sumOf { it.iterations }
                    println("  ${scale.toString().padEnd(3)} | ${String.format("%6.1f", avgWinRate * 100)}%  | $totalBattles battles")
                }
            }
            println()
        }
    }

    fun generateCSV(summary: SimulationSummary, outputPath: String) {
        val sb = StringBuilder()
        sb.appendLine("Level,Weapon,WeaponBonus,Armor,ArmorBonus,Monster,ScaleFactor,Iterations,Wins,Losses,WinRate,AvgTurnsWin,AvgTurnsLose,AvgPlayerHPRemaining,AvgDamageDealt,AvgDamageTaken")

        summary.results.forEach { r ->
            sb.appendLine("${r.playerLevel},${r.weapon.displayName},${r.weapon.attackBonus},${r.armor.displayName},${r.armor.defenseBonus},${r.monsterType},${r.monsterScalingFactor},${r.iterations},${r.wins},${r.losses},${r.winRate},${r.avgTurnsToWin},${r.avgTurnsToLose},${r.avgPlayerHPRemaining},${r.avgDamageDealt},${r.avgDamageTaken}")
        }

        File(outputPath).writeText(sb.toString())
        println("CSV report written to: $outputPath")
    }

    fun generateMarkdown(summary: SimulationSummary, outputPath: String) {
        val sb = StringBuilder()
        sb.appendLine("# Zargon Battle Balance Report")
        sb.appendLine()
        sb.appendLine("## Summary")
        sb.appendLine()
        sb.appendLine("- **Total Scenarios:** ${summary.totalScenarios}")
        sb.appendLine("- **Total Battles:** ${summary.totalBattles}")
        sb.appendLine("- **Execution Time:** ${summary.executionTimeMs}ms (${summary.executionTimeMs / 1000.0}s)")
        sb.appendLine()

        val dangerous = summary.results.filter { it.winRate < 0.5 }.sortedBy { it.winRate }
        sb.appendLine("## High Risk Combinations (<50% win rate)")
        sb.appendLine()
        sb.appendLine("| Level | Weapon | Armor | Monster | Scale | Win Rate | Avg Turns (Win) |")
        sb.appendLine("|-------|--------|-------|---------|-------|----------|-----------------|")
        dangerous.take(50).forEach {
            sb.appendLine("| ${it.playerLevel} | ${it.weapon.displayName} | ${it.armor.displayName} | ${it.monsterType} | ${it.monsterScalingFactor}x | ${String.format("%.1f", it.winRate * 100)}% | ${String.format("%.1f", it.avgTurnsToWin)} |")
        }
        sb.appendLine()

        val impossible = summary.results.filter { it.winRate == 0.0 }
        if (impossible.isNotEmpty()) {
            sb.appendLine("## Impossible Scenarios (0% win rate)")
            sb.appendLine()
            sb.appendLine("| Level | Weapon | Armor | Monster | Scale |")
            sb.appendLine("|-------|--------|-------|---------|-------|")
            impossible.forEach {
                sb.appendLine("| ${it.playerLevel} | ${it.weapon.displayName} | ${it.armor.displayName} | ${it.monsterType} | ${it.monsterScalingFactor}x |")
            }
            sb.appendLine()
        }

        val levelStats = summary.results.groupBy { it.playerLevel }
        sb.appendLine("## Win Rate by Player Level")
        sb.appendLine()
        sb.appendLine("| Level | Avg Win Rate | Dangerous Scenarios | Impossible Scenarios |")
        sb.appendLine("|-------|--------------|---------------------|----------------------|")
        levelStats.keys.sorted().forEach { level ->
            val scenarios = levelStats[level]!!
            val avgWinRate = scenarios.map { it.winRate }.average()
            val dangerousCount = scenarios.count { it.winRate < 0.5 }
            val impossibleCount = scenarios.count { it.winRate == 0.0 }
            sb.appendLine("| $level | ${String.format("%.1f", avgWinRate * 100)}% | $dangerousCount | $impossibleCount |")
        }

        File(outputPath).writeText(sb.toString())
        println("Markdown report written to: $outputPath")
    }
}
