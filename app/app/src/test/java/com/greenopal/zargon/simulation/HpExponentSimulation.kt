package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterType
import org.junit.Test
import kotlin.math.pow
import kotlin.random.Random

/**
 * HP Exponent Simulation — tests hpExponent values for monster HP scaling.
 *
 * Current formula: scaledDP = baseDP * scalingFactor  (linear, exponent=1.0)
 * Proposed formula: scaledDP = (baseDP * scalingFactor^hpExponent).toInt()
 *
 * Monster AP remains linear: scaledAP = baseAP * scalingFactor
 * Only HP (baseDP) scaling changes.
 *
 * This decouples monster durability from monster threat, allowing HP to grow
 * more slowly than AP so encounters don't stretch into war-of-attrition at
 * high scaling while still hitting hard.
 *
 * Canonical player loadouts by level (from design spec):
 *   Level 1:  baseAP=5,  weaponBonus=5  (Dagger),          armorBonus=5  (Cloth),          baseDP=20, maxHP=20
 *   Level 3:  baseAP=9,  weaponBonus=8  (Short Sword),      armorBonus=8  (Leather),         baseDP=28, maxHP=30
 *   Level 5:  baseAP=13, weaponBonus=13 (Long Sword),       armorBonus=15 (Plated Leather),  baseDP=36, maxHP=40
 *   Level 7:  baseAP=17, weaponBonus=18 (Sword of Thorns),  armorBonus=20 (Spiked Leather),  baseDP=44, maxHP=50
 *   Level 9:  baseAP=21, weaponBonus=23 (Broad Sword),      armorBonus=30 (Chain Mail),      baseDP=52, maxHP=60
 *   Level 11: baseAP=25, weaponBonus=35 (Atlantean Sword),  armorBonus=42 (Platemail),       baseDP=60, maxHP=70
 */
class HpExponentSimulation {

    companion object {
        private const val ITERATIONS = 2000
        private const val K = 30
        private const val SEED = 42L

        data class PlayerLoadout(
            val level: Int,
            val baseAP: Int,
            val weaponBonus: Int,
            val armorBonus: Int,
            val baseDP: Int,
            val maxHP: Int,
            val label: String
        ) {
            val totalAP: Int get() = baseAP + weaponBonus
            val totalDefense: Int get() = baseDP + armorBonus
        }

        val LOADOUTS = listOf(
            PlayerLoadout(1,  5,  5,  5,  20, 20, "Dagger/Cloth"),
            PlayerLoadout(3,  9,  8,  8,  28, 30, "Short Sword/Leather"),
            PlayerLoadout(5,  13, 13, 15, 36, 40, "Long Sword/Plated Leather"),
            PlayerLoadout(7,  17, 18, 20, 44, 50, "Sword of Thorns/Spiked Leather"),
            PlayerLoadout(9,  21, 23, 30, 52, 60, "Broad Sword/Chain Mail"),
            PlayerLoadout(11, 25, 35, 42, 60, 70, "Atlantean Sword/Platemail")
        )

        data class MonsterDef(
            val type: MonsterType,
            val name: String,
            val baseAP: Int,
            val baseDP: Int
        )

        val MONSTERS = listOf(
            MonsterDef(MonsterType.SLIME,         "Slime",        1,  5),
            MonsterDef(MonsterType.BAT,           "Bat",          2,  10),
            MonsterDef(MonsterType.BABBLE,        "Babble",       5,  12),
            MonsterDef(MonsterType.SPOOK,         "Spook",        7,  14),
            MonsterDef(MonsterType.BELETH,        "Beleth",       8,  16),
            MonsterDef(MonsterType.SKANDER_SNAKE, "SkanderSnake", 12, 20),
            MonsterDef(MonsterType.NECRO,         "Necro",        13, 30)
        )

        val HP_EXPONENTS = listOf(0.35, 0.5, 0.6, 0.7, 1.0)
        val SCALING_FACTORS = listOf(1, 2, 3, 4, 5, 6)
    }

    private fun simulateBattle(
        playerTotalAP: Int,
        playerTotalDefense: Int,
        playerMaxHP: Int,
        monsterAP: Int,
        monsterHP: Int,
        random: Random
    ): Boolean {
        var playerHP = playerMaxHP
        var mHP = monsterHP
        val kd = K.toDouble()
        val totalDef = playerTotalDefense.toDouble()

        while (playerHP > 0 && mHP > 0) {
            mHP -= playerTotalAP
            if (mHP <= 0) break
            val mult = 0.84 + random.nextDouble() * 0.32
            val rawDmg = monsterAP.toDouble() * kd / (totalDef + kd) * mult
            playerHP -= maxOf(1, rawDmg.toInt())
        }
        return playerHP > 0
    }

