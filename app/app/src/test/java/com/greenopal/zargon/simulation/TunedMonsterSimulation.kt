package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import org.junit.Test
import kotlin.random.Random

/**
 * Simulation to find the right monster baseAP values for the K=30 hyperbolic damage formula.
 *
 * Goal: at canonical level/equipment, scaling 1-4, win rate should be 70-90%.
 * At scaling 6-8, encounters should be hard/unwinnable (the accepted cliff).
 *
 * Formula: damage = max(1, (monsterAP * K / (totalDefense + K) * rand[0.84, 1.16]).toInt())
 *
 * We test proposed adjusted baseAP values and compare against current values.
 *
 * Additionally we test K=20 vs K=30 with the proposed stats to pick the better K.
 *
 * Proposed monster AP increases (targeting 1-4 dmg/hit at canonical level):
 *   Slime:       1  -> 8   (nuisance monster)
 *   Bat:         2  -> 12  (early challenge)
 *   Babble:      4  -> 15  (early-mid challenge)
 *   Spook:       6  -> 20  (mid challenge)
 *   Beleth:      8  -> 25  (mid-game)
 *   SkanderSnake: 12 -> 30  (late-mid)
 *   Necro:       13 -> 35  (late-game)
 *
 * HP stays the same (baseDP unchanged).
 */
class TunedMonsterSimulation {

    companion object {
        private const val ITERATIONS = 2000
        private val SEED = 42L

        data class MonsterDef(
            val name: String,
            val type: MonsterType,
            val baseAP: Int,
            val baseHP: Int,  // = baseDP in the enum (HP = baseDP * scale)
            val expectedLevel: Int
        )

        // Current monster definitions (from MonsterType.kt)
        val CURRENT_MONSTERS = listOf(
            MonsterDef("Slime",        MonsterType.SLIME,         1,  5,  1),
            MonsterDef("Bat",          MonsterType.BAT,           2,  10, 1),
            MonsterDef("Babble",       MonsterType.BABBLE,        4,  12, 1),
            MonsterDef("Spook",        MonsterType.SPOOK,         6,  14, 1),
            MonsterDef("Beleth",       MonsterType.BELETH,        8,  16, 2),
            MonsterDef("SkanderSnake", MonsterType.SKANDER_SNAKE, 12, 20, 5),
            MonsterDef("Necro",        MonsterType.NECRO,         13, 30, 6)
        )

        // Proposed monster definitions — AP tuned for 70-90% win rate at low-moderate scaling
        val PROPOSED_MONSTERS = listOf(
            MonsterDef("Slime",        MonsterType.SLIME,         8,  5,  1),
            MonsterDef("Bat",          MonsterType.BAT,           12, 10, 1),
            MonsterDef("Babble",       MonsterType.BABBLE,        15, 12, 1),
            MonsterDef("Spook",        MonsterType.SPOOK,         20, 14, 1),
            MonsterDef("Beleth",       MonsterType.BELETH,        25, 16, 2),
            MonsterDef("SkanderSnake", MonsterType.SKANDER_SNAKE, 30, 20, 5),
            MonsterDef("Necro",        MonsterType.NECRO,         35, 30, 6)
        )

        // Canonical loadouts (matching the design spec exactly)
        data class Loadout(val label: String, val levelRange: IntRange, val weaponAP: Int, val armorDef: Int)
        val LOADOUTS = listOf(
            Loadout("Dagger/Cloth",         1..2,  5,  5),
            Loadout("Short Sword/Leather",  3..4,  8,  8),
            Loadout("Long Sword/Plated",    5..6,  13, 15),
            Loadout("Sword Thorns/Spike",   7..8,  18, 20),
            Loadout("Broad Sword/Chain",    9..10, 23, 30),
            Loadout("2H Sword/Platemail",   11..13, 28, 42)
        )
    }

