package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.MonsterType
import org.junit.Test
import kotlin.random.Random

/**
 * Simulates lower K values (8, 12, 15, 20) for the hyperbolic damage formula to find
 * one that creates genuine endgame tension (40-60% win rate at L9-11, scale 5-6)
 * without making early game brutal.
 *
 * Formula: damage = max(1, (monsterAP * K / (totalDefense + K) * randomMultiplier).toInt())
 *          randomMultiplier = Uniform(0.84, 1.16)  (±16% variance)
 *
 * Canonical loadouts (totalAP, totalDef, maxHP) are given directly:
 *   L1:  totalAP=10, totalDef=25, maxHP=20
 *   L3:  totalAP=17, totalDef=36, maxHP=30
 *   L5:  totalAP=26, totalDef=51, maxHP=40
 *   L7:  totalAP=35, totalDef=64, maxHP=50
 *   L9:  totalAP=44, totalDef=82, maxHP=60
 *   L11: totalAP=60, totalDef=102, maxHP=70
 *
 * Design targets:
 *   Early (L1 Sc1-2):   85-100% — manageable
 *   Early (L1 Sc3-4):   50-70%  — starting to feel dangerous
 *   Mid   (L5 Sc3-4):   70-90%  — challenging but winnable
 *   Mid   (L5 Sc5-6):   30-50%  — genuinely dangerous
 *   End   (L9-11 Sc4-5): 60-80% — tense but player favored
 *   End   (L9-11 Sc6):   40-60% — real danger, not guaranteed
 */
class LowerKEndgameSimulation {

    companion object {
        private const val ITERATIONS = 2000
        private val SEED = 42L

        val K_VALUES = listOf(8, 12, 15, 20)

        // Canonical loadouts: label, totalAP, totalDef, maxHP
        data class Loadout(val label: String, val level: Int, val totalAP: Int, val totalDef: Int, val maxHP: Int)

        val LOADOUTS = listOf(
            Loadout("L1",  1,  10,  25, 20),
            Loadout("L3",  3,  17,  36, 30),
            Loadout("L5",  5,  26,  51, 40),
            Loadout("L7",  7,  35,  64, 50),
            Loadout("L9",  9,  44,  82, 60),
            Loadout("L11", 11, 60, 102, 70)
        )

        // All 7 regular monsters
        val MONSTERS = listOf(
            MonsterType.SLIME,        // baseAP=1
            MonsterType.BAT,          // baseAP=2
            MonsterType.BABBLE,       // baseAP=4
            MonsterType.SPOOK,        // baseAP=6
            MonsterType.BELETH,       // baseAP=8
            MonsterType.SKANDER_SNAKE,// baseAP=12
            MonsterType.NECRO         // baseAP=13
        )

        val SCALING_FACTORS = 1..6
    }

    /**
     * Simulate one battle with the hyperbolic K formula.
     * Returns true if player wins.
     */
    private fun simulateBattle(
        totalAP: Int,
        totalDef: Int,
        maxHP: Int,
        monsterAP: Int,
        monsterHP: Int,
        k: Int,
        random: Random
    ): Boolean {
        var playerHP = maxHP
        var mHP = monsterHP

        while (playerHP > 0 && mHP > 0) {
            // Player attacks
            mHP -= totalAP
            if (mHP <= 0) break

            // Monster attacks: damage = max(1, (monAP * K / (totalDef + K) * rand[0.84,1.16]).toInt())
            val randomMultiplier = 0.84 + random.nextDouble() * 0.32
            val rawDamage = monsterAP.toDouble() * k / (totalDef + k) * randomMultiplier
            val damage = maxOf(1, rawDamage.toInt())
            playerHP -= damage
        }

        return playerHP > 0
    }

