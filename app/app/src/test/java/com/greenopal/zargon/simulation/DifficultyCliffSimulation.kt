package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import com.greenopal.zargon.ui.screens.Armor
import com.greenopal.zargon.ui.screens.Weapon
import org.junit.Test
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * Simulates two proposed fixes for the bi-modal difficulty cliff in Zargon's battle system.
 *
 * Baseline: Player damage = totalAP (flat, deterministic)
 *           Monster damage = max(1, monsterAP - totalDefense + random(0..scalingFactor))
 *
 * Option C: Player damage = random(totalAP / 2 .. totalAP)
 *           (adds variance to player output, smearing the win-rate cliff)
 *
 * Option D: Monster damage = monsterAP * (1.0 - defense / (defense + K))
 *           where K = 30 (smooth diminishing-return defense curve, no hard floor)
 *           Player damage stays flat (same as baseline).
 */
class DifficultyCliffSimulation {

    private val ITERATIONS = 2000
    private val SEED = 42L

    // --- Equipment loadouts requested: no armor, mid armor, best armor ---
    // We map these to concrete Armor enum values.
    // No armor   = CLOTH (lowest available; 0 bonus not in enum so we use +5)
    // Mid armor  = CHAIN_MAIL (+28)
    // Best armor = PLATEMAIL (+50)
    private val armorLoadouts = listOf(
        Armor.CLOTH,       // "no armor" proxy
        Armor.CHAIN_MAIL,  // mid armor
        Armor.PLATEMAIL    // best armor
    )

    // Representative weapon per level bracket (closest to what a player would have)
    // Level 1  -> Dagger (+2)
    // Level 5  -> Long Sword (+5)
    // Level 10 -> Broad Sword (+9) … player might have this mid-end game
    private val weaponForLevel = mapOf(
        1  to Weapon.DAGGER,
        5  to Weapon.LONG_SWORD,
        10 to Weapon.BROAD_SWORD
    )

    private val targetLevels   = listOf(1, 5, 10)
    private val scalingFactors = (1..10).toList()

    // Regular (non-boss) monsters only for the per-level analysis
    private val regularMonsters = listOf(
        MonsterType.SLIME,
        MonsterType.BAT,
        MonsterType.BABBLE,
        MonsterType.SPOOK,
        MonsterType.BELETH,
        MonsterType.SKANDER_SNAKE,
        MonsterType.NECRO
    )

