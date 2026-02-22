package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import org.junit.Test
import kotlin.random.Random

/**
 * Canonical K-value simulation for the hyperbolic damage formula.
 *
 * Formula under test:
 *   damage = max(1, (monsterAP * K / (totalDefense + K) * randomMultiplier).toInt())
 *   where randomMultiplier = Uniform(0.84, 1.16)
 *
 * Canonical player loadouts (exact equipment from design spec):
 *   Level 1-2:  Dagger (+5 AP),           Cloth armor (+5 def)       -> totalAP = baseAP+5, totalDef = baseDP+5
 *   Level 3-4:  Short Sword (+8 AP),       Leather (+8 def)
 *   Level 5-6:  Long Sword (+13 AP),       Plated Leather (+15 def)
 *   Level 7-8:  Sword of Thorns (+18 AP),  Spiked Leather (+20 def)
 *   Level 9-10: Broad Sword (+23 AP),      Chain Mail (+30 def)
 *   Level 11+:  Two-Handed Sword (+28 AP), Platemail (+42 def)
 *
 * Character stats (post-decoupling):
 *   baseDP (defense): 20 + (level-1)*4
 *   maxHP:            20 + (level-1)*5
 *   baseAP:           5 + (level-1)*3  (approximate average — grows ~2-4/level)
 *
 * Monster scaling:
 *   scalingFactor starts at 1; for each level above 1 there is a 50% chance to increment.
 *   Expected scalingFactor ~ 1 + (level-1)*0.5
 *
 * Simulation goals:
 *   Q1: Which K value (15, 20, 30, 40) gives 70-90% win rates at expected level/equipment,
 *       low-moderate scaling (1-4)?
 *   Q2: Which monsters are too strong/weak at their canonical levels?
 *   Q3: Should max scaling factor be capped?
 */
class CanonicalKSimulation {

    companion object {
        private const val ITERATIONS = 2000
        private val SEED = 42L

        private val K_VALUES = listOf(15, 20, 30, 40)

        /**
         * Canonical equipment data class.
         * weaponAP and armorDef are the BONUS values (added to base stats).
         */
        data class CanonicalLoadout(
            val label: String,
            val levelRange: IntRange,
            val weaponAP: Int,
            val armorDef: Int
        )

        val CANONICAL_LOADOUTS = listOf(
            CanonicalLoadout("Dagger / Cloth",          1..2,  weaponAP = 5,  armorDef = 5),
            CanonicalLoadout("Short Sword / Leather",   3..4,  weaponAP = 8,  armorDef = 8),
            CanonicalLoadout("Long Sword / Plated",     5..6,  weaponAP = 13, armorDef = 15),
            CanonicalLoadout("Sword of Thorns / Spike", 7..8,  weaponAP = 18, armorDef = 20),
            CanonicalLoadout("Broad Sword / Chain",     9..10, weaponAP = 23, armorDef = 30),
            CanonicalLoadout("2H Sword / Platemail",    11..13, weaponAP = 28, armorDef = 42)
        )

        // Representative level for each canonical loadout (midpoint)
        val CANONICAL_LEVELS = listOf(1, 3, 5, 7, 9, 11)

        // Low-moderate scaling: 1-4 (expected encounter range for mid-level players)
        // High scaling: 6-8+ (stress test)
        val LOW_MOD_SCALING = 1..4
        val HIGH_SCALING    = 6..8

        // Regular (non-boss) monsters and their expected encounter levels
        data class MonsterProfile(
            val type: MonsterType,
            val expectedLevelRange: IntRange   // levels where this monster is commonly encountered
        )

        val MONSTER_PROFILES = listOf(
            MonsterProfile(MonsterType.SLIME,         1..4),
            MonsterProfile(MonsterType.BAT,           1..4),
            MonsterProfile(MonsterType.BABBLE,        1..5),
            MonsterProfile(MonsterType.SPOOK,         1..6),
            MonsterProfile(MonsterType.BELETH,        2..7),
            MonsterProfile(MonsterType.SKANDER_SNAKE, 5..10),
            MonsterProfile(MonsterType.NECRO,         6..11)
        )
    }

    // -------------------------------------------------------------------------
    // Character builder — uses design-spec baseDP and maxHP
    // -------------------------------------------------------------------------

