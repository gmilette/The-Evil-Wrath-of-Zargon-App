package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import org.junit.Test
import kotlin.random.Random

/**
 * Simulates current vs proposed power-law armor redesign.
 *
 * Design goal: defense ~ cost^0.55
 *   - Cheapest armor gives most defense per gold
 *   - Each successive tier costs significantly more but adds less marginal defense
 *   - Spiked Leather and Plated Leather are clearly differentiated
 *   - Platemail end-game bonus is reduced enough to keep high-scale monsters threatening
 *
 * Current armor:
 *   Cloth          cost=20  bonus=5
 *   Leather        cost=40  bonus=10
 *   Plated Leather cost=88  bonus=18
 *   Spiked Leather cost=98  bonus=20
 *   Chain Mail     cost=134 bonus=28
 *   Platemail      cost=279 bonus=50
 *
 * Proposed power-law armor (defense ~ cost^0.55, k=0.955, calibrated to Cloth=5 at cost=15):
 *   Cloth          cost=15  bonus=5
 *   Leather        cost=35  bonus=8
 *   Plated Leather cost=80  bonus=15
 *   Spiked Leather cost=160 bonus=20
 *   Chain Mail     cost=300 bonus=30
 *   Platemail      cost=550 bonus=42
 *
 * D/G ratios (defense per gold):
 *   Cloth:          5/15  = 0.333  (best)
 *   Leather:        8/35  = 0.229
 *   Plated Leather: 13/80 = 0.163
 *   Spiked Leather: 20/160= 0.125
 *   Chain Mail:     30/300= 0.100
 *   Platemail:      42/550= 0.076  (worst)
 */
class ArmorRedesignSimulation {

    private val ITERATIONS = 2000
    private val SEED = 42L

    data class ArmorSpec(val name: String, val cost: Int, val bonus: Int)

    private val CURRENT_ARMORS = listOf(
        ArmorSpec("Cloth",          20,  5),
        ArmorSpec("Leather",        40, 10),
        ArmorSpec("Plated Leather", 88, 18),
        ArmorSpec("Spiked Leather", 98, 20),
        ArmorSpec("Chain Mail",    134, 28),
        ArmorSpec("Platemail",     279, 50)
    )

    private val PROPOSED_ARMORS = listOf(
        ArmorSpec("Cloth",          15,  5),
        ArmorSpec("Leather",        35,  8),
        ArmorSpec("Plated Leather", 80, 15),
        ArmorSpec("Spiked Leather",160, 20),
        ArmorSpec("Chain Mail",    300, 30),
        ArmorSpec("Platemail",     550, 42)
    )

    private val GAME_TIERS = listOf(
        Triple(1,  2, "Dagger(+2)"),
        Triple(3,  3, "ShortSword(+3)"),
        Triple(5,  5, "LongSword(+5)"),
        Triple(7,  9, "BroadSword(+9)"),
        Triple(10, 15, "TwoHandedSword(+15)")
    )

    private val REGULAR_MONSTERS = listOf(
        MonsterType.SLIME,
        MonsterType.BAT,
        MonsterType.BABBLE,
        MonsterType.SPOOK,
        MonsterType.BELETH,
        MonsterType.SKANDER_SNAKE,
        MonsterType.NECRO
    )

    @Test
    fun `armor power law redesign simulation`() {
        val random = Random(SEED)

        println("=================================================================")
        println("ARMOR POWER-LAW REDESIGN SIMULATION")
        println("=================================================================")
        println()

        printArmorTable()
        println()

        printDefenseContextTable()
        println()

        runTierComparison(random)
        println()

        runArmorMattersCheck(random)
        println()

        runEndGameTensionCheck(random)
        println()

        println("=================================================================")
        println("END OF SIMULATION")
        println("=================================================================")
    }