    // -------------------------------------------------------------------------
    // Main test entry point
    // -------------------------------------------------------------------------
    @Test
    fun `compare baseline vs option C vs option D across levels monsters and scaling`() {
        val random = Random(SEED)

        println("================================================================")
        println("DIFFICULTY CLIFF SIMULATION — Baseline vs Option C vs Option D")
        println("================================================================")
        println("Iterations per scenario : $ITERATIONS")
        println("Player levels           : $targetLevels")
        println("Scaling factors         : 1-10")
        println("Armor loadouts          : ${armorLoadouts.map { it.displayName }}")
        println("Monsters (regular)      : ${regularMonsters.map { it.displayName }}")
        println()

        // ---- Collect results across the full matrix -------------------------
        data class Key(val level: Int, val armor: Armor, val monster: MonsterType, val scale: Int)
        data class Row(
            val key: Key,
            val weapon: Weapon,
            val baselineWR: Double,
            val optionCWR: Double,
            val optionDWR: Double
        )

        val rows = mutableListOf<Row>()

        for (level in targetLevels) {
            val weapon = weaponForLevel[level]!!
            for (armor in armorLoadouts) {
                val character = buildCharacter(level, weapon, armor)
                for (monster in regularMonsters) {
                    for (scale in scalingFactors) {
                        val monsterStats = buildMonster(monster, scale)

                        val baselineWR = simulateBaseline(character, monsterStats, ITERATIONS, random)
                        val optionCWR  = simulateOptionC(character, monsterStats, ITERATIONS, random)
                        val optionDWR  = simulateOptionD(character, monsterStats, ITERATIONS, random, K = 30.0)

                        rows.add(Row(Key(level, armor, monster, scale), weapon, baselineWR, optionCWR, optionDWR))
                    }
                }
            }
        }

        // ---- Report 1: Win-rate by scaling factor (aggregate across all combos) ----
        printSectionHeader("REPORT 1 — Win-rate distribution across scaling factors (all combos averaged)")
        println("  Scaling | Baseline | Option C | Option D | C-Base | D-Base")
        println("  --------|----------|----------|----------|--------|-------")
        for (scale in scalingFactors) {
            val scaled = rows.filter { it.key.scale == scale }
            val bWR = scaled.map { it.baselineWR }.average()
            val cWR = scaled.map { it.optionCWR }.average()
            val dWR = scaled.map { it.optionDWR }.average()
            val cDelta = cWR - bWR
            val dDelta = dWR - bWR
            println("  scale=%-2d | %6.1f%% | %6.1f%% | %6.1f%% | %+6.1f%% | %+6.1f%%"
                .format(scale, bWR * 100, cWR * 100, dWR * 100, cDelta * 100, dDelta * 100))
        }
        println()

        // ---- Report 2: Mid-game 70-90% target band achievability ----------------
        printSectionHeader("REPORT 2 — 70-90% target win-rate band (level 5, mid armor = Chain Mail)")
        val midGame = rows.filter { it.key.level == 5 && it.key.armor == Armor.CHAIN_MAIL }
        println("  Monster        | Scale | Baseline | Option C | Option D")
        println("  ---------------|-------|----------|----------|----------")
        for (row in midGame.sortedWith(compareBy({ it.key.monster.name }, { it.key.scale }))) {
            val bFlag = bandFlag(row.baselineWR)
            val cFlag = bandFlag(row.optionCWR)
            val dFlag = bandFlag(row.optionDWR)
            println("  %-14s | %5d | %6.1f%%%s | %6.1f%%%s | %6.1f%%%s"
                .format(row.key.monster.displayName, row.key.scale,
                    row.baselineWR * 100, bFlag,
                    row.optionCWR * 100, cFlag,
                    row.optionDWR * 100, dFlag))
        }
        println()
        println("  * = in 70-90% target band")
        println()

        // ---- Report 3: Cliff detection — how abrupt is each transition? ---------
        printSectionHeader("REPORT 3 — Cliff analysis (largest single-step win-rate drop per monster×armor×level)")
        println("  Measures the biggest single-increment drop in win rate across scaling factors.")
        println("  A smaller max drop = smoother difficulty curve.")
        println()
        println("  Level | Armor           | Monster        | Baseline max drop | C max drop | D max drop")
        println("  ------|-----------------|----------------|-------------------|------------|----------")

        for (level in targetLevels) {
            for (armor in armorLoadouts) {
                for (monster in regularMonsters) {
                    val series = rows.filter { it.key.level == level && it.key.armor == armor && it.key.monster == monster }
                        .sortedBy { it.key.scale }
                    if (series.size < 2) continue
                    val bMaxDrop = maxDrop(series.map { it.baselineWR })
                    val cMaxDrop = maxDrop(series.map { it.optionCWR })
                    val dMaxDrop = maxDrop(series.map { it.optionDWR })
                    println("  %5d | %-15s | %-14s | %17.1f%% | %10.1f%% | %10.1f%%"
                        .format(level, armor.displayName, monster.displayName,
                            bMaxDrop * 100, cMaxDrop * 100, dMaxDrop * 100))
                }
            }
        }
        println()

        // ---- Report 4: High-defense unkillability check (Option D only) ---------
        printSectionHeader("REPORT 4 — High-defense unkillability check (Option D, level 10 + Platemail)")
        println("  Does Option D make best-armor characters unkillable?")
        println()
        val highDefRows = rows.filter { it.key.level == 10 && it.key.armor == Armor.PLATEMAIL }
        println("  Monster        | Scale | D win rate | Avg dmg/hit (raw)")
        println("  ---------------|-------|------------|------------------")
        for (row in highDefRows.sortedWith(compareBy({ it.key.monster.name }, { it.key.scale }))) {
            val char = buildCharacter(10, weaponForLevel[10]!!, Armor.PLATEMAIL)
            val mon  = buildMonster(row.key.monster, row.key.scale)
            val avgDmgD = avgMonsterDamageOptionD(mon, char, K = 30.0)
            println("  %-14s | %5d | %9.1f%% | %.2f"
                .format(row.key.monster.displayName, row.key.scale,
                    row.optionDWR * 100, avgDmgD))
        }
        println()

        // ---- Report 5: Summary table per level, averaged across monsters/armor --
        printSectionHeader("REPORT 5 — Win-rate summary by level (averaged across all monsters and armor loadouts)")
        println("  Level | Scaling | Baseline | Option C | Option D")
        println("  ------|---------|----------|----------|----------")
        for (level in targetLevels) {
            for (scale in scalingFactors) {
                val sub = rows.filter { it.key.level == level && it.key.scale == scale }
                val bWR = sub.map { it.baselineWR }.average()
                val cWR = sub.map { it.optionCWR }.average()
                val dWR = sub.map { it.optionDWR }.average()
                println("  %5d | %7d | %6.1f%% | %6.1f%% | %6.1f%%"
                    .format(level, scale, bWR * 100, cWR * 100, dWR * 100))
            }
        }
        println()

        // ---- Report 6: Cliff smoothness score summary ---------------------------
        printSectionHeader("REPORT 6 — Overall cliff smoothness scores")
        val allBDrops = mutableListOf<Double>()
        val allCDrops = mutableListOf<Double>()
        val allDDrops = mutableListOf<Double>()
        for (level in targetLevels) {
            for (armor in armorLoadouts) {
                for (monster in regularMonsters) {
                    val series = rows.filter { it.key.level == level && it.key.armor == armor && it.key.monster == monster }
                        .sortedBy { it.key.scale }
                    if (series.size < 2) continue
                    allBDrops.add(maxDrop(series.map { it.baselineWR }))
                    allCDrops.add(maxDrop(series.map { it.optionCWR }))
                    allDDrops.add(maxDrop(series.map { it.optionDWR }))
                }
            }
        }
        println("  Metric                    | Baseline | Option C | Option D")
        println("  --------------------------|----------|----------|----------")
        println("  Avg max single-step drop  | %6.1f%% | %6.1f%% | %6.1f%%"
            .format(allBDrops.average() * 100, allCDrops.average() * 100, allDDrops.average() * 100))
        println("  Max single-step drop ever | %6.1f%% | %6.1f%% | %6.1f%%"
            .format(allBDrops.max() * 100, allCDrops.max() * 100, allDDrops.max() * 100))
        println()

        // ---- Report 7: Scenarios in 70-90% target band --------------------------
        printSectionHeader("REPORT 7 — Proportion of scenarios in the 70-90% target win-rate band")
        val total = rows.size.toDouble()
        val bInBand = rows.count { it.baselineWR in 0.70..0.90 } / total
        val cInBand = rows.count { it.optionCWR  in 0.70..0.90 } / total
        val dInBand = rows.count { it.optionDWR  in 0.70..0.90 } / total
        println("  Baseline : ${(bInBand * 100).roundToInt()}% of scenarios fall in 70-90% band")
        println("  Option C : ${(cInBand * 100).roundToInt()}% of scenarios fall in 70-90% band")
        println("  Option D : ${(dInBand * 100).roundToInt()}% of scenarios fall in 70-90% band")
        println()

        println("================================================================")
        println("END OF SIMULATION")
        println("================================================================")
    }