    private fun scaledMonsterHP(baseDP: Int, scalingFactor: Int, hpExponent: Double): Int {
        return (baseDP.toDouble() * scalingFactor.toDouble().pow(hpExponent)).toInt().coerceAtLeast(1)
    }

    private fun winRate(
        loadout: PlayerLoadout,
        monster: MonsterDef,
        scalingFactor: Int,
        hpExponent: Double,
        random: Random
    ): Double {
        val monsterAP = monster.baseAP * scalingFactor
        val monsterHP = scaledMonsterHP(monster.baseDP, scalingFactor, hpExponent)
        var wins = 0
        repeat(ITERATIONS) {
            if (simulateBattle(loadout.totalAP, loadout.totalDefense, loadout.maxHP, monsterAP, monsterHP, random)) {
                wins++
            }
        }
        return wins.toDouble() / ITERATIONS
    }

    private fun formatWR(wr: Double): String {
        val pct = (wr * 100).toInt()
        val flag = when {
            wr >= 0.85 -> "E"
            wr >= 0.70 -> "."
            wr >= 0.50 -> "H"
            wr >= 0.20 -> "D"
            else -> "X"
        }
        return "%3d$flag".format(pct)
    }

    @Test
    fun `HP exponent simulation - full difficulty matrix`() {
        val random = Random(SEED)

        println("=".repeat(100))
        println("HP EXPONENT SIMULATION — Decoupling monster HP from AP scaling")
        println("Damage formula: K=$K, ±16% variance on monster attacks")
        println("Monster AP scaling: LINEAR (baseAP * scalingFactor)")
        println("Monster HP scaling: POWER   (baseDP * scalingFactor^hpExponent)")
        println("Player AP scaling: FLAT     (no randomness, totalAP per hit)")
        println("=".repeat(100))
        println()
        println("Legend: E=85-100% (early game safe zone) | .=70-84% (target) | H=50-69% (tough) | D=20-49% (dangerous) | X=<20% (deadly)")
        println()

        for (hpExp in HP_EXPONENTS) {
            println("=".repeat(100))
            println("HP EXPONENT = $hpExp${if (hpExp == 1.0) " (CURRENT — linear)" else " (proposed)"}")
            println("=".repeat(100))
            println()

            for (loadout in LOADOUTS) {
                println("  Level ${loadout.level} | ${loadout.label}")
                println("  totalAP=${loadout.totalAP}  totalDef=${loadout.totalDefense}  HP=${loadout.maxHP}")
                println("  %-14s | %s".format(
                    "Monster",
                    SCALING_FACTORS.joinToString("  ") { "Sc$it" }
                ))
                println("  " + "-".repeat(60))

                for (monster in MONSTERS) {
                    val rates = SCALING_FACTORS.map { sf ->
                        winRate(loadout, monster, sf, hpExp, random)
                    }
                    val rateStr = rates.joinToString("  ") { formatWR(it) }
                    val scaledHPs = SCALING_FACTORS.map { sf ->
                        scaledMonsterHP(monster.baseDP, sf, hpExp)
                    }
                    println("  %-14s | %s   [HP: %s]".format(
                        monster.name,
                        rateStr,
                        scaledHPs.joinToString(", ")
                    ))
                }
                println()
            }
        }
    }

    @Test
    fun `HP exponent simulation - cliff analysis`() {
        val random = Random(SEED)

        println("=".repeat(100))
        println("CLIFF ANALYSIS — At what scaling factor does win rate first drop below 70%, 50%, 20%?")
        println("=".repeat(100))
        println()

        data class CliffResult(val exp: Double, val level: Int, val monster: String, val cliff70: Int?, val cliff50: Int?, val cliff20: Int?)

        for (hpExp in HP_EXPONENTS) {
            println("-".repeat(100))
            println("HP EXPONENT = $hpExp${if (hpExp == 1.0) " (CURRENT)" else ""}")
            println("%-6s | %-14s | %-10s | %-10s | %-10s".format("Level", "Monster", "70% cliff", "50% cliff", "20% cliff"))
            println("-".repeat(55))

            for (loadout in LOADOUTS) {
                for (monster in MONSTERS) {
                    val cliff70 = SCALING_FACTORS.firstOrNull { sf ->
                        winRate(loadout, monster, sf, hpExp, random) < 0.70
                    }
                    val cliff50 = SCALING_FACTORS.firstOrNull { sf ->
                        winRate(loadout, monster, sf, hpExp, random) < 0.50
                    }
                    val cliff20 = SCALING_FACTORS.firstOrNull { sf ->
                        winRate(loadout, monster, sf, hpExp, random) < 0.20
                    }
                    val c70str = cliff70?.toString() ?: ">6"
                    val c50str = cliff50?.toString() ?: ">6"
                    val c20str = cliff20?.toString() ?: ">6"
                    println("%-6d | %-14s | %-10s | %-10s | %-10s".format(
                        loadout.level, monster.name, c70str, c50str, c20str
                    ))
                }
                println()
            }
        }
    }

