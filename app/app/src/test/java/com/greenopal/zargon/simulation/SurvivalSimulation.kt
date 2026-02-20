package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.PrestigeBonus
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.domain.progression.LevelingSystem
import com.greenopal.zargon.ui.screens.Armor
import com.greenopal.zargon.ui.screens.Weapon
import org.junit.Test
import kotlin.random.Random

class SurvivalSimulation {

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

    private val iterations = 1000
    private val levelingSystem = LevelingSystem()

    private fun createCharacter(level: Int, weapon: Weapon, armor: Armor): CharacterStats {
        var character = CharacterStats(weaponBonus = weapon.attackBonus, armorBonus = armor.defenseBonus)
        for (lev in 1 until level) {
            character = levelingSystem.levelUp(character)
        }
        return character.copy(weaponBonus = weapon.attackBonus, armorBonus = armor.defenseBonus)
    }

    private fun simulateSurvivalRun(
        tier: EquipmentTier,
        simulator: BattleSimulator,
        random: Random,
        prestige: PrestigeData? = null
    ): Int {
        var character = createCharacter(tier.level, tier.weapon, tier.armor)
        var battleCount = 0

        while (character.isAlive) {
            val monsterType = SimulationHelpers.selectRandomMonster(tier.level, random)
            val monster = SimulationHelpers.createMonsterWithRandomScaling(monsterType, tier.level, random)
            val log = simulator.simulateBattle(character, monster, prestige = prestige)
            battleCount++

            if (log.outcome == BattleSimulator.BattleOutcome.PlayerDefeat) break

            character = character.copy(currentDP = log.playerHPRemaining)
        }

        return battleCount
    }

    private fun runSurvival(
        tier: EquipmentTier,
        simulator: BattleSimulator,
        random: Random,
        prestige: PrestigeData? = null
    ): Double {
        return (1..iterations).map {
            simulateSurvivalRun(tier, simulator, random, prestige)
        }.average()
    }

    @Test
    fun `simulate battles before dying`() {
        val random = Random(12345)
        val simulator = BattleSimulator(random)

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

        println("=== SURVIVAL SIMULATION ===")
        println("Avg consecutive battles survived before dying (no healing between battles)")
        println("Iterations per scenario: $iterations")
        println()

        for (tier in tiers) {
            println("--- ${tier.name}: Level ${tier.level}, ${tier.weapon.displayName}, ${tier.armor.displayName} ---")

            val baseline = runSurvival(tier, simulator, random)

            val bonusResults = combatBonuses.map { bonus ->
                val prestige = PrestigeData(
                    unlockedBonuses = setOf(bonus),
                    activeBonuses = setOf(bonus)
                )
                runSurvival(tier, simulator, random, prestige)
            }

            val allResult = runSurvival(tier, simulator, random, allBonusPrestige)

            val header = String.format(
                "  %-12s %10s %10s %10s %10s",
                "No Bonus",
                combatBonuses[0].name.take(10),
                combatBonuses[1].name.take(10),
                combatBonuses[2].name.take(10),
                "ALL"
            )
            println(header)
            println(String.format(
                "  %-12.1f %10.1f %10.1f %10.1f %10.1f",
                baseline,
                bonusResults[0],
                bonusResults[1],
                bonusResults[2],
                allResult
            ))
            println()
        }

        println("Simulation complete.")
    }
}