    // -------------------------------------------------------------------------
    // Battle simulators — three variants
    // -------------------------------------------------------------------------

    /** Baseline: player damage flat totalAP, monster damage max(1, AP - defense + rand(0..scale)) */
    private fun simulateBaseline(
        character: CharacterStats,
        monster: MonsterStats,
        iterations: Int,
        random: Random
    ): Double {
        var wins = 0
        repeat(iterations) {
            var playerHP = character.currentHP
            var monsterHP = monster.currentHP
            while (playerHP > 0 && monsterHP > 0) {
                monsterHP -= character.totalAP
                if (monsterHP <= 0) { wins++; break }
                val rand = random.nextInt(0, monster.scalingFactor + 1)
                val dmg = maxOf(1, monster.attackPower - character.totalDefense + rand)
                playerHP -= dmg
            }
        }
        return wins.toDouble() / iterations
    }

    /** Option C: player damage = random(totalAP/2 .. totalAP), monster damage unchanged */
    private fun simulateOptionC(
        character: CharacterStats,
        monster: MonsterStats,
        iterations: Int,
        random: Random
    ): Double {
        val minDmg = maxOf(1, character.totalAP / 2)
        val maxDmg = maxOf(minDmg, character.totalAP)
        var wins = 0
        repeat(iterations) {
            var playerHP = character.currentHP
            var monsterHP = monster.currentHP
            while (playerHP > 0 && monsterHP > 0) {
                val playerDmg = if (maxDmg > minDmg) random.nextInt(minDmg, maxDmg + 1) else minDmg
                monsterHP -= playerDmg
                if (monsterHP <= 0) { wins++; break }
                val rand = random.nextInt(0, monster.scalingFactor + 1)
                val dmg = maxOf(1, monster.attackPower - character.totalDefense + rand)
                playerHP -= dmg
            }
        }
        return wins.toDouble() / iterations
    }