    private fun buildCharacter(level: Int, weaponAP: Int, armorDef: Int): CharacterStats {
        val baseDP = 20 + (level - 1) * 4
        val maxHP  = 20 + (level - 1) * 5
        val baseAP = 5  + (level - 1) * 3
        return CharacterStats(
            baseAP = baseAP, baseDP = baseDP, maxHP = maxHP, currentHP = maxHP,
            baseMP = 10, currentMP = 10, level = level,
            weaponBonus = weaponAP, armorBonus = armorDef
        )
    }

    private fun simulateBattle(
        character: CharacterStats,
        monsterAP: Int,
        monsterHP: Int,
        scalingFactor: Int,
        k: Int,
        random: Random
    ): Boolean {
        var playerHP  = character.currentHP
        var mHP = monsterHP
        val playerDmg = character.totalAP
        val totalDef  = character.totalDefense.toDouble()
        val kd        = k.toDouble()

        while (playerHP > 0 && mHP > 0) {
            mHP -= playerDmg
            if (mHP <= 0) break
            val mult = 0.84 + random.nextDouble() * 0.32
            val rawDmg = monsterAP.toDouble() * kd / (totalDef + kd) * mult
            playerHP -= maxOf(1, rawDmg.toInt())
        }
        return playerHP > 0
    }

