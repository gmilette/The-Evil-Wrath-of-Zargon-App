package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterType
import org.junit.Test
import kotlin.random.Random

/**
 * Sanity-check simulation for the updated monster scaling mechanic.
 *
 * Change under test:
 *   Old: 50% chance per level above 1 to increment scalingFactor
 *   New: 67% chance (Random.nextInt(1,4) >= 2, i.e. rolls 2 or 3 out of 1-3) per level, capped at 6
 *
 * Damage formula: max(1, (monsterAP * 20.0 / (totalDefense + 20.0) * U[0.84, 1.16]).toInt())
 *
 * Canonical player stats provided by spec (not derived from level formula):
 *   Level 1:  totalAP=10, totalDef=25, maxHP=20
 *   Level 3:  totalAP=17, totalDef=36, maxHP=30
 *   Level 5:  totalAP=26, totalDef=51, maxHP=40
 *   Level 7:  totalAP=35, totalDef=64, maxHP=50
 *   Level 9:  totalAP=44, totalDef=82, maxHP=60
 *   Level 11: totalAP=60, totalDef=102, maxHP=70
 *
 * "Good" results:
 *   - Scaling distribution should shift noticeably higher than old 50% baseline
 *   - Win rates 70-90% per battle at each canonical level (weighted by actual encounter distribution)
 *   - No level should be consistently above 90% (too easy) or below 60% (too brutal)
 *
 * Monster AP values are the proposed tuned values from TunedMonsterSimulation (K=20 design).
 */
class ScalingDistributionSimulation {

    companion object {
        private const val ITERATIONS = 5000
        private val SEED = 77L
        private const val K = 20.0
        private const val SCALE_CAP = 6

        // Canonical player stats exactly as specified — not computed from a level formula
        data class CanonicalPlayer(
            val level: Int,
            val totalAP: Int,
            val totalDef: Int,
            val maxHP: Int
        )

        val CANONICAL_PLAYERS = listOf(
            CanonicalPlayer(level = 1,  totalAP = 10, totalDef = 25,  maxHP = 20),
            CanonicalPlayer(level = 3,  totalAP = 17, totalDef = 36,  maxHP = 30),
            CanonicalPlayer(level = 5,  totalAP = 26, totalDef = 51,  maxHP = 40),
            CanonicalPlayer(level = 7,  totalAP = 35, totalDef = 64,  maxHP = 50),
            CanonicalPlayer(level = 9,  totalAP = 44, totalDef = 82,  maxHP = 60),
            CanonicalPlayer(level = 11, totalAP = 60, totalDef = 102, maxHP = 70)
        )

        // Proposed tuned monster AP values (from TunedMonsterSimulation)
        // baseHP kept at original baseDP values from MonsterType.kt
        data class MonsterDef(
            val name: String,
            val type: MonsterType,
            val baseAP: Int,
            val baseHP: Int,
            val minLevel: Int
        )

        val MONSTERS = listOf(
            MonsterDef("Slime",        MonsterType.SLIME,         8,  5,  1),
            MonsterDef("Bat",          MonsterType.BAT,           12, 10, 1),
            MonsterDef("Babble",       MonsterType.BABBLE,        15, 12, 1),
            MonsterDef("Spook",        MonsterType.SPOOK,         20, 14, 1),
            MonsterDef("Beleth",       MonsterType.BELETH,        25, 16, 2),
            MonsterDef("SkanderSnake", MonsterType.SKANDER_SNAKE, 30, 20, 5),
            MonsterDef("Necro",        MonsterType.NECRO,         35, 30, 6)
        )
    }

    // -------------------------------------------------------------------------
    // Scaling factor generator — the mechanic under test
    // 67% chance per level above 1: Random.nextInt(1,4) generates 1, 2, or 3.
    // Values 2 and 3 satisfy >= 2, giving P(increment) = 2/3 ~ 67%.
    // Cap at SCALE_CAP (6).
    // -------------------------------------------------------------------------

    private fun rollScalingFactor(playerLevel: Int, random: Random): Int {
        var scalingFactor = 1
        for (i in 1 until playerLevel) {
            if (scalingFactor >= SCALE_CAP) break
            if (random.nextInt(1, 4) >= 2) scalingFactor++
        }
        return scalingFactor
    }

    // -------------------------------------------------------------------------
    // Damage formula: K=20 hyperbolic, ±16% variance
    // -------------------------------------------------------------------------

    private fun monsterDamage(monsterAP: Int, totalDef: Int, random: Random): Int {
        val mult = 0.84 + random.nextDouble() * 0.32   // uniform [0.84, 1.16]
        val raw = monsterAP.toDouble() * K / (totalDef.toDouble() + K) * mult
        return maxOf(1, raw.toInt())
    }

    // -------------------------------------------------------------------------
    // Single battle: returns true if player wins
    // -------------------------------------------------------------------------

