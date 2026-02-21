package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.PrestigeBonus
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.domain.challenges.PrestigeSystem
import com.greenopal.zargon.domain.progression.LevelingSystem
import com.greenopal.zargon.ui.screens.Armor
import com.greenopal.zargon.ui.screens.Weapon
import org.junit.Test
import kotlin.random.Random

class ProgressionSimulation {

    data class EquipmentTier(
        val name: String,
        val level: Int,
        val weapon: Weapon,
        val armor: Armor
    )

    private val tiers = listOf(
        EquipmentTier("Early", 1, Weapon.DAGGER, Armor.CLOTH),
        EquipmentTier("Early-Mid", 3, Weapon.SHORT_SWORD, Armor.LEATHER),
        EquipmentTier("Mid", 5, Weapon.LONG_SWORD, Armor.CHAIN_MAIL),
        EquipmentTier("Late-Mid", 7, Weapon.BROAD_SWORD, Armor.CHAIN_MAIL),
        EquipmentTier("End", 10, Weapon.TWOHANDED_SWORD, Armor.PLATEMAIL)
    )

    private val weaponTiers = Weapon.values().toList()
    private val armorTiers = Armor.values().toList()
    private val iterations = 1000
    private val levelingSystem = LevelingSystem()
    private val rewardSystem = SimulationHelpers.rewardSystem
    private val prestigeSystem = PrestigeSystem()

    private fun createCharacter(level: Int, weapon: Weapon, armor: Armor): CharacterStats {
        var character = CharacterStats(weaponBonus = weapon.attackBonus, armorBonus = armor.defenseBonus)
        for (lev in 1 until level) {
            character = levelingSystem.levelUp(character)
        }
        return character.copy(weaponBonus = weapon.attackBonus, armorBonus = armor.defenseBonus)
    }

    data class ShortProgressionResult(
        val battlesToTargetLevel: Int,
        val winRate: Float
    )

    private fun simulateShortProgression(
        tier: EquipmentTier,
        simulator: BattleSimulator,
        random: Random
    ): ShortProgressionResult {
        var character = createCharacter(tier.level, tier.weapon, tier.armor)
        var totalBattles = 0
        var wins = 0
        val targetLevel = tier.level + 2
        var nextLevelXP = rewardSystem.getInitialNextLevelXP()
        for (lev in 1 until character.level) {
            nextLevelXP = rewardSystem.calculateXPForNextLevel(lev, nextLevelXP)
        }

        while (character.level < targetLevel && totalBattles < 500) {
            val monsterType = SimulationHelpers.selectRandomMonster(character.level, random)
            val monster = SimulationHelpers.createMonsterWithRandomScaling(monsterType, character.level, random)

            val log = simulator.simulateBattle(character, monster)
            totalBattles++

            if (log.outcome == BattleSimulator.BattleOutcome.PlayerVictory) {
                wins++
                val xpGained = rewardSystem.calculateXP(monsterType, monster.scalingFactor)
                val goldGained = rewardSystem.calculateGold(monsterType, monster.scalingFactor)
                character = character.copy(
                    currentHP = log.playerHPRemaining,
                    experience = character.experience + xpGained,
                    gold = character.gold + goldGained
                )

                if (character.experience >= nextLevelXP) {
                    nextLevelXP = rewardSystem.calculateXPForNextLevel(character.level, nextLevelXP)
                    character = levelingSystem.levelUp(character)
                }
            } else {
                character = character.copy(
                    currentHP = character.maxHP,
                    currentMP = character.maxMP
                )
            }
        }

        return ShortProgressionResult(totalBattles, if (totalBattles > 0) wins.toFloat() / totalBattles else 0f)
    }

    private fun tryBuyEquipment(character: CharacterStats): CharacterStats {
        var stats = character
        val nextWeaponIdx = stats.weaponStatus + 1
        if (nextWeaponIdx < weaponTiers.size) {
            val nextWeapon = weaponTiers[nextWeaponIdx]
            if (stats.gold >= nextWeapon.basePrice) {
                stats = stats.copy(
                    gold = stats.gold - nextWeapon.basePrice,
                    weaponBonus = nextWeapon.attackBonus,
                    weaponStatus = nextWeaponIdx
                )
            }
        }
        val nextArmorIdx = stats.armorStatus + 1
        if (nextArmorIdx < armorTiers.size) {
            val nextArmor = armorTiers[nextArmorIdx]
            if (stats.gold >= nextArmor.basePrice) {
                stats = stats.copy(
                    gold = stats.gold - nextArmor.basePrice,
                    armorBonus = nextArmor.defenseBonus,
                    armorStatus = nextArmorIdx
                )
            }
        }
        return stats
    }

    data class FullProgressionResult(
        val battlesToTarget: Int,
        val battlesToAllEquipment: Int,
        val finalLevel: Int,
        val winRate: Float
    )

