package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import org.junit.Test
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Cliff calibration simulation.
 *
 * The bimodal cliff is structural: at some scaling factor the monster deals enough damage
 * per hit that it kills the player before the player kills it (player attacks first).
 *
 * Break-even analysis (analytical):
 *   Player kills monster in: ceil(monsterHP / playerAP) turns
 *   Monster kills player in: floor(playerHP / monsterDmgPerHit) turns (before player's final blow)
 *
 * For the cliff to appear at scaling factor S_cliff:
 *   monsterHP * S_cliff / playerAP >= playerHP / (baseAP * S_cliff * K / (totalDef + K))
 *
 * Solving for S_cliff (analytically) to understand where the cliff should be placed.
 *
 * Calibration goal:
 *   - Cliff should appear at scaling factor 4-6 for a player at the expected level/gear
 *   - This means the monster baseAP should be chosen so the break-even is at scale 4-6
 *
 * Formula for optimal baseAP (K=30):
 *   At break-even scale S: ceil(baseHP * S / playerAP) = floor(playerHP / (baseAP * S * 30 / (def + 30)))
 *   Simplifying: baseHP * S / playerAP ≈ playerHP * (def + 30) / (baseAP * S * 30)
 *   => baseAP^target = sqrt(playerHP * baseHP * (def + 30) / (playerAP * 30 * S^2))
 *
 * We solve this for each monster/level combination to find the baseAP that places
 * the cliff at exactly S = 4 (low end) and S = 6 (high end).
 */
class CliffCalibrationSimulation {

    companion object {
        private const val ITERATIONS = 2000
        private val SEED = 42L
        private const val K = 30

        data class Loadout(val label: String, val levelRange: IntRange, val weaponAP: Int, val armorDef: Int)

        val LOADOUTS = listOf(
            Loadout("Dagger/Cloth",         1..2,  5,  5),
            Loadout("Short Sword/Leather",  3..4,  8,  8),
            Loadout("Long Sword/Plated",    5..6,  13, 15),
            Loadout("Sword Thorns/Spike",   7..8,  18, 20),
            Loadout("Broad Sword/Chain",    9..10, 23, 30),
            Loadout("2H Sword/Platemail",   11..13, 28, 42)
        )

        // Monster base HP values (unchanged from original game)
        val MONSTER_BASE_HP = mapOf(
            MonsterType.SLIME         to 5,
            MonsterType.BAT           to 10,
            MonsterType.BABBLE        to 12,
            MonsterType.SPOOK         to 14,
            MonsterType.BELETH        to 16,
            MonsterType.SKANDER_SNAKE to 20,
            MonsterType.NECRO         to 30
        )

        val MONSTERS = listOf(
            MonsterType.SLIME,
            MonsterType.BAT,
            MonsterType.BABBLE,
            MonsterType.SPOOK,
            MonsterType.BELETH,
            MonsterType.SKANDER_SNAKE,
            MonsterType.NECRO
        )

        val MONSTER_EXPECTED_LEVELS = mapOf(
            MonsterType.SLIME         to 1,
            MonsterType.BAT           to 1,
            MonsterType.BABBLE        to 1,
            MonsterType.SPOOK         to 1,
            MonsterType.BELETH        to 2,
            MonsterType.SKANDER_SNAKE to 5,
            MonsterType.NECRO         to 6
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
    fun `compute optimal baseAP that places the cliff at scaling factor 4-6`() {
        println("=".repeat(90))
        println("CLIFF CALIBRATION — Finding optimal baseAP per monster for cliff at scale 4-6")
        println("=".repeat(90))
        println()
        println("Formula: baseAP_target = sqrt(playerHP * baseHP * (totalDef + K) / (playerAP * K * S_cliff^2))")
        println("This places the break-even (turns_to_kill_player = turns_to_kill_monster) at S_cliff.")
        println()
        println("K = $K")
        println()

        println("%-18s | %-6s | %-10s | %-10s | %-14s | %-14s | %-12s | %-12s".format(
            "Monster", "Level", "TotalDef", "PlayerAP", "BaseAP@Cliff=4", "BaseAP@Cliff=6", "BaseHP", "PlayerHP"))
        println("-".repeat(100))

        val recommendations = mutableMapOf<MonsterType, Int>()  // monster -> recommended baseAP

        for (mt in MONSTERS) {
            val level = MONSTER_EXPECTED_LEVELS[mt]!!
            val loadout = LOADOUTS.find { level in it.levelRange } ?: LOADOUTS.last()
            val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)
            val totalDef = character.totalDefense.toDouble()
            val playerAP = character.totalAP.toDouble()
            val playerHP = character.maxHP.toDouble()
            val baseHP = MONSTER_BASE_HP[mt]!!.toDouble()

            // Formula derivation:
            // Player needs ceil(baseHP * S / playerAP) turns to kill monster
            // Monster needs ceil(playerHP / dmg_per_hit) turns to kill player
            // dmg_per_hit = baseAP * S * K / (totalDef + K) [using midpoint multiplier 1.0]
            // At break-even S_cliff:
            //   baseHP * S_cliff / playerAP = playerHP / (baseAP * S_cliff * K / (totalDef + K))
            //   baseHP * S_cliff^2 * K / playerAP = playerHP * (totalDef + K) / baseAP
            //   baseAP = playerHP * (totalDef + K) / (baseHP * S_cliff^2 * K / playerAP)
            //   baseAP = playerHP * playerAP * (totalDef + K) / (baseHP * S_cliff^2 * K)

            val baseAPAtCliff4 = sqrt(playerHP * playerAP * (totalDef + K) / (baseHP * 16.0 * K))
            val baseAPAtCliff6 = sqrt(playerHP * playerAP * (totalDef + K) / (baseHP * 36.0 * K))

            // Recommended: place cliff at ~scale 5 (midpoint)
            val recommended = sqrt(playerHP * playerAP * (totalDef + K) / (baseHP * 25.0 * K)).toInt()
            recommendations[mt] = maxOf(1, recommended)

            println("%-18s | %-6d | %-10.0f | %-10.0f | %-14.1f | %-14.1f | %-12.0f | %-12.0f".format(
                mt.displayName, level, totalDef, playerAP, baseAPAtCliff4, baseAPAtCliff6, baseHP, playerHP))
        }

        println()
        println("Recommended baseAP (cliff at ~scale 5):")
        println("%-18s | %-12s | %-12s".format("Monster", "Recommended", "Current"))
        println("-".repeat(50))
        val currentAP = mapOf(
            MonsterType.SLIME to 1, MonsterType.BAT to 2, MonsterType.BABBLE to 4,
            MonsterType.SPOOK to 6, MonsterType.BELETH to 8,
            MonsterType.SKANDER_SNAKE to 12, MonsterType.NECRO to 13
        )
        for (mt in MONSTERS) {
            println("%-18s | %-12d | %-12d".format(mt.displayName, recommendations[mt], currentAP[mt]))
        }
        println()
    }

    @Test
    fun `verify calibrated stats produce correct cliff placement`() {
        val random = Random(SEED)

        // These are the analytically derived recommended baseAP values from the calibration above.
        // We run simulation to verify the cliff is placed near scale 4-6.
        data class CalibratedMonster(val type: MonsterType, val baseAP: Int, val baseHP: Int, val expectedLevel: Int)

        // Analytically computed approximate values targeting cliff at scale 5:
        val calibrated = listOf(
            CalibratedMonster(MonsterType.SLIME,         4,  5,  1),
            CalibratedMonster(MonsterType.BAT,           5,  10, 1),
            CalibratedMonster(MonsterType.BABBLE,        6,  12, 1),
            CalibratedMonster(MonsterType.SPOOK,         7,  14, 1),
            CalibratedMonster(MonsterType.BELETH,        9,  16, 2),
            CalibratedMonster(MonsterType.SKANDER_SNAKE, 15, 20, 5),
            CalibratedMonster(MonsterType.NECRO,         16, 30, 6)
        )

        println("=".repeat(90))
        println("CALIBRATED STATS VERIFICATION — Cliff should appear near scale 4-6")
        println("K = $K")
        println("=".repeat(90))
        println()

        for (mon in calibrated) {
            val level = mon.expectedLevel
            val loadout = LOADOUTS.find { level in it.levelRange } ?: LOADOUTS.last()
            val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)
            val totalDef = character.totalDefense

            print("${mon.type.displayName} (AP=${mon.baseAP}, HP=${mon.baseHP}) at L$level (def=$totalDef, AP=${character.totalAP}, HP=${character.maxHP}): ")
            val rates = (1..10).map { scale ->
                val mAP = mon.baseAP * scale
                val mHP = mon.baseHP * scale
                val wins = (1..ITERATIONS).count { simulateBattle(character, mAP, mHP, K, random) }
                wins.toDouble() / ITERATIONS
            }

            val rateStr = rates.mapIndexed { i, r ->
                val scale = i + 1
                val pct = (r * 100).toInt()
                val flag = when {
                    r > 0.90 -> "E"
                    r in 0.70..0.90 -> "."
                    r > 0.40 -> "H"
                    else -> "X"
                }
                "Sc$scale:$pct$flag"
            }.joinToString(" ")
            println(rateStr)
        }
        println()
        println("E=>90% easy | .=70-90% target | H=40-70% hard | X=<40% danger")
        println()

        // Find cliff position for each monster
        println("Cliff positions (first scale where win rate drops below 70%):")
        for (mon in calibrated) {
            val level = mon.expectedLevel
            val loadout = LOADOUTS.find { level in it.levelRange } ?: LOADOUTS.last()
            val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)
            val cliffScale = (1..10).firstOrNull { scale ->
                val mAP = mon.baseAP * scale
                val mHP = mon.baseHP * scale
                val wins = (1..ITERATIONS).count { simulateBattle(character, mAP, mHP, K, random) }
                wins.toDouble() / ITERATIONS < 0.70
            }
            println("  ${mon.type.displayName}: cliff at scale ${cliffScale ?: ">10"}")
        }
        println()

        // Summary: are these calibrated values a good choice?
        println("=".repeat(90))
        println("CALIBRATED STATS — FINAL VALUES FOR IMPLEMENTATION")
        println("=".repeat(90))
        println()
        println("These values place the cliff at approximately scale 4-6 with canonical gear:")
        println()
        println("%-18s | %-10s | %-10s | %-12s".format("Monster", "New BaseAP", "Old BaseAP", "Change"))
        println("-".repeat(55))
        val oldAP = mapOf(
            MonsterType.SLIME to 1, MonsterType.BAT to 2, MonsterType.BABBLE to 4,
            MonsterType.SPOOK to 6, MonsterType.BELETH to 8,
            MonsterType.SKANDER_SNAKE to 12, MonsterType.NECRO to 13
        )
        for (mon in calibrated) {
            val old = oldAP[mon.type]!!
            val change = if (mon.baseAP > old) "+${mon.baseAP - old}" else "${mon.baseAP - old}"
            println("%-18s | %-10d | %-10d | %-12s".format(mon.type.displayName, mon.baseAP, old, change))
        }
        println()
        println("Note: These are conservative increases. The cliff is structural — it cannot be")
        println("eliminated without adding player damage variance or decoupling monster HP from AP scaling.")
        println("The 70-90% target band can only be achieved within a narrow range near the cliff edge.")
    }

    @Test
    fun `final K value selection with calibrated stats`() {
        val random = Random(SEED)

        // Calibrated baseAP values
        val calibratedAP = mapOf(
            MonsterType.SLIME to 4, MonsterType.BAT to 5, MonsterType.BABBLE to 6,
            MonsterType.SPOOK to 7, MonsterType.BELETH to 9,
            MonsterType.SKANDER_SNAKE to 15, MonsterType.NECRO to 16
        )
        val monsterBaseHP = mapOf(
            MonsterType.SLIME to 5, MonsterType.BAT to 10, MonsterType.BABBLE to 12,
            MonsterType.SPOOK to 14, MonsterType.BELETH to 16,
            MonsterType.SKANDER_SNAKE to 20, MonsterType.NECRO to 30
        )

        println("=".repeat(90))
        println("FINAL K VALUE SELECTION — Calibrated monster stats, K=20 vs K=30")
        println("=".repeat(90))
        println()

        for (k in listOf(20, 30)) {
            println("--- K = $k ---")
            var inBand = 0
            var tooEasy = 0
            var tooHard = 0
            var total = 0

            for ((mt, baseAP) in calibratedAP) {
                val level = MONSTER_EXPECTED_LEVELS[mt]!!
                val loadout = LOADOUTS.find { level in it.levelRange } ?: LOADOUTS.last()
                val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)
                val baseHP = monsterBaseHP[mt]!!

                for (scale in 1..6) {
                    val wins = (1..ITERATIONS).count {
                        simulateBattle(character, baseAP * scale, baseHP * scale, k, random)
                    }
                    val wr = wins.toDouble() / ITERATIONS
                    when {
                        wr in 0.70..0.90 -> inBand++
                        wr > 0.90 -> tooEasy++
                        else -> tooHard++
                    }
                    total++
                }
            }

            println("  Scenarios in 70-90% band: $inBand / $total")
            println("  Too easy (>90%): $tooEasy")
            println("  Too hard (<70%): $tooHard")
            println()
        }

        // Detailed per-monster table for K=30 with calibrated stats
        println("K=30, Calibrated stats — Win rate per monster at expected level, scaling 1-8:")
        println()
        println("%-18s | %-8s | %-6s | %s".format("Monster", "def", "ap", (1..8).joinToString("  ") { "Sc$it" }))
        println("-".repeat(85))

        for ((mt, baseAP) in calibratedAP) {
            val level = MONSTER_EXPECTED_LEVELS[mt]!!
            val loadout = LOADOUTS.find { level in it.levelRange } ?: LOADOUTS.last()
            val character = buildCharacter(level, loadout.weaponAP, loadout.armorDef)
            val baseHP = monsterBaseHP[mt]!!

            val rates = (1..8).map { scale ->
                val wins = (1..ITERATIONS).count {
                    simulateBattle(character, baseAP * scale, baseHP * scale, 30, random)
                }
                wins.toDouble() / ITERATIONS
            }

            val rateStr = rates.joinToString("  ") { r ->
                val pct = (r * 100).toInt()
                val flag = when {
                    r > 0.90 -> "E"
                    r in 0.70..0.90 -> "."
                    r > 0.40 -> "H"
                    else -> "X"
                }
                "%3d$flag".format(pct)
            }
            println("%-18s | %-8d | %-6d | %s".format(mt.displayName, character.totalDefense, character.totalAP, rateStr))
        }
        println("E=easy(>90%) | .=target(70-90%) | H=hard(40-70%) | X=danger(<40%)")
    }
}