    private fun printArmorTable() {
        println("--- ARMOR COMPARISON TABLE ---")
        println()
        println("%-16s | %8s | %8s | %8s | %8s | %8s | %8s | %10s"
            .format("Armor", "OldCost", "NewCost", "OldBonus", "NewBonus", "OldD/G", "NewD/G", "CostChange"))
        println("-".repeat(95))
        for (i in CURRENT_ARMORS.indices) {
            val cur = CURRENT_ARMORS[i]
            val prop = PROPOSED_ARMORS[i]
            val oldDG = cur.bonus.toDouble() / cur.cost
            val newDG = prop.bonus.toDouble() / prop.cost
            val costChange = "+${prop.cost - cur.cost}"
            println("%-16s | %8d | %8d | %8d | %8d | %8s | %8s | %10s"
                .format(cur.name, cur.cost, prop.cost, cur.bonus, prop.bonus,
                    "%.3f".format(oldDG), "%.3f".format(newDG), costChange))
        }
        println()
        println("Key: D/G = defense per gold. Highest = best value for money.")
        println("Power law exponent ~0.55: bonus = 0.955 * cost^0.55")
    }

    private fun printDefenseContextTable() {
        println("--- TOTAL DEFENSE BY TIER AND ARMOR (new model) ---")
        println("baseDP = 20 + (level-1)*4, totalDefense = baseDP + armorBonus")
        println()
        println("%-16s | %8s | %6s | %6s | %6s | %6s | %6s | %6s"
            .format("Armor", "Bonus", "L1 def", "L3 def", "L5 def", "L7 def", "L10 def", "Change"))
        println("-".repeat(78))

        for (i in CURRENT_ARMORS.indices) {
            val cur = CURRENT_ARMORS[i]
            val prop = PROPOSED_ARMORS[i]
            val defAtLevel = { level: Int -> 20 + (level - 1) * 4 + prop.bonus }
            val oldDefL10 = 20 + 9 * 4 + cur.bonus
            val newDefL10 = defAtLevel(10)
            println("%-16s | %8d | %6d | %6d | %6d | %6d | %6d | %6s"
                .format(prop.name, prop.bonus,
                    defAtLevel(1), defAtLevel(3), defAtLevel(5), defAtLevel(7), defAtLevel(10),
                    "${newDefL10 - oldDefL10}"))
        }

        println()
        println("Monster AP reference (base * scaling):")
        println("  Beleth x5  = 8*5=40   Skander x5 = 12*5=60  Necro x5 = 13*5=65")
        println("  Beleth x8  = 8*8=64   Skander x8 = 12*8=96  Necro x8 = 13*8=104")
        println("  Beleth x10 = 8*10=80  Skander x10= 12*10=120 Necro x10= 13*10=130")
    }

    private fun runTierComparison(random: Random) {
        println("--- TIER WIN RATE COMPARISON (avg across all regular monsters, scales 1-10) ---")
        println()
        println("%-16s | %-20s | %-10s | %-10s | %-10s"
            .format("Armor", "Tier (level/weapon)", "OldWR", "NewWR", "Delta"))
        println("-".repeat(80))

        for ((level, weaponBonus, tierLabel) in GAME_TIERS) {
            val tierArmorIndex = when {
                level <= 1 -> 0
                level <= 3 -> 1
                level <= 5 -> 3
                level <= 7 -> 4
                else -> 5
            }

            val curArmor = CURRENT_ARMORS[tierArmorIndex]
            val propArmor = PROPOSED_ARMORS[tierArmorIndex]

            val oldWR = avgWinRate(level, weaponBonus, curArmor.bonus, random)
            val newWR = avgWinRate(level, weaponBonus, propArmor.bonus, random)

            println("%-16s | %-20s | %8.1f%% | %8.1f%% | %+8.1f%%"
                .format(curArmor.name, "L$level $tierLabel",
                    oldWR * 100, newWR * 100, (newWR - oldWR) * 100))
        }
    }