    /**
     * Option D: monster damage uses smooth formula, no hard floor of 1.
     *   monsterDmg = monsterAP * (1.0 - defense / (defense + K))
     * Player damage stays flat (same as baseline).
     * We round to nearest int and apply a floor of 0 (monster can do 0 damage if defense is very high).
     */
    private fun simulateOptionD(
        character: CharacterStats,
        monster: MonsterStats,
        iterations: Int,
        random: Random,
        K: Double
    ): Double {
        val defense = character.totalDefense.toDouble()
        val monAP   = monster.attackPower.toDouble()
        val scale   = monster.scalingFactor
        // Base damage before random component
        val baseDmgD = monAP * (1.0 - defense / (defense + K))
        var wins = 0
        repeat(iterations) {
            var playerHP = character.currentHP
            var monsterHP = monster.currentHP
            while (playerHP > 0 && monsterHP > 0) {
                monsterHP -= character.totalAP
                if (monsterHP <= 0) { wins++; break }
                // Random component: same spirit as baseline — add a fraction of the scaling noise
                // We preserve the existing random(0..scalingFactor) additive component
                // but apply it to the smooth base so variance still exists.
                val rand = if (scale > 0) random.nextInt(0, scale + 1) else 0
                // The random component is scaled proportionally to baseDmgD to avoid dominating
                val dmg = maxOf(0, (baseDmgD + rand).roundToInt())
                playerHP -= dmg
            }
        }
        return wins.toDouble() / iterations
    }

    /** Compute average monster damage per hit under Option D (for unkillability report) */
    private fun avgMonsterDamageOptionD(monster: MonsterStats, character: CharacterStats, K: Double): Double {
        val defense = character.totalDefense.toDouble()
        val monAP   = monster.attackPower.toDouble()
        return monAP * (1.0 - defense / (defense + K))
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun buildCharacter(level: Int, weapon: Weapon, armor: Armor): CharacterStats {
        val baseAP = 5 + (level - 1) * 3
        val baseDP = 20 + (level - 1) * 4
        val maxHP = 20 + (level - 1) * 5
        val baseMP = 10 + (level - 1) * 4
        return CharacterStats(
            baseAP      = baseAP,
            baseDP      = baseDP,
            maxHP       = maxHP,
            currentHP   = maxHP,
            baseMP      = baseMP,
            currentMP   = baseMP,
            level       = level,
            weaponBonus = weapon.attackBonus,
            armorBonus  = armor.defenseBonus
        )
    }

    private fun buildMonster(type: MonsterType, scalingFactor: Int): MonsterStats {
        return MonsterStats(
            type          = type,
            attackPower   = type.baseAP * scalingFactor,
            currentHP     = type.baseDP * scalingFactor,
            maxHP         = type.baseDP * scalingFactor,
            scalingFactor = scalingFactor
        )
    }

    private fun maxDrop(winRates: List<Double>): Double {
        if (winRates.size < 2) return 0.0
        return (0 until winRates.size - 1).maxOf { winRates[it] - winRates[it + 1] }.coerceAtLeast(0.0)
    }

    private fun bandFlag(wr: Double) = if (wr in 0.70..0.90) "*" else " "

    private fun printSectionHeader(title: String) {
        println("--- $title ---")
    }
}