    private fun runBattle(
        player: CanonicalPlayer,
        monsterAP: Int,
        monsterHP: Int,
        random: Random
    ): Boolean {
        var playerHP = player.maxHP
        var mHP = monsterHP

        while (playerHP > 0 && mHP > 0) {
            mHP -= player.totalAP
            if (mHP <= 0) break
            playerHP -= monsterDamage(monsterAP, player.totalDef, random)
        }
        return playerHP > 0
    }

    // -------------------------------------------------------------------------
    // TEST 1: Scaling factor distribution per canonical level
    // Reports % of encounters landing at each scale factor 1-6
    // -------------------------------------------------------------------------

    @Test
    fun `scaling factor distribution at canonical levels`() {
        val random = Random(SEED)

        println("=".repeat(80))
        println("SCALING FACTOR DISTRIBUTION (67% increment probability, cap=$SCALE_CAP)")
        println("Iterations: $ITERATIONS per level | Old mechanic was 50% per level (uncapped sim)")
        println("=".repeat(80))
        println()
        println("For reference — theoretical distribution comparison:")
        println("  50% per level (old): E[scale] at L7 ~ 4.0, P(max=6) never reached before cap")
        println("  67% per level (new): E[scale] at L7 grows faster; cap at 6 clips the tail")
        println()

        val scaleHeader = (1..SCALE_CAP).joinToString(" | ") { "Sc$it  " }
        println("%-8s | $scaleHeader | E[scale] | P(cap=6)".format("Level"))
        println("-".repeat(80))

        for (player in CANONICAL_PLAYERS) {
            val counts = IntArray(SCALE_CAP + 1)   // index 1..6

            repeat(ITERATIONS) {
                val sf = rollScalingFactor(player.level, random)
                counts[sf]++
            }

            val expectedScale = (1..SCALE_CAP).sumOf { sf -> sf.toDouble() * counts[sf] } / ITERATIONS
            val pCap = counts[SCALE_CAP].toDouble() / ITERATIONS

            val distStr = (1..SCALE_CAP).joinToString(" | ") { sf ->
                "%5.1f%%".format(counts[sf].toDouble() / ITERATIONS * 100)
            }
            println("%-8d | %s | %8.2f | %7.1f%%".format(
                player.level, distStr, expectedScale, pCap * 100))
        }
        println()
    }

    // -------------------------------------------------------------------------
    // TEST 2: Win rate per level, weighted by actual encounter distribution
    // For each canonical level, simulate ITERATIONS encounters:
    //   1. Roll scaling factor for this level
    //   2. Pick a valid monster (uniform random from those with minLevel <= playerLevel)
    //   3. Run one battle
    // Report: weighted win rate, and per-scale win rate breakdown
    // -------------------------------------------------------------------------

    @Test
    fun `win rate per level weighted by actual encounter distribution`() {
        val random = Random(SEED)

        println("=".repeat(80))
        println("WIN RATE PER LEVEL — Weighted by 67% scaling distribution, K=20, cap=$SCALE_CAP")
        println("Iterations: $ITERATIONS encounters per canonical level")
        println("=".repeat(80))
        println()
        println("Target: 70-90% win rate per encounter | >90% = too easy | <60% = too brutal")
        println()

        println("%-8s | %-10s | %-30s | %-8s".format("Level", "WinRate", "Avg scale (E[sf])", "Flag"))
        println("-".repeat(65))

        val redFlags = mutableListOf<String>()

        for (player in CANONICAL_PLAYERS) {
            val validMonsters = MONSTERS.filter { it.minLevel <= player.level }
            var wins = 0
            var totalScale = 0

            repeat(ITERATIONS) {
                val sf = rollScalingFactor(player.level, random)
                totalScale += sf
                val monster = validMonsters[random.nextInt(validMonsters.size)]
                val monAP = monster.baseAP * sf
                val monHP = monster.baseHP * sf
                if (runBattle(player, monAP, monHP, random)) wins++
            }

            val winRate = wins.toDouble() / ITERATIONS
            val avgScale = totalScale.toDouble() / ITERATIONS

            val flag = when {
                winRate > 0.90 -> "TOO EASY"
                winRate < 0.60 -> "TOO BRUTAL"
                winRate < 0.70 -> "HARD"
                else           -> "OK"
            }

            if (flag != "OK") {
                redFlags.add("Level ${player.level}: $flag (WR=${(winRate * 100).toInt()}%)")
            }

            println("%-8d | %8.1f%% | avgScale=%-6.2f                  | %s".format(
                player.level, winRate * 100, avgScale, flag))
        }

        println()

        // Per-scale breakdown table
        println("=".repeat(80))
        println("PER-SCALE WIN RATES (avg over valid monsters at each level)")
        println("Shows win rate when a specific scaling factor is rolled")
        println("=".repeat(80))
        println()
        val scaleHeader = (1..SCALE_CAP).joinToString(" | ") { "Sc$it  " }
        println("%-8s | $scaleHeader".format("Level"))
        println("-".repeat(70))

        for (player in CANONICAL_PLAYERS) {
            val validMonsters = MONSTERS.filter { it.minLevel <= player.level }

            val scaleWinRates = (1..SCALE_CAP).map { sf ->
                val sfRandom = Random(SEED + sf.toLong() + player.level.toLong() * 10)
                var sfWins = 0
                repeat(ITERATIONS) {
                    val monster = validMonsters[sfRandom.nextInt(validMonsters.size)]
                    val monAP = monster.baseAP * sf
                    val monHP = monster.baseHP * sf
                    if (runBattle(player, monAP, monHP, sfRandom)) sfWins++
                }
                sfWins.toDouble() / ITERATIONS
            }

            val rateStr = scaleWinRates.joinToString(" | ") { wr ->
                val flag = when {
                    wr > 0.90  -> "E"
                    wr >= 0.70 -> "."
                    wr >= 0.50 -> "H"
                    else       -> "X"
                }
                "%4.0f%%%s".format(wr * 100, flag)
            }
            println("%-8d | %s".format(player.level, rateStr))
        }
        println("Legend: . = 70-90% target | E = >90% too easy | H = 50-70% hard | X = <50% danger")
        println()

        // Red flags summary
        println("=".repeat(80))
        println("RED FLAG SUMMARY")
        println("=".repeat(80))
        if (redFlags.isEmpty()) {
            println("No red flags. All canonical levels fall within acceptable range.")
        } else {
            println("Issues found:")
            redFlags.forEach { println("  - $it") }
        }
        println()
    }