    private fun buildCharacter(level: Int, weaponAP: Int, armorDef: Int): CharacterStats {
        val baseDP = 20 + (level - 1) * 4
        val maxHP  = 20 + (level - 1) * 5
        val baseAP = 5  + (level - 1) * 3
        return CharacterStats(
            baseAP    = baseAP,
            baseDP    = baseDP,
            maxHP     = maxHP,
            currentHP = maxHP,
            baseMP    = 10,
            currentMP = 10,
            level     = level,
            weaponBonus = weaponAP,
            armorBonus  = armorDef
        )
    }

    // -------------------------------------------------------------------------
    // Monster builder
    // -------------------------------------------------------------------------

    private fun buildMonster(type: MonsterType, scalingFactor: Int): MonsterStats {
        return MonsterStats(
            type          = type,
            attackPower   = type.baseAP * scalingFactor,
            currentHP     = type.baseDP * scalingFactor,
            maxHP         = type.baseDP * scalingFactor,
            scalingFactor = scalingFactor
        )
    }

    // -------------------------------------------------------------------------
    // Battle simulator — new formula
    // damage = max(1, (monsterAP * K / (totalDefense + K) * randomMultiplier).toInt())
    // randomMultiplier = Uniform(0.84, 1.16)
    // -------------------------------------------------------------------------

    private fun simulateBattleNewFormula(
        character: CharacterStats,
        monster: MonsterStats,
        k: Int,
        random: Random
    ): Boolean {
        var playerHP  = character.currentHP
        var monsterHP = monster.currentHP

        val playerDmgPerHit = character.totalAP
        val totalDefense    = character.totalDefense.toDouble()
        val monsterAP       = monster.attackPower.toDouble()
        val kd              = k.toDouble()

        while (playerHP > 0 && monsterHP > 0) {
            monsterHP -= playerDmgPerHit
            if (monsterHP <= 0) break

            // New hyperbolic formula with ±16% multiplicative variance
            val randomMultiplier = 0.84 + random.nextDouble() * 0.32   // uniform [0.84, 1.16]
            val rawDamage = monsterAP * kd / (totalDefense + kd) * randomMultiplier
            val damage = maxOf(1, rawDamage.toInt())
            playerHP -= damage
        }

        return playerHP > 0
    }

    // -------------------------------------------------------------------------
    // Old formula (baseline for comparison)
    // damage = max(1, monsterAP - totalDefense + random(0..scalingFactor))
    // -------------------------------------------------------------------------

    private fun simulateBattleOldFormula(
        character: CharacterStats,
        monster: MonsterStats,
        random: Random
    ): Boolean {
        var playerHP  = character.currentHP
        var monsterHP = monster.currentHP

        val playerDmgPerHit = character.totalAP
        val totalDefense    = character.totalDefense

        while (playerHP > 0 && monsterHP > 0) {
            monsterHP -= playerDmgPerHit
            if (monsterHP <= 0) break

            val rand   = random.nextInt(0, monster.scalingFactor + 1)
            val damage = maxOf(1, monster.attackPower - totalDefense + rand)
            playerHP -= damage
        }

        return playerHP > 0
    }

    // -------------------------------------------------------------------------
    // Q1: K-value comparison at canonical level/equipment, low-moderate scaling
    // -------------------------------------------------------------------------

    @Test
    fun `Q1 K value comparison at canonical loadouts low-moderate scaling`() {
        val random = Random(SEED)

        println("=".repeat(90))
        println("Q1: K-VALUE COMPARISON — CANONICAL LOADOUTS, SCALING 1-4")
        println("Formula: damage = max(1, (monsterAP * K / (totalDefense + K) * rand[0.84,1.16]).toInt())")
        println("=".repeat(90))
        println()
        println("Design goal: 70-90% win rate at low-moderate scaling (1-4) with expected equipment.")
        println()

        data class KResult(val k: Int, val winRates: Map<String, Double>)

        val allKResults = mutableListOf<KResult>()

        // For Q1 we test at the midpoint level of each loadout, scaling 1-4
        for (k in K_VALUES) {
            val kRandom = Random(SEED)
            val winRatesByScenario = mutableMapOf<String, Double>()

            for (loadout in CANONICAL_LOADOUTS) {
                val level = loadout.levelRange.first  // Use lowest level in range (hardest case)
                val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)

                for (scalingFactor in LOW_MOD_SCALING) {
                    for (mProfile in MONSTER_PROFILES) {
                        // Only test monsters valid at this level
                        if (level !in mProfile.expectedLevelRange) continue

                        val monster = buildMonster(mProfile.type, scalingFactor)
                        val wins = (1..ITERATIONS).count {
                            simulateBattleNewFormula(character, monster, k, kRandom)
                        }
                        val wr = wins.toDouble() / ITERATIONS
                        val key = "${loadout.label} | L$level | ${mProfile.type.displayName} x$scalingFactor"
                        winRatesByScenario[key] = wr
                    }
                }
            }

            allKResults.add(KResult(k, winRatesByScenario))
        }