    @Test
    fun `test proposed monster AP values with K=20 and K=30`() {
        val random = Random(SEED)

        println("=".repeat(90))
        println("TUNED MONSTER SIMULATION — Testing proposed baseAP values with K=20 and K=30")
        println("=".repeat(90))
        println()
        println("Target: 70-90% win rate at scaling 1-4 with canonical gear at expected encounter level.")
        println("Cliff (scale 5-8) should produce hard-to-impossible encounters.")
        println()

        val kValues = listOf(20, 30)

        for (k in kValues) {
            println("=" .repeat(90))
            println("K = $k")
            println("=" .repeat(90))
            println()

            // For each monster, show current vs proposed win rates at its expected encounter level
            println("%-18s | %-6s | %-30s | %-30s".format(
                "", "", "--- CURRENT baseAP ---", "--- PROPOSED baseAP ---"))
            println("%-18s | %-6s | Sc1   Sc2   Sc3   Sc4   Sc6   Sc8 | Sc1   Sc2   Sc3   Sc4   Sc6   Sc8".format(
                "Monster", "Level"))
            println("-".repeat(100))

            for (i in CURRENT_MONSTERS.indices) {
                val curr = CURRENT_MONSTERS[i]
                val prop = PROPOSED_MONSTERS[i]
                val level = curr.expectedLevel
                val loadout = LOADOUTS.find { level in it.levelRange } ?: LOADOUTS.last()
                val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)

                val scales = listOf(1, 2, 3, 4, 6, 8)

                val currRates = scales.map { scale ->
                    val mAP = curr.baseAP * scale
                    val mHP = curr.baseHP * scale
                    val wins = (1..ITERATIONS).count { simulateBattle(character, mAP, mHP, scale, k, random) }
                    wins.toDouble() / ITERATIONS
                }

                val propRates = scales.map { scale ->
                    val mAP = prop.baseAP * scale
                    val mHP = prop.baseHP * scale
                    val wins = (1..ITERATIONS).count { simulateBattle(character, mAP, mHP, scale, k, random) }
                    wins.toDouble() / ITERATIONS
                }

                fun fmtRate(r: Double): String {
                    val pct = (r * 100).toInt()
                    val flag = when {
                        r > 0.90 -> "E"
                        r in 0.70..0.90 -> "."
                        r > 0.40 -> "H"
                        else -> "X"
                    }
                    return "%3d%%$flag".format(pct)
                }

                val currStr = currRates.joinToString(" ") { fmtRate(it) }
                val propStr = propRates.joinToString(" ") { fmtRate(it) }
                val monsterCol = "%-18s".format(curr.name)
                val levelCol = "%-6d".format(level)
                println("$monsterCol | $levelCol | $currStr | $propStr")
            }
            println("  E=>90% too easy | .=70-90% target | H=40-70% hard | X=<40% danger")
            println()

            // Summary: how many scenarios in target band with proposed stats?
            println("Summary with PROPOSED stats, K=$k (scaling 1-4, at expected level):")
            var inBand = 0
            var total = 0
            for (prop in PROPOSED_MONSTERS) {
                val level = prop.expectedLevel
                val loadout = LOADOUTS.find { level in it.levelRange } ?: LOADOUTS.last()
                val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)
                for (scale in 1..4) {
                    val mAP = prop.baseAP * scale
                    val mHP = prop.baseHP * scale
                    val wins = (1..ITERATIONS).count { simulateBattle(character, mAP, mHP, scale, k, random) }
                    val wr = wins.toDouble() / ITERATIONS
                    if (wr in 0.70..0.90) inBand++
                    total++
                }
            }
            println("  Scenarios in 70-90% band: $inBand / $total (${inBand * 100 / total}%)")
            println()
        }

        // Now show per-monster avg damage per hit (analytical) for both K values with proposed stats
        println("=".repeat(90))
        println("ANALYTICAL AVG DAMAGE PER HIT (no random, K=20 and K=30) with PROPOSED stats")
        println("=".repeat(90))
        println()
        println("%-18s | %-6s | %-8s | %-6s | %-8s | %-8s | %-8s | %-8s".format(
            "Monster", "Level", "PropAP", "Sc1AP", "TotalDef", "Dmg K=20", "Dmg K=30", "PlayerHP"))
        println("-".repeat(80))

        for (prop in PROPOSED_MONSTERS) {
            val level = prop.expectedLevel
            val loadout = LOADOUTS.find { level in it.levelRange } ?: LOADOUTS.last()
            val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)
            val totalDef = character.totalDefense.toDouble()
            val sc1AP = prop.baseAP.toDouble()

            val dmgK20 = sc1AP * 20 / (totalDef + 20)
            val dmgK30 = sc1AP * 30 / (totalDef + 30)

            println("%-18s | %-6d | %-8d | %-6d | %-8.0f | %-8.1f | %-8.1f | %-8d".format(
                prop.name, level, prop.baseAP, prop.baseAP, totalDef, dmgK20, dmgK30, character.maxHP))
        }
        println()
        println("With proposed stats + K=30:")
        println("  Slime sc1: ~%.1f dmg/hit against L1 player (HP=20) -> takes ~%.0f hits to kill".format(
            PROPOSED_MONSTERS[0].baseAP.toDouble() * 30 / (25 + 30),
            20.0 / (PROPOSED_MONSTERS[0].baseAP.toDouble() * 30 / (25 + 30))
        ))
    }

    @Test
    fun `fine-tune proposed stats - win rate table at all canonical level-monster pairings`() {
        val random = Random(SEED)
        val k = 30

        println("=".repeat(90))
        println("FINE-TUNED WIN RATES — Proposed monster stats, K=30")
        println("Each canonical loadout level tested against valid monsters, scaling 1-8")
        println("=".repeat(90))
        println()

        // Test each loadout level against all monsters that would be encountered at that level
        val levelMonsterMap = mapOf(
            1  to listOf("Slime", "Bat", "Babble", "Spook"),
            2  to listOf("Slime", "Bat", "Babble", "Spook", "Beleth"),
            3  to listOf("Bat", "Babble", "Spook", "Beleth"),
            5  to listOf("Babble", "Spook", "Beleth", "SkanderSnake"),
            6  to listOf("Spook", "Beleth", "SkanderSnake", "Necro"),
            7  to listOf("Beleth", "SkanderSnake", "Necro"),
            9  to listOf("SkanderSnake", "Necro"),
            11 to listOf("SkanderSnake", "Necro")
        )

        val testLevels = listOf(1, 2, 3, 5, 6, 7, 9, 11)
        val scales = listOf(1, 2, 3, 4, 5, 6, 8)

        for (level in testLevels) {
            val loadout = LOADOUTS.find { level in it.levelRange } ?: LOADOUTS.last()
            val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)
            val validMonsterNames = levelMonsterMap[level] ?: continue

            println("Level $level | ${loadout.label} | totalDef=${character.totalDefense} | totalAP=${character.totalAP} | HP=${character.maxHP}")
            println("  %-18s | %s".format("Monster (propAP)", scales.joinToString(" | ") { "Sc%-2d".format(it) }))
            println("  " + "-".repeat(80))

            for (name in validMonsterNames) {
                val prop = PROPOSED_MONSTERS.find { it.name == name } ?: continue
                val rateStr = scales.map { scale ->
                    val mAP = prop.baseAP * scale
                    val mHP = prop.baseHP * scale
                    val wins = (1..ITERATIONS).count { simulateBattle(character, mAP, mHP, scale, k, random) }
                    val wr = wins.toDouble() / ITERATIONS
                    val pct = (wr * 100).toInt()
                    val flag = when {
                        wr > 0.90 -> "E"
                        wr in 0.70..0.90 -> "."
                        wr > 0.40 -> "H"
                        else -> "X"
                    }
                    "%3d$flag".format(pct)
                }.joinToString(" | ")
                println("  %-18s | %s".format("${name}(AP=${prop.baseAP})", rateStr))
            }
            println("  E=>90% easy | .=70-90% target | H=40-70% hard | X=<40% danger")
            println()
        }
    }

    @Test
    fun `evaluate scaling cap impact - K=30 proposed stats with cap at 6`() {
        val random = Random(SEED)
        val k = 30
        val scaleCap = 6

        println("=".repeat(90))
        println("SCALING CAP EVALUATION — K=30, Proposed stats, cap=$scaleCap")
        println("=".repeat(90))
        println()
        println("With a scaling cap of $scaleCap, the maximum monster AP = baseAP * $scaleCap")
        println("This prevents the near-impossible X-zone encounters at high player levels.")
        println()

        // Show effective AP and expected damage for capped vs uncapped at high player levels
        val highLevels = listOf(7, 9, 11)
        for (level in highLevels) {
            val loadout = LOADOUTS.find { level in it.levelRange } ?: LOADOUTS.last()
            val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)
            val totalDef = character.totalDefense.toDouble()

            println("Level $level | ${loadout.label} | totalDef=${character.totalDefense} | HP=${character.maxHP}")
            println("  %-18s | %-8s | %-10s | %-10s | %-10s | %-10s | %-10s".format(
                "Monster", "PropAP", "CappedMaxAP", "Dmg@Cap", "WR@Cap", "UncapMaxAP", "WR@Uncap"))
            println("  " + "-".repeat(85))

            for (prop in PROPOSED_MONSTERS) {
                val cappedScaleAP = prop.baseAP * scaleCap
                val uncappedScaleAP = prop.baseAP * (level + 2)  // rough uncapped max for this level
                val cappedHP = prop.baseHP * scaleCap
                val uncappedHP = prop.baseHP * (level + 2)

                val dmgAtCap = cappedScaleAP.toDouble() * k / (totalDef + k)
                val cappedWins = (1..ITERATIONS).count {
                    simulateBattle(character, cappedScaleAP, cappedHP, scaleCap, k, random)
                }
                val cappedWR = cappedWins.toDouble() / ITERATIONS
                val uncappedWins = (1..ITERATIONS).count {
                    simulateBattle(character, uncappedScaleAP, uncappedHP, level + 2, k, random)
                }
                val uncappedWR = uncappedWins.toDouble() / ITERATIONS

                fun fmtWR(wr: Double) = when {
                    wr > 0.90 -> "%4.0f%%E".format(wr * 100)
                    wr in 0.70..0.90 -> "%4.0f%%.".format(wr * 100)
                    wr > 0.40 -> "%4.0f%%H".format(wr * 100)
                    else -> "%4.0f%%X".format(wr * 100)
                }

                println("  %-18s | %-8d | %-10d | %-10.1f | %-10s | %-10d | %-10s".format(
                    prop.name, prop.baseAP, cappedScaleAP, dmgAtCap,
                    fmtWR(cappedWR), uncappedScaleAP, fmtWR(uncappedWR)))
            }
            println()
        }

        println("Conclusion: with cap=$scaleCap, worst-case scaling for all levels is capped.")
        println("The cliff is preserved but moved to scale 6 consistently across all player levels.")
    }
}
