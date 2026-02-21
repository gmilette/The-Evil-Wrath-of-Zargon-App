package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.MonsterType
import org.junit.Test
import kotlin.random.Random

/**
 * Simulates current vs proposed power-law weapon redesign.
 *
 * Design goal: attackBonus ~ cost^0.55
 *   - Cheapest weapon gives most AP per gold
 *   - Each successive tier costs significantly more but adds less marginal AP
 *   - Clear differentiation between all 7 tiers
 *   - Atlantean Sword remains the top-end prestige weapon
 *
 * Current weapons:
 *   Dagger           cost=20  bonus=2
 *   Short Sword      cost=30  bonus=3
 *   Long Sword       cost=65  bonus=5
 *   Sword of Thorns  cost=88  bonus=7
 *   Broad Sword      cost=103 bonus=9
 *   Two-Handed Sword cost=250 bonus=15
 *   Atlantean Sword  cost=500 bonus=25
 *
 * Proposed power-law weapons (attackBonus ~ cost^0.55):
 *   Dagger           cost=20  bonus=5
 *   Short Sword      cost=45  bonus=8
 *   Long Sword       cost=100 bonus=13
 *   Sword of Thorns  cost=175 bonus=18
 *   Broad Sword      cost=280 bonus=23
 *   Two-Handed Sword cost=400 bonus=28
 *   Atlantean Sword  cost=600 bonus=35
 *
 * AP/G ratios (attack per gold):
 *   Dagger:           5/20  = 0.250  (best)
 *   Short Sword:      8/45  = 0.178
 *   Long Sword:      13/100 = 0.130
 *   Sword of Thorns: 18/175 = 0.103
 *   Broad Sword:     23/280 = 0.082
 *   Two-Handed Sword:28/400 = 0.070
 *   Atlantean Sword: 35/600 = 0.058  (worst)
 */
class WeaponRedesignSimulation {

    private val ITERATIONS = 2000
    private val SEED = 42L

    data class WeaponSpec(val name: String, val cost: Int, val bonus: Int)

    private val CURRENT_WEAPONS = listOf(
        WeaponSpec("Dagger",           20,  2),
        WeaponSpec("Short Sword",      30,  3),
        WeaponSpec("Long Sword",       65,  5),
        WeaponSpec("Sword of Thorns",  88,  7),
        WeaponSpec("Broad Sword",     103,  9),
        WeaponSpec("Two-Handed Sword",250, 15),
        WeaponSpec("Atlantean Sword", 500, 25)
    )

    private val PROPOSED_WEAPONS = listOf(
        WeaponSpec("Dagger",           20,  5),
        WeaponSpec("Short Sword",      45,  8),
        WeaponSpec("Long Sword",      100, 13),
        WeaponSpec("Sword of Thorns", 175, 18),
        WeaponSpec("Broad Sword",     280, 23),
        WeaponSpec("Two-Handed Sword",400, 28),
        WeaponSpec("Atlantean Sword", 600, 35)
    )

