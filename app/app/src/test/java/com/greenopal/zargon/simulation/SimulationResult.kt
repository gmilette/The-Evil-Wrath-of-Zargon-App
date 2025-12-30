package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.MonsterType
import com.greenopal.zargon.ui.screens.Weapon
import com.greenopal.zargon.ui.screens.Armor

data class ScenarioResult(
    val playerLevel: Int,
    val weapon: Weapon,
    val armor: Armor,
    val monsterType: MonsterType,
    val monsterScalingFactor: Int,
    val iterations: Int,
    val wins: Int,
    val losses: Int,
    val winRate: Double,
    val avgTurnsToWin: Double,
    val avgTurnsToLose: Double,
    val avgPlayerHPRemaining: Double,
    val avgDamageDealt: Double,
    val avgDamageTaken: Double
)

data class SimulationSummary(
    val totalScenarios: Int,
    val totalBattles: Int,
    val executionTimeMs: Long,
    val results: List<ScenarioResult>
)