    private fun simulateFullProgression(
        simulator: BattleSimulator,
        random: Random,
        prestige: PrestigeData? = null
    ): FullProgressionResult {
        var character = CharacterStats(
            weaponBonus = 0,
            armorBonus = 0,
            weaponStatus = -1,
            armorStatus = -1
        )
        if (prestige != null) {
            character = prestigeSystem.applyPrestigeBonusesToCharacter(character, prestige)
        }
        var totalBattles = 0
        var wins = 0
        var battlesToAllEquipment = -1
        val maxWeaponStatus = weaponTiers.size - 1
        val maxArmorStatus = armorTiers.size - 1
        var nextLevelXP = rewardSystem.getInitialNextLevelXP()
        val maxBattles = 5000

        val xpMultiplier = if (prestige != null) prestigeSystem.getXPMultiplier(prestige) else 1.0f
        val goldMultiplier = if (prestige != null) prestigeSystem.getGoldMultiplier(prestige) else 1.0f

        while (character.level < 10 && totalBattles < maxBattles) {
            val monsterType = SimulationHelpers.selectRandomMonster(character.level, random)
            val monster = SimulationHelpers.createMonsterWithRandomScaling(monsterType, character.level, random)

            val log = simulator.simulateBattle(character, monster, prestige = prestige)
            totalBattles++

            if (log.outcome == BattleSimulator.BattleOutcome.PlayerVictory) {
                wins++
                val baseXP = rewardSystem.calculateXP(monsterType, monster.scalingFactor)
                val baseGold = rewardSystem.calculateGold(monsterType, monster.scalingFactor)
                val xpGained = (baseXP * xpMultiplier).toInt()
                val goldGained = (baseGold * goldMultiplier).toInt()
                character = character.copy(
                    currentHP = log.playerHPRemaining,
                    experience = character.experience + xpGained,
                    gold = character.gold + goldGained
                )

                character = tryBuyEquipment(character)

                if (battlesToAllEquipment == -1 &&
                    character.weaponStatus == maxWeaponStatus &&
                    character.armorStatus == maxArmorStatus) {
                    battlesToAllEquipment = totalBattles
                }

                if (character.experience >= nextLevelXP) {
                    nextLevelXP = rewardSystem.calculateXPForNextLevel(character.level, nextLevelXP)
                    character = levelingSystem.levelUp(character)
                }
            } else {
                character = character.copy(
                    currentHP = character.maxHP,
                    currentMP = character.maxMP
                )
            }
        }

        if (battlesToAllEquipment == -1) {
            battlesToAllEquipment = totalBattles
        }

        return FullProgressionResult(
            totalBattles,
            battlesToAllEquipment,
            character.level,
            if (totalBattles > 0) wins.toFloat() / totalBattles else 0f
        )
    }

    private fun runFullProgression(
        simulator: BattleSimulator,
        random: Random,
        prestige: PrestigeData? = null
    ): List<FullProgressionResult> {
        return (1..iterations).map { simulateFullProgression(simulator, random, prestige) }
    }

    private fun printFullProgressionStats(label: String, results: List<FullProgressionResult>) {
        val battleCounts = results.map { it.battlesToTarget }.sorted()
        val equipCounts = results.map { it.battlesToAllEquipment }.sorted()
        val reachedTarget = results.count { it.finalLevel >= 10 }
        val avgWinRate = results.map { it.winRate }.average()

        println("  [$label]")
        println(String.format("  Reached level 10:  %d / %d (%.1f%%)", reachedTarget, iterations, reachedTarget * 100.0 / iterations))
        println(String.format("  Avg win rate:      %.1f%%", avgWinRate * 100))
        println(String.format("  Avg battles to L10:  %10.1f   P50: %d   P80: %d", battleCounts.average(), battleCounts[battleCounts.size / 2], battleCounts[(battleCounts.size * 0.8).toInt()]))
        println(String.format("  Avg battles to equip: %9.1f   P50: %d   P80: %d", equipCounts.average(), equipCounts[equipCounts.size / 2], equipCounts[(equipCounts.size * 0.8).toInt()]))
        println()
    }

    @Test
    fun `simulate progression per tier`() {
        val random = Random(12345)
        val simulator = BattleSimulator(random)

        println("=== PROGRESSION SIMULATION: Battles to Gain 2 Levels ===")
        println("Iterations per tier: $iterations")
        println()
        println(String.format(
            "%-12s %-6s %-20s %-20s %12s %12s %12s %10s",
            "Phase", "Level", "Weapon", "Armor", "Avg Battles", "P50", "P80", "Win Rate"
        ))
        println("-".repeat(110))

        for (tier in tiers) {
            val results = (1..iterations).map {
                simulateShortProgression(tier, simulator, random)
            }

            val battleCounts = results.map { it.battlesToTargetLevel }.sorted()
            val avgWinRate = results.map { it.winRate }.average()

            println(String.format(
                "%-12s %-6d %-20s %-20s %12.1f %12d %12d %9.1f%%",
                tier.name,
                tier.level,
                tier.weapon.displayName,
                tier.armor.displayName,
                battleCounts.average(),
                battleCounts[battleCounts.size / 2],
                battleCounts[(battleCounts.size * 0.8).toInt()],
                avgWinRate * 100
            ))
        }

        println()
        println("=== FULL PROGRESSION: Level 1 to Level 10 with Equipment Purchases ===")
        println("Start: Level 1, no equipment, 0 gold (unless Starting Gold bonus)")
        println("Buys next weapon/armor tier as soon as affordable")
        println("On death: full HP/MP restore (no permadeath)")
        println("Iterations: $iterations")
        println()

        val combatBonuses = listOf(
            PrestigeBonus.GREAT_WEAPONS,
            PrestigeBonus.GREATER_ARMOR,
            PrestigeBonus.MASTER_SPELLBOOK
        )

        val allBonuses = PrestigeBonus.values().toSet()
        val allBonusPrestige = PrestigeData(
            unlockedBonuses = allBonuses,
            activeBonuses = allBonuses
        )

        val baselineResults = runFullProgression(simulator, random)
        printFullProgressionStats("No Bonus", baselineResults)

        for (bonus in combatBonuses) {
            val prestige = PrestigeData(
                unlockedBonuses = setOf(bonus),
                activeBonuses = setOf(bonus)
            )
            val results = runFullProgression(simulator, random, prestige)
            printFullProgressionStats(bonus.displayName, results)
        }

        val allResults = runFullProgression(simulator, random, allBonusPrestige)
        printFullProgressionStats("ALL BONUSES", allResults)

        println("Simulation complete.")
    }
}