    private val GAME_TIERS = listOf(
        Triple(1,  0, "No weapon (base AP only)"),
        Triple(1,  5, "Dagger(+5)"),
        Triple(3,  8, "ShortSword(+8)"),
        Triple(5, 13, "LongSword(+13)"),
        Triple(7, 23, "BroadSword(+23)"),
        Triple(10, 28, "TwoHandedSword(+28)")
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
    fun `weapon power law redesign simulation`() {
        val random = Random(SEED)

        println("=================================================================")
        println("WEAPON POWER-LAW REDESIGN SIMULATION")
        println("=================================================================")
        println()

        printWeaponTable()
        println()

        printAttackContextTable()
        println()

        runTierComparison(random)
        println()

        runWeaponMattersCheck(random)
        println()

        runEndGameOffenseCheck(random)
        println()

        println("=================================================================")
        println("END OF SIMULATION")
        println("=================================================================")
    }

    private fun printWeaponTable() {
        println("--- WEAPON COMPARISON TABLE ---")
        println()
        println("%-18s | %8s | %8s | %8s | %8s | %8s | %8s | %10s"
            .format("Weapon", "OldCost", "NewCost", "OldBonus", "NewBonus", "OldAP/G", "NewAP/G", "CostChange"))
        println("-".repeat(100))
        for (i in CURRENT_WEAPONS.indices) {
            val cur = CURRENT_WEAPONS[i]
            val prop = PROPOSED_WEAPONS[i]
            val oldAPG = cur.bonus.toDouble() / cur.cost
            val newAPG = prop.bonus.toDouble() / prop.cost
            val costChange = if (prop.cost >= cur.cost) "+${prop.cost - cur.cost}" else "${prop.cost - cur.cost}"
            println("%-18s | %8d | %8d | %8d | %8d | %8s | %8s | %10s"
                .format(cur.name, cur.cost, prop.cost, cur.bonus, prop.bonus,
                    "%.3f".format(oldAPG), "%.3f".format(newAPG), costChange))
        }
        println()
        println("Key: AP/G = attack bonus per gold. Highest = best value for money.")
        println("Power law: bonus ~ cost^0.55 with strong diminishing returns.")
        println()
        println("Marginal efficiency (additional AP per additional gold spent on upgrade):")
        for (i in 0 until PROPOSED_WEAPONS.size - 1) {
            val a = PROPOSED_WEAPONS[i]
            val b = PROPOSED_WEAPONS[i + 1]
            val marginal = (b.bonus - a.bonus).toDouble() / (b.cost - a.cost)
            println("  %-18s -> %-18s : +%dg for +%d AP (%.4f AP/g marginal)"
                .format(a.name, b.name, b.cost - a.cost, b.bonus - a.bonus, marginal))
        }
    }

    private fun printAttackContextTable() {
        println("--- TOTAL ATTACK BY TIER (new model) ---")
        println("baseAP = 5 + (level-1)*3, totalAP = baseAP + weaponBonus")
        println()
        println("%-18s | %8s | %7s | %7s | %7s | %7s | %7s"
            .format("Weapon", "Bonus", "L1 AP", "L3 AP", "L5 AP", "L7 AP", "L10 AP"))
        println("-".repeat(80))

        for (prop in PROPOSED_WEAPONS) {
            val apAtLevel = { level: Int -> 5 + (level - 1) * 3 + prop.bonus }
            println("%-18s | %8d | %7d | %7d | %7d | %7d | %7d"
                .format(prop.name, prop.bonus,
                    apAtLevel(1), apAtLevel(3), apAtLevel(5), apAtLevel(7), apAtLevel(10)))
        }

        println()
        println("Monster HP reference (base * scaling):")
        println("  Slime x1=5   Bat x1=10   Babble x3=36  Spook x3=42  Beleth x5=80")
        println("  Skander x5=100  Necro x5=150  Necro x8=240  Necro x10=300")
    }

    private fun runTierComparison(random: Random) {
        println("--- TIER WIN RATE COMPARISON (avg across all regular monsters, scales 1-10) ---")
        println("Using Chain Mail armor (+30 DP) throughout for isolation of weapon effect")
        println()
        println("%-18s | %-28s | %-10s | %-10s | %-10s"
            .format("Weapon", "Tier (level/weapon)", "OldWR", "NewWR", "Delta"))
        println("-".repeat(88))

        val armorBonus = 30

        for ((index, cur) in CURRENT_WEAPONS.withIndex()) {
            val prop = PROPOSED_WEAPONS[index]
            val tierLevel = when (index) {
                0 -> 1
                1 -> 3
                2 -> 5
                3 -> 6
                4 -> 7
                5 -> 9
                6 -> 10
                else -> 10
            }
            val oldWR = avgWinRate(tierLevel, cur.bonus, armorBonus, random)
            val newWR = avgWinRate(tierLevel, prop.bonus, armorBonus, random)

            println("%-18s | %-28s | %8.1f%% | %8.1f%% | %+8.1f%%"
                .format(cur.name, "L$tierLevel old=+${cur.bonus} new=+${prop.bonus}",
                    oldWR * 100, newWR * 100, (newWR - oldWR) * 100))
        }
    }

    private fun runWeaponMattersCheck(random: Random) {
        println("--- WEAPON EFFECTIVENESS CHECK (distinct outcomes across tiers?) ---")
        println("Tests at level 5 vs Babble (mid threat), scales 3-7, Chain Mail armor (+30)")
        println()
        println("%-18s | %6s | %5s | %5s | %5s | %5s | %5s | %10s | %10s"
            .format("Weapon", "Bonus", "Sx3", "Sx4", "Sx5", "Sx6", "Sx7", "OldAvg3-7", "NewAvg3-7"))
        println("-".repeat(106))

        for (i in CURRENT_WEAPONS.indices) {
            val cur = CURRENT_WEAPONS[i]
            val prop = PROPOSED_WEAPONS[i]
            val level = 5
            val armorBonus = 30

            val oldRates = (3..7).map { scale ->
                simulateWinRate(level, cur.bonus, armorBonus, MonsterType.BABBLE, scale, random)
            }
            val newRates = (3..7).map { scale ->
                simulateWinRate(level, prop.bonus, armorBonus, MonsterType.BABBLE, scale, random)
            }

            println("%-18s | %6d | %4.0f%% | %4.0f%% | %4.0f%% | %4.0f%% | %4.0f%% | %9.1f%% | %9.1f%%"
                .format(prop.name, prop.bonus,
                    newRates[0]*100, newRates[1]*100, newRates[2]*100, newRates[3]*100, newRates[4]*100,
                    oldRates.average()*100, newRates.average()*100))
        }
    }

    private fun runEndGameOffenseCheck(random: Random) {
        println("--- END-GAME OFFENSE CHECK (Level 10 + Atlantean Sword vs hard monsters) ---")
        println("Goal: high-scale Necro/Skander should still require multiple hits, not be trivial")
        println()
        println("%-14s | %5s | %8s | %8s | %8s | %10s | %10s"
            .format("Monster", "Scale", "OldWR", "NewWR", "Delta", "OldHits", "NewHits"))
        println("-".repeat(82))

        val level = 10
        val curAtlantean = CURRENT_WEAPONS.last()
        val propAtlantean = PROPOSED_WEAPONS.last()
        val armorBonus = 42

        for (monster in listOf(MonsterType.BELETH, MonsterType.SKANDER_SNAKE, MonsterType.NECRO)) {
            for (scale in listOf(5, 6, 7, 8, 9, 10)) {
                val random2 = Random(SEED)
                val oldWR = simulateWinRate(level, curAtlantean.bonus, armorBonus, monster, scale, random2)
                val newWR = simulateWinRate(level, propAtlantean.bonus, armorBonus, monster, scale, random2)

                val oldAP = 5 + (level - 1) * 3 + curAtlantean.bonus
                val newAP = 5 + (level - 1) * 3 + propAtlantean.bonus
                val monsterHP = monster.baseDP * scale
                val oldHits = Math.ceil(monsterHP.toDouble() / oldAP)
                val newHits = Math.ceil(monsterHP.toDouble() / newAP)

                println("%-14s | %5d | %6.1f%% | %6.1f%% | %+7.1f%% | %9.1f | %9.1f"
                    .format(monster.displayName, scale,
                        oldWR * 100, newWR * 100, (newWR - oldWR) * 100,
                        oldHits, newHits))
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