    // -------------------------------------------------------------------------
    // TEST 3: Scaling distribution comparison — 50% vs 67% side by side
    // Shows exactly how much harder the new mechanic is vs the old one
    // -------------------------------------------------------------------------

    @Test
    fun `compare 50pct vs 67pct scaling distribution and win rate impact`() {
        val random = Random(SEED)

        println("=".repeat(80))
        println("SCALING COMPARISON: 50% (old) vs 67% (new) per-level increment probability")
        println("Cap=$SCALE_CAP for both. K=20, canonical stats. Iterations: $ITERATIONS")
        println("=".repeat(80))
        println()

        data class CompRow(
            val level: Int,
            val oldAvgScale: Double,
            val newAvgScale: Double,
            val oldWinRate: Double,
            val newWinRate: Double
        )

        val rows = mutableListOf<CompRow>()

        for (player in CANONICAL_PLAYERS) {
            val validMonsters = MONSTERS.filter { it.minLevel <= player.level }

            // Old mechanic: 50% chance = Random.nextBoolean()
            var oldScale = 0
            var oldWins = 0
            val oldRandom = Random(SEED + player.level.toLong())
            repeat(ITERATIONS) {
                var sf = 1
                for (i in 1 until player.level) {
                    if (sf >= SCALE_CAP) break
                    if (oldRandom.nextBoolean()) sf++
                }
                oldScale += sf
                val monster = validMonsters[oldRandom.nextInt(validMonsters.size)]
                if (runBattle(player, monster.baseAP * sf, monster.baseHP * sf, oldRandom)) oldWins++
            }

            // New mechanic: 67% chance
            var newScale = 0
            var newWins = 0
            val newRandom = Random(SEED + player.level.toLong())
            repeat(ITERATIONS) {
                val sf = rollScalingFactor(player.level, newRandom)
                newScale += sf
                val monster = validMonsters[newRandom.nextInt(validMonsters.size)]
                if (runBattle(player, monster.baseAP * sf, monster.baseHP * sf, newRandom)) newWins++
            }

            rows.add(CompRow(
                level = player.level,
                oldAvgScale = oldScale.toDouble() / ITERATIONS,
                newAvgScale = newScale.toDouble() / ITERATIONS,
                oldWinRate = oldWins.toDouble() / ITERATIONS,
                newWinRate = newWins.toDouble() / ITERATIONS
            ))
        }

        println("%-8s | %-10s | %-10s | %-11s | %-11s | %-12s | %-12s".format(
            "Level", "OldAvgSc", "NewAvgSc", "OldWinRate", "NewWinRate", "ScaleDelta", "WRDelta"))
        println("-".repeat(90))

        for (r in rows) {
            val scaleDelta = r.newAvgScale - r.oldAvgScale
            val wrDelta = r.newWinRate - r.oldWinRate
            val wrFlag = when {
                r.newWinRate > 0.90 -> " (TOO EASY)"
                r.newWinRate < 0.60 -> " (TOO BRUTAL)"
                r.newWinRate < 0.70 -> " (HARD)"
                else -> ""
            }
            println("%-8d | %10.2f | %10.2f | %9.1f%% | %9.1f%% | %+11.2f | %+10.1f%%%s".format(
                r.level,
                r.oldAvgScale, r.newAvgScale,
                r.oldWinRate * 100, r.newWinRate * 100,
                scaleDelta,
                wrDelta * 100,
                wrFlag))
        }
        println()
        println("ScaleDelta = new avg scale - old avg scale (positive = monsters are bigger on avg)")
        println("WRDelta    = new win rate - old win rate (negative = new mechanic is harder)")
        println()
    }
}