    @Test
    fun `HP exponent simulation - overall difficulty feel summary`() {
        val random = Random(SEED)

        println("=".repeat(100))
        println("DIFFICULTY FEEL SUMMARY — Per exponent, per game phase")
        println("Goals:")
        println("  Early (L1, Sc1-2):  85-100% win rate (manageable)")
        println("  Mid cliff (L5-7):   drops below 50% around Sc4-5 (tough but fair)")
        println("  Endgame (L9-11):    40-60% at Sc5-6 (genuinely dangerous)")
        println("=".repeat(100))
        println()

        data class PhaseStats(val totalScenarios: Int, val aboveTarget: Int, val inTarget: Int, val belowTarget: Int)

        for (hpExp in HP_EXPONENTS) {
            println("-".repeat(80))
            println("HP EXPONENT = $hpExp${if (hpExp == 1.0) " (CURRENT)" else ""}")
            println()

            // Early game: L1, Sc1-2, all monsters — target 85-100%
            var earlyPass = 0
            var earlyTotal = 0
            val l1 = LOADOUTS[0]
            for (monster in MONSTERS) {
                for (sf in listOf(1, 2)) {
                    val wr = winRate(l1, monster, sf, hpExp, random)
                    earlyTotal++
                    if (wr >= 0.85) earlyPass++
                }
            }

            // Mid game: L5-7, check if 50% cliff is at Sc4-5
            var midCliffOk = 0
            var midCliffTotal = 0
            for (loadout in LOADOUTS.filter { it.level in listOf(5, 7) }) {
                for (monster in MONSTERS) {
                    val cliff50 = SCALING_FACTORS.firstOrNull { sf ->
                        winRate(loadout, monster, sf, hpExp, random) < 0.50
                    }
                    midCliffTotal++
                    if (cliff50 != null && cliff50 in 4..5) midCliffOk++
                }
            }

            // Endgame: L9-11, Sc5-6 — target 40-60%
            var endgameInRange = 0
            var endgameTotal = 0
            for (loadout in LOADOUTS.filter { it.level in listOf(9, 11) }) {
                for (monster in MONSTERS) {
                    for (sf in listOf(5, 6)) {
                        val wr = winRate(loadout, monster, sf, hpExp, random)
                        endgameTotal++
                        if (wr in 0.40..0.60) endgameInRange++
                    }
                }
            }

            println("  Early game  (L1, Sc1-2):   scenarios at 85%+  win rate: $earlyPass / $earlyTotal")
            println("  Mid cliff   (L5+7, all M):  50%% cliff at Sc4-5: $midCliffOk / $midCliffTotal scenarios")
            println("  Endgame     (L9+11, Sc5-6): scenarios in 40-60%%: $endgameInRange / $endgameTotal")

            // Quick monster HP at Sc=6 comparison
            println()
            println("  Monster HP at Sc6 (baseDP scaling):")
            for (monster in MONSTERS) {
                val linearHP = monster.baseDP * 6
                val scaledHP = scaledMonsterHP(monster.baseDP, 6, hpExp)
                val pct = (scaledHP.toDouble() / linearHP * 100).toInt()
                println("    %-14s  linear=%3d  exp${"%.2f".format(hpExp)}=%3d  (%d%% of linear)".format(
                    monster.name, linearHP, scaledHP, pct
                ))
            }
            println()
        }
    }