        // Print summary: for each K, how many scenarios fall in 70-90% band?
        println("%-6s | %-10s | %-15s | %-15s | %-15s".format(
            "K", "In 70-90%", "Avg Win Rate", "Too Easy >90%", "Too Hard <70%"))
        println("-".repeat(72))
        for (kr in allKResults) {
            val rates = kr.winRates.values
            val inBand   = rates.count { it in 0.70..0.90 }
            val tooEasy  = rates.count { it > 0.90 }
            val tooHard  = rates.count { it < 0.70 }
            val total    = rates.size
            val avg      = rates.average()
            println("%-6d | %6d/%-3d | %13.1f%% | %13d | %13d".format(
                kr.k, inBand, total, avg * 100, tooEasy, tooHard))
        }
        println()

        // Per-loadout breakdown for each K
        println("Per-loadout win rates averaged over scaling 1-4 and valid monsters:")
        println()

        val kHeaders = K_VALUES.joinToString(" | ") { "K=%-5d".format(it) }
        val scenarioHeader = "%-45s".format("Scenario")
        println("$scenarioHeader | $kHeaders | OLD")
        println("-".repeat(80))

        // Collect old formula results too
        val oldRandom = Random(SEED)
        val oldWinRates = mutableMapOf<String, Double>()

        for (loadout in CANONICAL_LOADOUTS) {
            val level = loadout.levelRange.first
            val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)