    private fun runArmorMattersCheck(random: Random) {
        println("--- ARMOR EFFECTIVENESS CHECK (do armor tiers produce distinct outcomes?) ---")
        println("Tests at level 5 vs Skander Snake (mid-high threat), scales 4-8")
        println()
        println("%-16s | %6s | %6s | %6s | %6s | %6s | %6s | %10s | %10s"
            .format("Armor", "Bonus", "Sx4", "Sx5", "Sx6", "Sx7", "Sx8", "OldAvg4-8", "NewAvg4-8"))
        println("-".repeat(100))

        for (i in CURRENT_ARMORS.indices) {
            val cur = CURRENT_ARMORS[i]
            val prop = PROPOSED_ARMORS[i]
            val level = 5
            val weaponBonus = 5

            val oldRates = (4..8).map { scale ->
                simulateWinRate(level, weaponBonus, cur.bonus, MonsterType.SKANDER_SNAKE, scale, random)
            }
            val newRates = (4..8).map { scale ->
                simulateWinRate(level, weaponBonus, prop.bonus, MonsterType.SKANDER_SNAKE, scale, random)
            }

            println("%-16s | %6d | %5.0f%% | %5.0f%% | %5.0f%% | %5.0f%% | %5.0f%% | %9.1f%% | %9.1f%%"
                .format(prop.name, prop.bonus,
                    newRates[0]*100, newRates[1]*100, newRates[2]*100, newRates[3]*100, newRates[4]*100,
                    oldRates.average()*100, newRates.average()*100))
        }
    }

    private fun runEndGameTensionCheck(random: Random) {
        println("--- END-GAME TENSION CHECK (Level 10 + Platemail vs hard monsters) ---")
        println("Goal: Necro/Skander at high scales should still threaten (not always 1 damage)")
        println()
        println("%-14s | %5s | %8s | %8s | %8s | %8s"
            .format("Monster", "Scale", "OldWR", "NewWR", "Delta", "NewDmg/hit"))
        println("-".repeat(72))

        val level = 10
        val weaponBonus = 15
        val curPlate = CURRENT_ARMORS.last()
        val propPlate = PROPOSED_ARMORS.last()

        for (monster in listOf(MonsterType.BELETH, MonsterType.SKANDER_SNAKE, MonsterType.NECRO)) {
            for (scale in listOf(5, 6, 7, 8, 9, 10)) {
                val random2 = Random(SEED)
                val oldWR = simulateWinRate(level, weaponBonus, curPlate.bonus, monster, scale, random2)
                val newWR = simulateWinRate(level, weaponBonus, propPlate.bonus, monster, scale, random2)

                val oldDef = 20 + (level - 1) * 4 + curPlate.bonus
                val newDef = 20 + (level - 1) * 4 + propPlate.bonus
                val monsterAP = monster.baseAP * scale
                val avgDmgNew = maxOf(1.0, monsterAP.toDouble() - newDef + scale / 2.0)

                println("%-14s | %5d | %6.1f%% | %6.1f%% | %+7.1f%% | %7.1f"
                    .format(monster.displayName, scale,
                        oldWR * 100, newWR * 100, (newWR - oldWR) * 100, avgDmgNew))
            }
            println()
        }
    }

    private fun avgWinRate(level: Int, weaponBonus: Int, armorBonus: Int, random: Random): Double {
        var totalWR = 0.0
        var count = 0
        for (monster in REGULAR_MONSTERS) {
            for (scale in 1..10) {
                totalWR += simulateWinRate(level, weaponBonus, armorBonus, monster, scale, random)
                count++
            }
        }
        return totalWR / count
    }

    private fun simulateWinRate(
        level: Int,
        weaponBonus: Int,
        armorBonus: Int,
        monsterType: MonsterType,
        scale: Int,
        random: Random
    ): Double {
        val baseDP = 20 + (level - 1) * 4
        val maxHP  = 20 + (level - 1) * 5
        val baseAP = 5  + (level - 1) * 3
        val totalAP = baseAP + weaponBonus
        val totalDef = baseDP + armorBonus
        val monsterAP = monsterType.baseAP * scale
        val monsterHP = monsterType.baseDP * scale

        var wins = 0
        repeat(ITERATIONS) {
            var playerHP = maxHP
            var mHP = monsterHP

            while (playerHP > 0 && mHP > 0) {
                mHP -= totalAP
                if (mHP <= 0) { wins++; break }
                val rand = random.nextInt(0, scale + 1)
                val dmg = maxOf(1, monsterAP - totalDef + rand)
                playerHP -= dmg
            }
        }
        return wins.toDouble() / ITERATIONS
    }
}
