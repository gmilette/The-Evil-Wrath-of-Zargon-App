package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.Challenge
import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.PrestigeBonus
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.domain.progression.LevelingSystem
import com.greenopal.zargon.ui.screens.Armor
import com.greenopal.zargon.ui.screens.Weapon
import org.junit.Test
import kotlin.random.Random

/**
 * ChallengePrestigenSimulation
 *
 * Measures per-battle outcomes (win rate, avg turns to win, avg HP remaining after winning)
 * across all challenge modes and prestige bonus combinations.
 *
 * Good results: baseline win rate 70-90%, challenges noticeably harder,
 * prestige bonuses improve outcomes by 10-30% without trivializing challenges.
 */
class ChallengePrestigenSimulation {

    data class EquipmentTier(
        val name: String,
        val level: Int,
        val weapon: Weapon,
        val armor: Armor
    )

    data class BattleScenarioResult(
        val winRate: Double,
        val avgTurnsToWin: Double,
        val avgHPRemaining: Double,
        val sampleSize: Int
    )

    private val tiers = listOf(
        EquipmentTier("Early   (L1)", 1, Weapon.DAGGER, Armor.CLOTH),
        EquipmentTier("Early-Mid(L3)", 3, Weapon.SHORT_SWORD, Armor.LEATHER),
        EquipmentTier("Mid     (L5)", 5, Weapon.LONG_SWORD, Armor.PLATED_LEATHER),
        EquipmentTier("Late-Mid (L7)", 7, Weapon.BROAD_SWORD, Armor.CHAIN_MAIL),
        EquipmentTier("Late    (L8)", 8, Weapon.ATLANTEAN_SWORD, Armor.PLATEMAIL),
        EquipmentTier("Late    (L9)", 9, Weapon.ATLANTEAN_SWORD, Armor.PLATEMAIL),
        EquipmentTier("End     (L10)", 10, Weapon.ATLANTEAN_SWORD, Armor.PLATEMAIL)
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

    private fun runBattleScenario(
        tier: EquipmentTier,
        simulator: BattleSimulator,
        random: Random,
        config: ChallengeConfig?,
        prestige: PrestigeData?
    ): BattleScenarioResult {
        var wins = 0
        var totalTurnsOnWin = 0
        var totalHPOnWin = 0

        repeat(iterations) {
            val character = createCharacter(tier.level, tier.weapon, tier.armor)
            val monsterType = SimulationHelpers.selectRandomMonster(tier.level, random)
            val monster = SimulationHelpers.createMonsterWithRandomScaling(monsterType, tier.level, random, config)
            val log = simulator.simulateBattle(character, monster, config, prestige)

            if (log.outcome == BattleSimulator.BattleOutcome.PlayerVictory) {
                wins++
                totalTurnsOnWin += log.turnsElapsed
                totalHPOnWin += log.playerHPRemaining
            }
        }

        val winRate = wins.toDouble() / iterations
        val avgTurns = if (wins > 0) totalTurnsOnWin.toDouble() / wins else 0.0
        val avgHP = if (wins > 0) totalHPOnWin.toDouble() / wins else 0.0

        return BattleScenarioResult(winRate, avgTurns, avgHP, wins)
    }

    private fun makePrestige(vararg bonuses: PrestigeBonus): PrestigeData {
        val bonusSet = bonuses.toSet()
        return PrestigeData(unlockedBonuses = bonusSet, activeBonuses = bonusSet)
    }

    private fun printRow(label: String, result: BattleScenarioResult) {
        println(String.format(
            "  %-35s  Win: %5.1f%%  AvgTurns: %5.1f  AvgHP: %5.1f",
            label,
            result.winRate * 100,
            result.avgTurnsToWin,
            result.avgHPRemaining
        ))
    }

    @Test
    fun `evaluate challenge and prestige battle outcomes`() {
        val random = Random(42)
        val simulator = BattleSimulator(random)

        println()
        println("=== CHALLENGE + PRESTIGE PER-BATTLE SIMULATION ===")
        println("Iterations per scenario: $iterations")
        println("Metrics: Win Rate | Avg Turns to Win (victories only) | Avg HP Remaining (victories only)")
        println("Balance targets: Baseline win rate 70-90%, challenges harder, prestige +10-30%")
        println()

        for (tier in tiers) {
            println("=" .repeat(80))
            println("TIER: ${tier.name}  |  Weapon: ${tier.weapon.displayName} (+${tier.weapon.attackBonus})  |  Armor: ${tier.armor.displayName} (+${tier.armor.defenseBonus})")
            println("=" .repeat(80))

            val baseline = runBattleScenario(tier, simulator, random, null, null)
            printRow("Baseline (no challenge, no prestige)", baseline)
            println()

            println("  -- Prestige bonuses only (no challenge) --")
            val gwOnly = runBattleScenario(tier, simulator, random, null, makePrestige(PrestigeBonus.GREAT_WEAPONS))
            printRow("  + GREAT_WEAPONS only (2.5x weapon)", gwOnly)
            val gaOnly = runBattleScenario(tier, simulator, random, null, makePrestige(PrestigeBonus.GREATER_ARMOR))
            printRow("  + GREATER_ARMOR only (2.0x armor)", gaOnly)
            val msOnly = runBattleScenario(tier, simulator, random, null, makePrestige(PrestigeBonus.MASTER_SPELLBOOK))
            printRow("  + MASTER_SPELLBOOK only (1.5x spells)", msOnly)
            val gwGa = runBattleScenario(tier, simulator, random, null, makePrestige(PrestigeBonus.GREAT_WEAPONS, PrestigeBonus.GREATER_ARMOR))
            printRow("  + GREAT_WEAPONS + GREATER_ARMOR", gwGa)
            println()

            println("  -- Challenges vs baseline --")
            for (challenge in Challenge.values()) {
                val config = ChallengeConfig(challenges = setOf(challenge))
                val noBonus = runBattleScenario(tier, simulator, random, config, null)
                printRow("Challenge: ${challenge.name}", noBonus)
            }
            println()

            println("  -- Key challenge + prestige combos --")
            val mageQuestConfig = ChallengeConfig(challenges = setOf(Challenge.MAGE_QUEST))
            val mqBase = runBattleScenario(tier, simulator, random, mageQuestConfig, null)
            printRow("MAGE_QUEST (no prestige)", mqBase)
            val mqGW = runBattleScenario(tier, simulator, random, mageQuestConfig, makePrestige(PrestigeBonus.GREAT_WEAPONS))
            printRow("MAGE_QUEST + GREAT_WEAPONS", mqGW)
            val mqGA = runBattleScenario(tier, simulator, random, mageQuestConfig, makePrestige(PrestigeBonus.GREATER_ARMOR))
            printRow("MAGE_QUEST + GREATER_ARMOR", mqGA)
            val mqGWGA = runBattleScenario(tier, simulator, random, mageQuestConfig, makePrestige(PrestigeBonus.GREAT_WEAPONS, PrestigeBonus.GREATER_ARMOR))
            printRow("MAGE_QUEST + GREAT_WEAPONS + GREATER_ARMOR", mqGWGA)

            val warriorConfig = ChallengeConfig(challenges = setOf(Challenge.WARRIOR_MODE))
            val wmBase = runBattleScenario(tier, simulator, random, warriorConfig, null)
            printRow("WARRIOR_MODE (no prestige)", wmBase)
            val wmGW = runBattleScenario(tier, simulator, random, warriorConfig, makePrestige(PrestigeBonus.GREAT_WEAPONS))
            printRow("WARRIOR_MODE + GREAT_WEAPONS", wmGW)

            val strongerConfig = ChallengeConfig(challenges = setOf(Challenge.STRONGER_ENEMIES))
            val seBase = runBattleScenario(tier, simulator, random, strongerConfig, null)
            printRow("STRONGER_ENEMIES (no prestige)", seBase)
            val seGA = runBattleScenario(tier, simulator, random, strongerConfig, makePrestige(PrestigeBonus.GREATER_ARMOR))
            printRow("STRONGER_ENEMIES + GREATER_ARMOR", seGA)

            val impossConfig = ChallengeConfig(challenges = setOf(Challenge.IMPOSSIBLE_MISSION))
            val imBase = runBattleScenario(tier, simulator, random, impossConfig, null)
            printRow("IMPOSSIBLE_MISSION (no prestige)", imBase)
            val imAll = runBattleScenario(tier, simulator, random, impossConfig, makePrestige(PrestigeBonus.GREAT_WEAPONS, PrestigeBonus.GREATER_ARMOR))
            printRow("IMPOSSIBLE_MISSION + GW + GA", imAll)

            println()
        }

        println("=".repeat(80))
        println("SIMULATION COMPLETE")
        println("=".repeat(80))
    }
}