            for (scalingFactor in LOW_MOD_SCALING) {
                for (mProfile in MONSTER_PROFILES) {
                    if (level !in mProfile.expectedLevelRange) continue
                    val monster = buildMonster(mProfile.type, scalingFactor)
                    val wins = (1..ITERATIONS).count {
                        simulateBattleOldFormula(character, monster, oldRandom)
                    }
                    val key = "${loadout.label} | L$level | ${mProfile.type.displayName} x$scalingFactor"
                    oldWinRates[key] = wins.toDouble() / ITERATIONS
                }
            }
        }

        // Print per-loadout summary (averaged over scaling and monsters)
        for (loadout in CANONICAL_LOADOUTS) {
            val level = loadout.levelRange.first
            val totalDefense = (20 + (level - 1) * 4) + loadout.armorDef
            val totalAP      = (5  + (level - 1) * 3) + loadout.weaponAP

            val kRates = K_VALUES.map { k ->
                val kr = allKResults.find { it.k == k }!!
                val relevant = kr.winRates.filter { it.key.startsWith(loadout.label) }
                if (relevant.isNotEmpty()) relevant.values.average() else 0.0
            }
            val oldAvg = oldWinRates.filter { it.key.startsWith(loadout.label) }.values
                .let { if (it.isNotEmpty()) it.average() else 0.0 }

            val kStr = kRates.joinToString(" | ") { "%5.1f%%".format(it * 100) }
            val oldStr = "%5.1f%%".format(oldAvg * 100)
            val rowLabel = "%-45s".format("${loadout.label} (L$level, def=$totalDefense, ap=$totalAP)")
            println("$rowLabel | $kStr | $oldStr")
        }
        println()

        // Win rate table per K: level vs scaling factor (averaged over all valid monsters)
        for (k in K_VALUES) {
            println("--- K=$k: Win rates by level and scaling factor (avg over valid monsters) ---")
            println("%-12s | %-8s | %-8s | %-8s | %-8s | %-8s | %-8s | %-8s | %-8s".format(
                "Loadout", "Scale=1", "Scale=2", "Scale=3", "Scale=4", "Scale=5", "Scale=6", "Scale=7", "Scale=8"))
            println("-".repeat(90))

            val kr = allKResults.find { it.k == k }!!

            // We need all scaling factors for this sub-report; run extra sims
            val kRandom2 = Random(SEED)
            for (loadout in CANONICAL_LOADOUTS) {
                val level = loadout.levelRange.first
                val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)

                val scaleRates = (1..8).map { scale ->
                    val validMonsters = MONSTER_PROFILES.filter { level in it.expectedLevelRange }
                    if (validMonsters.isEmpty()) return@map 0.0
                    val rates = validMonsters.map { mp ->
                        val monster = buildMonster(mp.type, scale)
                        val wins = (1..ITERATIONS).count { simulateBattleNewFormula(character, monster, k, kRandom2) }
                        wins.toDouble() / ITERATIONS
                    }
                    rates.average()
                }

                val cells = scaleRates.joinToString(" | ") { rate ->
                    val flag = when {
                        rate >= 0.90 -> "E"  // too Easy
                        rate >= 0.70 -> "."  // in band
                        rate >= 0.50 -> "H"  // too Hard
                        else         -> "X"  // danger zone
                    }
                    "%5.1f%%$flag".format(rate * 100)
                }
                val loadoutLabel = "%-12s".format(loadout.label.take(12))
                println("$loadoutLabel | $cells")
            }
            println("  Legend: . = 70-90% target | E = >90% too easy | H = 50-70% too hard | X = <50% danger")
            println()
        }
    }

    // -------------------------------------------------------------------------
    // Q2: Monster strength audit at expected encounter levels
    // -------------------------------------------------------------------------

    @Test
    fun `Q2 monster strength audit at expected encounter levels`() {
        println("=".repeat(90))
        println("Q2: MONSTER STRENGTH AUDIT — each monster at its expected level range, canonical gear")
        println("=".repeat(90))
        println()
        println("Win rate target: 70-90% at scaling 1-4")
        println()

        val bestK = 30   // Anticipated best K based on prior simulations

        data class MonsterAuditRow(
            val monster: MonsterType,
            val level: Int,
            val scalingFactor: Int,
            val winRate: Double,
            val avgMonsterDmgPerHit: Double,
            val turnsToKillPlayer: Double,
            val turnsToKillMonster: Double
        )

        val rows = mutableListOf<MonsterAuditRow>()
        val random = Random(SEED)

        for (mp in MONSTER_PROFILES) {
            // Test at the minimum encounter level (hardest for player) and midpoint
            val testLevels = listOf(mp.expectedLevelRange.first, (mp.expectedLevelRange.first + mp.expectedLevelRange.last) / 2)
                .distinct()

            for (level in testLevels) {
                val loadout = CANONICAL_LOADOUTS.find { level in it.levelRange }
                    ?: CANONICAL_LOADOUTS.last()

                val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)
                val totalDefense = character.totalDefense.toDouble()
                val totalAP      = character.totalAP

                for (scale in 1..6) {
                    val monster = buildMonster(mp.type, scale)

                    val wins = (1..ITERATIONS).count {
                        simulateBattleNewFormula(character, monster, bestK, random)
                    }
                    val wr = wins.toDouble() / ITERATIONS

                    // Analytical average damage per hit (K=30, mid-multiplier=1.0)
                    val avgDmg = monster.attackPower.toDouble() * bestK / (totalDefense + bestK)

                    // Approximate turns (analytical)
                    val turnsToKillPlayer  = if (avgDmg > 0) character.maxHP / avgDmg else 9999.0
                    val turnsToKillMonster = if (totalAP > 0) monster.maxHP.toDouble() / totalAP else 9999.0

                    rows.add(MonsterAuditRow(mp.type, level, scale, wr, avgDmg, turnsToKillPlayer, turnsToKillMonster))
                }
            }
        }

        // Print per-monster tables
        for (mp in MONSTER_PROFILES) {
            println("Monster: ${mp.type.displayName}  (baseAP=${mp.type.baseAP}, baseHP=${mp.type.baseDP}, minLevel=${mp.type.minLevel})")
            println("  Expected encounter levels: ${mp.expectedLevelRange}")
            println()
            println("  %-8s | %-8s | %-8s | %-12s | %-16s | %-18s | %-18s | %-8s".format(
                "Level", "Scale", "WinRate", "MonAP(scaled)", "TotalDefense", "AvgMonDmg/Hit", "Turns:Kill Mon", "Turns:Die"))
            println("  " + "-".repeat(100))

            for (row in rows.filter { it.monster == mp.type }.sortedWith(compareBy({ it.level }, { it.scalingFactor }))) {
                val loadout = CANONICAL_LOADOUTS.find { row.level in it.levelRange } ?: CANONICAL_LOADOUTS.last()
                val character = buildCharacter(row.level, loadout.weaponAP, loadout.armorDef)
                val monAP = mp.type.baseAP * row.scalingFactor
                val flag = when {
                    row.winRate > 0.90 -> " [TOO EASY]"
                    row.winRate in 0.70..0.90 -> " [OK]"
                    row.winRate in 0.50..0.70 -> " [TOO HARD]"
                    else -> " [DANGER]"
                }
                println("  %-8d | %-8d | %6.1f%%%s | %-13d | %-16d | %-18.1f | %-18.1f | %-8.1f".format(
                    row.level, row.scalingFactor, row.winRate * 100, flag,
                    monAP, character.totalDefense,
                    row.avgMonsterDmgPerHit, row.turnsToKillMonster, row.turnsToKillPlayer))
            }
            println()
        }

        // Summary: problematic monsters
        println("=".repeat(90))
        println("MONSTER AUDIT SUMMARY (K=$bestK, scaling 1-4, at minimum encounter level)")
        println("=".repeat(90))
        println()
        println("%-18s | %-8s | %-8s | %-10s | %-10s | %-12s".format(
            "Monster", "BaseAP", "BaseHP", "Sc1 WR%", "Sc4 WR%", "Assessment"))
        println("-".repeat(80))

        for (mp in MONSTER_PROFILES) {
            val minLevel = mp.expectedLevelRange.first
            val sc1 = rows.find { it.monster == mp.type && it.level == minLevel && it.scalingFactor == 1 }
            val sc4 = rows.find { it.monster == mp.type && it.level == minLevel && it.scalingFactor == 4 }

            val sc1Rate = sc1?.winRate ?: 0.0
            val sc4Rate = sc4?.winRate ?: 0.0

            val assessment = when {
                sc1Rate > 0.95 && sc4Rate > 0.90 -> "Too weak — barely threatens player"
                sc1Rate > 0.90 -> "Slightly weak at low scaling"
                sc1Rate in 0.70..0.90 && sc4Rate in 0.50..0.85 -> "Well tuned"
                sc1Rate in 0.70..0.90 && sc4Rate < 0.50 -> "Moderate — steep cliff at scale 4"
                sc1Rate < 0.60 -> "Too strong at base"
                else -> "OK"
            }

            println("%-18s | %-8d | %-8d | %8.1f%% | %8.1f%% | %s".format(
                mp.type.displayName, mp.type.baseAP, mp.type.baseDP,
                sc1Rate * 100, sc4Rate * 100, assessment))
        }
        println()
    }

    // -------------------------------------------------------------------------
    // Q3: Scaling cap analysis
    // -------------------------------------------------------------------------

    @Test
    fun `Q3 scaling cap analysis — win rates at high scaling factors`() {
        println("=".repeat(90))
        println("Q3: SCALING CAP ANALYSIS — should max scaling be capped?")
        println("=".repeat(90))
        println()
        println("Expected scaling factor distribution (50% chance per level above 1):")
        for (level in listOf(1, 3, 5, 7, 9, 11)) {
            val expectedScale = 1.0 + (level - 1) * 0.5
            val p90scale = 1 + ((level - 1) * 0.75).toInt()  // rough 90th percentile
            println("  Level $level: expected scale ~${"%.1f".format(expectedScale)}, 90th pct ~$p90scale")
        }
        println()

        val bestK = 30
        val random = Random(SEED)

        // Test all regular monsters at scaling factors 1-12 for key levels
        println("Win rates at high scaling (K=$bestK), canonical gear at representative levels:")
        println()

        val testLevels = listOf(3, 5, 7, 9)

        println("%-18s | %-6s | %s".format("Monster", "Level", (1..12).joinToString(" | ") { "Sc%2d".format(it) }))
        println("-".repeat(100))

        for (level in testLevels) {
            val loadout = CANONICAL_LOADOUTS.find { level in it.levelRange } ?: CANONICAL_LOADOUTS.last()
            val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)

            for (mp in MONSTER_PROFILES) {
                if (level !in mp.expectedLevelRange) continue

                val rates = (1..12).map { scale ->
                    val monster = buildMonster(mp.type, scale)
                    val wins = (1..ITERATIONS).count { simulateBattleNewFormula(character, monster, bestK, random) }
                    wins.toDouble() / ITERATIONS
                }

                val rateStr = rates.joinToString(" | ") { rate ->
                    val flag = when {
                        rate > 0.90 -> "E"
                        rate > 0.70 -> "."
                        rate > 0.40 -> "H"
                        else -> "X"
                    }
                    "%3.0f%%%s".format(rate * 100, flag)
                }
                println("%-18s | %-6d | %s".format(mp.type.displayName, level, rateStr))
            }
            println()
        }

        println("Legend: E=>90% too easy | .=70-90% target | H=40-70% hard | X=<40% near-impossible")
        println()

        // Cap recommendation
        println("=".repeat(90))
        println("SCALING CAP ANALYSIS SUMMARY")
        println("=".repeat(90))
        println()
        println("Expected maximum scaling factor by level (99th percentile of binomial(level-1, 0.5)):")
        println("  Level 1:  max scale = 1")
        println("  Level 3:  max scale = 3 (rarely 4)")
        println("  Level 5:  max scale = 5 (rarely 6)")
        println("  Level 7:  max scale = 7 (rarely 8)")
        println("  Level 9:  max scale = 9 (rarely 10)")
        println("  Level 11: max scale = 11 (rarely 12)")
        println()
        println("With uncapped scaling, a level-5 player can face scale-8+ monsters (~1% of encounters).")
        println("With K=30 and canonical gear, win rates at extreme scaling are evaluated above.")
        println()

        // Find the exact scale where win rate drops below 40% for each level+monster combo
        println("Scale threshold where win rate first drops below 40% (near-impossible) at K=$bestK:")
        println("%-18s | %-6s | %-12s | %-12s".format("Monster", "Level", "Threshold", "Win Rate at threshold"))
        println("-".repeat(60))

        val dangerCases = mutableListOf<Triple<MonsterType, Int, Int>>()  // monster, level, threshold scale
        for (level in testLevels) {
            val loadout = CANONICAL_LOADOUTS.find { level in it.levelRange } ?: CANONICAL_LOADOUTS.last()
            val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)

            for (mp in MONSTER_PROFILES) {
                if (level !in mp.expectedLevelRange) continue
                val dangerScale = (1..12).firstOrNull { scale ->
                    val monster = buildMonster(mp.type, scale)
                    val wins = (1..ITERATIONS).count { simulateBattleNewFormula(character, monster, bestK, random) }
                    wins.toDouble() / ITERATIONS < 0.40
                }
                if (dangerScale != null) {
                    dangerCases.add(Triple(mp.type, level, dangerScale))
                    val monster = buildMonster(mp.type, dangerScale)
                    val wins = (1..ITERATIONS).count { simulateBattleNewFormula(character, monster, bestK, random) }
                    val wr = wins.toDouble() / ITERATIONS
                    println("%-18s | %-6d | %-12d | %.1f%%".format(mp.type.displayName, level, dangerScale, wr * 100))
                }
            }
        }
        println()

        if (dangerCases.isNotEmpty()) {
            val minDangerScale = dangerCases.minOf { it.third }
            println("Earliest danger-zone scale: $minDangerScale")
            println()
            if (minDangerScale <= 6) {
                println("RECOMMENDATION: Cap maximum scaling factor at 6 for regular monsters.")
                println("This eliminates near-unwinnable encounters while preserving late-game tension.")
            } else {
                println("RECOMMENDATION: Cap maximum scaling factor at ${minDangerScale - 1}.")
                println("Encounters above that scale become effectively unwinnable for appropriately-equipped players.")
            }
        } else {
            println("No near-impossible scenarios found in range 1-12. Scaling cap may not be needed.")
        }
        println()
    }

    // -------------------------------------------------------------------------
    // Q4: Head-to-head comparison — old formula vs chosen K for key scenarios
    // -------------------------------------------------------------------------

    @Test
    fun `Q4 head-to-head old formula vs K=20 and K=30 for representative scenarios`() {
        println("=".repeat(90))
        println("Q4: HEAD-TO-HEAD — Old formula vs K=20 vs K=30 for representative scenarios")
        println("=".repeat(90))
        println()

        data class Scenario(
            val label: String,
            val level: Int,
            val weaponAP: Int,
            val armorDef: Int,
            val monsterType: MonsterType,
            val scalingFactor: Int
        )

        val scenarios = listOf(
            // Early game
            Scenario("Early: L1 Dagger/Cloth vs Slime x1",        1,  5,  5,  MonsterType.SLIME,         1),
            Scenario("Early: L1 Dagger/Cloth vs Babble x2",       1,  5,  5,  MonsterType.BABBLE,        2),
            Scenario("Early: L1 Dagger/Cloth vs Spook x3",        1,  5,  5,  MonsterType.SPOOK,         3),
            Scenario("Early: L2 Dagger/Cloth vs Beleth x2",       2,  5,  5,  MonsterType.BELETH,        2),
            // Mid game
            Scenario("Mid: L5 LongSword/Plated vs SkanderSnake x3", 5, 13, 15, MonsterType.SKANDER_SNAKE, 3),
            Scenario("Mid: L5 LongSword/Plated vs Necro x2",       5, 13, 15, MonsterType.NECRO,         2),
            Scenario("Mid: L5 LongSword/Plated vs Beleth x4",      5, 13, 15, MonsterType.BELETH,        4),
            // Late-mid
            Scenario("Late-Mid: L7 SwordOfThorns/Spike vs Necro x4", 7, 18, 20, MonsterType.NECRO,       4),
            Scenario("Late-Mid: L7 SwordOfThorns/Spike vs Necro x6", 7, 18, 20, MonsterType.NECRO,       6),
            // High scaling stress
            Scenario("Stress: L9 BroadSword/Chain vs Necro x6",    9, 23, 30, MonsterType.NECRO,         6),
            Scenario("Stress: L9 BroadSword/Chain vs Necro x8",    9, 23, 30, MonsterType.NECRO,         8),
            Scenario("Stress: L5 LongSword/Plated vs Spook x6",    5, 13, 15, MonsterType.SPOOK,         6)
        )

        val oldRandom = Random(SEED)
        val k20Random = Random(SEED)
        val k30Random = Random(SEED)

        println("%-48s | %-6s | %-6s | %-6s | Notes".format("Scenario", "OLD", "K=20", "K=30"))
        println("-".repeat(95))

        for (s in scenarios) {
            val character = buildCharacter(s.level, s.weaponAP, s.armorDef)
            val monster   = buildMonster(s.monsterType, s.scalingFactor)

            val oldWR = (1..ITERATIONS).count { simulateBattleOldFormula(character, monster, oldRandom) }.toDouble() / ITERATIONS
            val k20WR = (1..ITERATIONS).count { simulateBattleNewFormula(character, monster, 20, k20Random) }.toDouble() / ITERATIONS
            val k30WR = (1..ITERATIONS).count { simulateBattleNewFormula(character, monster, 30, k30Random) }.toDouble() / ITERATIONS

            fun flag(wr: Double) = when {
                wr > 0.90  -> "E"   // too easy
                wr in 0.70..0.90 -> "."   // target
                wr > 0.40  -> "H"   // hard
                else -> "X"         // danger
            }

            val totalDefense = character.totalDefense
            val monAP = monster.attackPower
            val avgDmgOld = maxOf(1.0, monAP.toDouble() - totalDefense + monster.scalingFactor / 2.0)
            val avgDmgK20 = maxOf(1.0, monAP.toDouble() * 20 / (totalDefense + 20))
            val avgDmgK30 = maxOf(1.0, monAP.toDouble() * 30 / (totalDefense + 30))

            val note = "def=$totalDefense monAP=$monAP | avgDmg old=%.1f k20=%.1f k30=%.1f".format(avgDmgOld, avgDmgK20, avgDmgK30)

            println("%-48s | %4.0f%%${flag(oldWR)} | %4.0f%%${flag(k20WR)} | %4.0f%%${flag(k30WR)} | %s".format(
                s.label.take(48), oldWR * 100, k20WR * 100, k30WR * 100, note))
        }
        println()
        println("Legend: . = 70-90% target | E = >90% too easy | H = 40-70% hard | X = <40% danger")
        println()
    }

    // -------------------------------------------------------------------------
    // Q5: Monster stat adjustment recommendations
    // -------------------------------------------------------------------------

    @Test
    fun `Q5 monster stat adjustment recommendations for K=30`() {
        println("=".repeat(90))
        println("Q5: MONSTER STAT ADJUSTMENT RECOMMENDATIONS (K=30)")
        println("=".repeat(90))
        println()
        println("Current monster stats:")
        println("%-18s | %-8s | %-8s | %-10s".format("Monster", "BaseAP", "BaseHP", "MinLevel"))
        println("-".repeat(50))
        for (mp in MONSTER_PROFILES) {
            println("%-18s | %-8d | %-8d | %-10d".format(
                mp.type.displayName, mp.type.baseAP, mp.type.baseDP, mp.type.minLevel))
        }
        println()

        val bestK = 30
        val random = Random(SEED)

        // For each monster, compute win rates at minLevel with canonical gear, scaling 1-4
        println("Win rates at minimum encounter level, canonical gear, K=$bestK:")
        println()
        println("%-18s | %-6s | %-10s | %-10s | %-10s | %-10s | %-12s".format(
            "Monster", "Level", "Scale 1", "Scale 2", "Scale 3", "Scale 4", "Status"))
        println("-".repeat(90))

        data class Recommendation(val monster: MonsterType, val issue: String, val fix: String)
        val recommendations = mutableListOf<Recommendation>()

        for (mp in MONSTER_PROFILES) {
            val level = mp.expectedLevelRange.first
            val loadout = CANONICAL_LOADOUTS.find { level in it.levelRange } ?: CANONICAL_LOADOUTS.last()
            val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)

            val scaleWinRates = (1..4).map { scale ->
                val monster = buildMonster(mp.type, scale)
                val wins = (1..ITERATIONS).count { simulateBattleNewFormula(character, monster, bestK, random) }
                wins.toDouble() / ITERATIONS
            }

            val sc1 = scaleWinRates[0]
            val sc4 = scaleWinRates[3]

            val status = when {
                sc1 > 0.95 && sc4 > 0.90 -> {
                    recommendations.add(Recommendation(mp.type,
                        "Too easy across all scaling — win rates >${(sc1*100).toInt()}% even at scale 4",
                        "Increase baseAP by 20-30%"))
                    "TOO EASY"
                }
                sc1 > 0.90 -> {
                    recommendations.add(Recommendation(mp.type,
                        "Slightly weak at low scaling (scale 1: ${(sc1*100).toInt()}%)",
                        "Minor baseAP increase (+1 to +2)"))
                    "SLIGHTLY WEAK"
                }
                sc1 in 0.70..0.90 && sc4 in 0.40..0.70 -> "WELL TUNED"
                sc1 in 0.70..0.90 && sc4 < 0.40 -> {
                    recommendations.add(Recommendation(mp.type,
                        "Cliff at scale 4 (${(sc4*100).toInt()}%)",
                        "Lower baseAP slightly or cap this monster's max scaling"))
                    "CLIFF AT SC4"
                }
                sc1 < 0.60 -> {
                    recommendations.add(Recommendation(mp.type,
                        "Too strong even at scale 1 (${(sc1*100).toInt()}%)",
                        "Reduce baseAP"))
                    "TOO STRONG"
                }
                else -> "OK"
            }

            println("%-18s | %-6d | %8.1f%% | %8.1f%% | %8.1f%% | %8.1f%% | %s".format(
                mp.type.displayName, level,
                sc1 * 100, scaleWinRates[1] * 100, scaleWinRates[2] * 100, sc4 * 100,
                status))
        }
        println()

        if (recommendations.isNotEmpty()) {
            println("=".repeat(90))
            println("RECOMMENDATIONS")
            println("=".repeat(90))
            println()
            for (rec in recommendations) {
                println("${rec.monster.displayName}:")
                println("  Issue: ${rec.issue}")
                println("  Fix:   ${rec.fix}")
                println()
            }
        } else {
            println("All monsters are well-tuned at K=$bestK. No adjustments needed.")
        }
    }
}