    @Test
    fun `HP exponent simulation - recommendation`() {
        val random = Random(SEED)

        println("=".repeat(100))
        println("RECOMMENDATION — Best hpExponent value scoring against all design targets")
        println("=".repeat(100))
        println()
        println("Scoring (each criterion = 1 point):")
        println("  A: Early game L1, Sc1-2: avg win rate >= 0.85")
        println("  B: Mid game L5, best monster: 50% cliff at Sc4 or Sc5")
        println("  C: Endgame L9-11, Sc5-6: at least 1 monster combo in 40-60% range")
        println("  D: Not trivially easy at endgame (L11 Sc5 has at least 1 monster <60% WR)")
        println("  E: Not instant death at endgame (L11 Sc6 has at least 1 monster >20% WR)")
        println()

        data class ExponentScore(val exp: Double, val score: Int, val notes: List<String>)
        val scores = mutableListOf<ExponentScore>()

        for (hpExp in HP_EXPONENTS) {
            var score = 0
            val notes = mutableListOf<String>()

            // Criterion A: Early game L1, Sc1-2: avg win rate >= 0.85
            val l1 = LOADOUTS[0]
            val earlyWRs = MONSTERS.flatMap { monster ->
                listOf(1, 2).map { sf -> winRate(l1, monster, sf, hpExp, random) }
            }
            val avgEarly = earlyWRs.average()
            if (avgEarly >= 0.85) {
                score++
                notes.add("A: PASS  avg early WR=%.2f".format(avgEarly))
            } else {
                notes.add("A: FAIL  avg early WR=%.2f (need >=0.85)".format(avgEarly))
            }

            // Criterion B: Mid game L5: 50% cliff at Sc4 or Sc5 for any monster
            val l5 = LOADOUTS[2]
            val midCliffAt45 = MONSTERS.any { monster ->
                val cliff50 = SCALING_FACTORS.firstOrNull { sf ->
                    winRate(l5, monster, sf, hpExp, random) < 0.50
                }
                cliff50 != null && cliff50 in 4..5
            }
            if (midCliffAt45) {
                score++
                notes.add("B: PASS  50% cliff at Sc4-5 for at least one L5 monster")
            } else {
                notes.add("B: FAIL  no L5 monster hits 50% cliff at Sc4-5")
            }

            // Criterion C: Endgame L9-11, Sc5-6: at least one combo in 40-60%
            val endgameInRange = LOADOUTS.filter { it.level in listOf(9, 11) }.any { loadout ->
                MONSTERS.any { monster ->
                    listOf(5, 6).any { sf ->
                        val wr = winRate(loadout, monster, sf, hpExp, random)
                        wr in 0.40..0.60
                    }
                }
            }
            if (endgameInRange) {
                score++
                notes.add("C: PASS  endgame has 40-60% scenarios")
            } else {
                notes.add("C: FAIL  no endgame L9-11 Sc5-6 scenarios in 40-60% range")
            }

            // Criterion D: L11 Sc5 has at least 1 monster <60% WR (not trivially easy)
            val l11 = LOADOUTS[5]
            val endgameNotTrivial = MONSTERS.any { monster ->
                winRate(l11, monster, 5, hpExp, random) < 0.60
            }
            if (endgameNotTrivial) {
                score++
                notes.add("D: PASS  L11 Sc5 has challenging encounter (<60% WR)")
            } else {
                notes.add("D: FAIL  L11 Sc5 all monsters trivially easy (all >60%)")
            }

            // Criterion E: L11 Sc6 has at least 1 monster >20% WR (not instant death)
            val endgameNotInstantDeath = MONSTERS.any { monster ->
                winRate(l11, monster, 6, hpExp, random) > 0.20
            }
            if (endgameNotInstantDeath) {
                score++
                notes.add("E: PASS  L11 Sc6 has survivable encounter (>20% WR)")
            } else {
                notes.add("E: FAIL  L11 Sc6 all monsters instant death (<20% WR)")
            }

            scores.add(ExponentScore(hpExp, score, notes))
        }

        for (es in scores.sortedByDescending { it.score }) {
            val tag = if (es.exp == 1.0) " (CURRENT)" else ""
            println("hpExponent=${es.exp}$tag  Score: ${es.score}/5")
            es.notes.forEach { println("    $it") }
            println()
        }

        val best = scores.maxByOrNull { it.score }!!
        println("=".repeat(100))
        if (best.exp == 1.0) {
            println("RECOMMENDATION: Keep current linear scaling (exponent=1.0) — it already meets most targets.")
        } else {
            println("RECOMMENDATION: Use hpExponent=%.2f".format(best.exp))
            println("  This achieves the best balance of early safety, mid-game tension, and endgame danger.")
            println("  It slows HP growth so encounters are shorter but AP threat still drives the danger.")
        }
        println("=".repeat(100))
    }
}