    @Test
    fun `lower K endgame tension simulation`() {
        println("=".repeat(90))
        println("LOWER K ENDGAME TENSION SIMULATION")
        println("Formula: damage = max(1, (monsterAP * K / (totalDef + K) * rand[0.84,1.16]).toInt())")
        println("K values tested: ${K_VALUES.joinToString()}")
        println("Iterations per scenario: $ITERATIONS")
        println("=".repeat(90))
        println()

        println("Design targets:")
        println("  Early game (L1 Sc1-2):    85-100% win rate  — manageable")
        println("  Early game (L1 Sc3-4):    50-70%  win rate  — starting to feel dangerous")
        println("  Mid-game   (L5 Sc3-4):    70-90%  win rate  — challenging but winnable")
        println("  Mid-game   (L5 Sc5-6):    30-50%  win rate  — genuinely dangerous")
        println("  Endgame    (L9-11 Sc4-5): 60-80%  win rate  — tense but player favored")
        println("  Endgame    (L9-11 Sc6):   40-60%  win rate  — real danger, not guaranteed")
        println()

        // Store results: K -> Loadout -> Monster -> Scale -> WinRate
        data class Result(val k: Int, val loadout: Loadout, val monster: MonsterType, val scale: Int, val winRate: Double)
        val allResults = mutableListOf<Result>()

        for (k in K_VALUES) {
            val random = Random(SEED)
            for (loadout in LOADOUTS) {
                for (monster in MONSTERS) {
                    for (scale in SCALING_FACTORS) {
                        val monsterAP = monster.baseAP * scale
                        val monsterHP = monster.baseDP * scale
                        val wins = (1..ITERATIONS).count {
                            simulateBattle(
                                loadout.totalAP, loadout.totalDef, loadout.maxHP,
                                monsterAP, monsterHP, k, random
                            )
                        }
                        allResults.add(Result(k, loadout, monster, scale, wins.toDouble() / ITERATIONS))
                    }
                }
            }
        }

        // Print difficulty matrix for each K value
        // Rows: selected loadout x monster combos. Columns: Sc1-6
        val matrixRows = listOf(
            Pair(LOADOUTS[0], MonsterType.SLIME),         // L1/Slime
            Pair(LOADOUTS[0], MonsterType.NECRO),         // L1/Necro
            Pair(LOADOUTS[2], MonsterType.SKANDER_SNAKE), // L5/Skander
            Pair(LOADOUTS[2], MonsterType.NECRO),         // L5/Necro
            Pair(LOADOUTS[4], MonsterType.NECRO),         // L9/Necro
            Pair(LOADOUTS[5], MonsterType.NECRO),         // L11/Necro
        )

        val rowLabels = listOf(
            "L1/Slime",
            "L1/Necro",
            "L5/Skander",
            "L5/Necro",
            "L9/Necro",
            "L11/Necro"
        )

        for (k in K_VALUES) {
            println("=".repeat(70))
            println("K=$k")
            println("=".repeat(70))

            // Print the difficulty matrix
            val header = "%-14s  %6s  %6s  %6s  %6s  %6s  %6s".format(
                "", "Sc1", "Sc2", "Sc3", "Sc4", "Sc5", "Sc6"
            )
            println(header)
            println("-".repeat(60))

            for (i in matrixRows.indices) {
                val (loadout, monster) = matrixRows[i]
                val label = rowLabels[i]
                val cells = (1..6).map { scale ->
                    val r = allResults.find { it.k == k && it.loadout == loadout && it.monster == monster && it.scale == scale }
                    r?.winRate ?: 0.0
                }
                val cellStr = cells.joinToString("  ") { wr ->
                    val flag = when {
                        wr >= 0.85 -> " "
                        wr >= 0.60 -> "~"
                        wr >= 0.40 -> "!"
                        else       -> "X"
                    }
                    "%5.1f%%%s".format(wr * 100, flag)
                }
                println("%-14s  %s".format(label, cellStr))
            }
            println()
            println("  Legend:  (space)=85%+ easy  ~=60-85% ok  !=40-60% dangerous  X=<40% brutal")
            println()
        }

        // Score each K value against design targets
        println("=".repeat(90))
        println("SCORING AGAINST DESIGN TARGETS")
        println("=".repeat(90))
        println()

        data class KScore(
            val k: Int,
            val earlySlimeSc12Avg: Double,    // L1/Slime Sc1-2 — should be 85-100%
            val earlyAllSc12Avg: Double,       // L1 all monsters Sc1-2 — should be 85-100%
            val earlyAllSc34Avg: Double,       // L1 Sc3-4 — should be 50-70%
            val midSc34Avg: Double,            // L5 Sc3-4 — should be 70-90%
            val midSc56Avg: Double,            // L5 Sc5-6 — should be 30-50%
            val endSc45Avg: Double,            // L9-11 Sc4-5 — should be 60-80%
            val endSc6Avg: Double,             // L9-11 Sc6 — should be 40-60%
            val score: Double
        )

        val scores = K_VALUES.map { k ->
            val kResults = allResults.filter { it.k == k }

            // Early (L1 Slime Sc1-2)
            val earlySlimeSc12 = kResults.filter { it.loadout.level == 1 && it.monster == MonsterType.SLIME && it.scale in 1..2 }
                .map { it.winRate }.average()

            // Early (L1 all Sc1-2)
            val earlyAllSc12 = kResults.filter { it.loadout.level == 1 && it.scale in 1..2 }
                .map { it.winRate }.average()

            // Early (L1 all Sc3-4)
            val earlyAllSc34 = kResults.filter { it.loadout.level == 1 && it.scale in 3..4 }
                .map { it.winRate }.average()

            // Mid (L5 Sc3-4)
            val midSc34 = kResults.filter { it.loadout.level == 5 && it.scale in 3..4 }
                .map { it.winRate }.average()

            // Mid (L5 Sc5-6)
            val midSc56 = kResults.filter { it.loadout.level == 5 && it.scale in 5..6 }
                .map { it.winRate }.average()

            // End (L9-11 Sc4-5)
            val endSc45 = kResults.filter { it.loadout.level in listOf(9, 11) && it.scale in 4..5 }
                .map { it.winRate }.average()

            // End (L9-11 Sc6)
            val endSc6 = kResults.filter { it.loadout.level in listOf(9, 11) && it.scale == 6 }
                .map { it.winRate }.average()

            // Score: penalise distance from target ranges
            fun inRange(v: Double, lo: Double, hi: Double) = v in lo..hi
            fun penalty(v: Double, lo: Double, hi: Double): Double = when {
                v < lo -> (lo - v) * 100
                v > hi -> (v - hi) * 100
                else -> 0.0
            }

            val p1 = penalty(earlySlimeSc12, 0.85, 1.00)  // early slime should be very manageable
            val p2 = penalty(earlyAllSc12,   0.75, 1.00)  // early game Sc1-2 survivable
            val p3 = penalty(earlyAllSc34,   0.45, 0.75)  // early Sc3-4 starting to feel dangerous
            val p4 = penalty(midSc34,         0.70, 0.92)  // mid Sc3-4 challenging but winnable
            val p5 = penalty(midSc56,         0.25, 0.55)  // mid Sc5-6 genuinely dangerous
            val p6 = penalty(endSc45,         0.58, 0.82)  // endgame Sc4-5 tense
            val p7 = penalty(endSc6,          0.38, 0.62)  // endgame Sc6 real danger

            val totalPenalty = p1 + p2 + p3 + p4 + p5 + p6 + p7
            val score = 1000.0 - totalPenalty

            KScore(k, earlySlimeSc12, earlyAllSc12, earlyAllSc34, midSc34, midSc56, endSc45, endSc6, score)
        }

        println("%-6s | %-22s | %-22s | %-22s | %-22s | %-22s | %-22s | %-22s | %-8s".format(
            "K",
            "L1/Slime Sc1-2 [85-100]",
            "L1/All Sc1-2 [75-100]",
            "L1/All Sc3-4 [45-75]",
            "L5 Sc3-4 [70-92]",
            "L5 Sc5-6 [25-55]",
            "L9-11 Sc4-5 [58-82]",
            "L9-11 Sc6 [38-62]",
            "Score"
        ))
        println("-".repeat(175))

        for (s in scores) {
            fun fmt(v: Double, lo: Double, hi: Double): String {
                val flag = if (v in lo..hi) "OK" else "!!"
                return "%5.1f%% %-2s".format(v * 100, flag)
            }
            println("%-6d | %-22s | %-22s | %-22s | %-22s | %-22s | %-22s | %-22s | %8.1f".format(
                s.k,
                fmt(s.earlySlimeSc12Avg, 0.85, 1.00),
                fmt(s.earlyAllSc12Avg,   0.75, 1.00),
                fmt(s.earlyAllSc34Avg,   0.45, 0.75),
                fmt(s.midSc34Avg,         0.70, 0.92),
                fmt(s.midSc56Avg,         0.25, 0.55),
                fmt(s.endSc45Avg,         0.58, 0.82),
                fmt(s.endSc6Avg,          0.38, 0.62),
                s.score
            ))
        }
        println()

        val bestK = scores.maxByOrNull { it.score }!!
        val worstK = scores.minByOrNull { it.score }!!
        println("Best K:  ${bestK.k}  (score ${bestK.score.toInt()})")
        println("Worst K: ${worstK.k}  (score ${worstK.score.toInt()})")
        println()

        // Detailed K=current(30) comparison row for reference
        println("=".repeat(90))
        println("EARLY GAME EARLY-DEATH CHECK (does lower K brutalize L1 Slime/Bat at Sc1-2?)")
        println("=".repeat(90))
        println()
        println("%-6s | %-20s | %-20s | %-20s | %-20s".format(
            "K", "L1/Slime Sc1", "L1/Slime Sc2", "L1/Bat Sc1", "L1/Bat Sc2"
        ))
        println("-".repeat(90))

        for (k in K_VALUES) {
            val kResults = allResults.filter { it.k == k && it.loadout.level == 1 }
            fun wr(monster: MonsterType, sc: Int) =
                kResults.find { it.monster == monster && it.scale == sc }?.winRate ?: 0.0

            val slimeSc1 = wr(MonsterType.SLIME, 1)
            val slimeSc2 = wr(MonsterType.SLIME, 2)
            val batSc1   = wr(MonsterType.BAT, 1)
            val batSc2   = wr(MonsterType.BAT, 2)

            val flag = { v: Double -> if (v >= 0.80) "OK" else if (v >= 0.60) "~" else "PROBLEM" }
            println("%-6d | %5.1f%% %-10s | %5.1f%% %-10s | %5.1f%% %-10s | %5.1f%% %-10s".format(
                k,
                slimeSc1 * 100, flag(slimeSc1),
                slimeSc2 * 100, flag(slimeSc2),
                batSc1 * 100,   flag(batSc1),
                batSc2 * 100,   flag(batSc2)
            ))
        }
        println()
        println("Target: Slime/Bat at Sc1-2 should be 80%+ win rate (not a problem for L1 players).")
        println()

        // Final recommendation
        println("=".repeat(90))
        println("RECOMMENDATION")
        println("=".repeat(90))
        println()
        println("Recommended K value: ${bestK.k}")
        println()

        // Qualitative analysis per K
        for (s in scores.sortedByDescending { it.score }) {
            val earlySafe = s.earlySlimeSc12Avg >= 0.80
            val endTense  = s.endSc6Avg in 0.38..0.65
            val midOk     = s.midSc34Avg >= 0.65
            val verdict = when {
                earlySafe && endTense && midOk -> "RECOMMENDED — meets all primary design goals"
                !earlySafe -> "REJECTED — early game too brutal (Slime/Bat Sc1-2 < 80%)"
                !endTense && s.endSc6Avg > 0.65 -> "NOT RECOMMENDED — endgame still too easy at Sc6"
                !endTense && s.endSc6Avg < 0.38 -> "NOT RECOMMENDED — endgame too brutal at Sc6"
                else -> "PARTIAL — some design goals not met"
            }
            println("K=${s.k}: $verdict")
            println("  Early (L1 Slime Sc1-2): ${"%.1f".format(s.earlySlimeSc12Avg * 100)}% [target 85-100%]")
            println("  Mid   (L5 Sc3-4):       ${"%.1f".format(s.midSc34Avg * 100)}% [target 70-92%]")
            println("  End   (L9-11 Sc6):      ${"%.1f".format(s.endSc6Avg * 100)}% [target 38-62%]")
            println()
        }
    }
}
