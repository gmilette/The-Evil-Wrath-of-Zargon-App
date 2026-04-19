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
import kotlin.math.pow
import kotlin.random.Random

/**
 * Focused simulation: how much do prestige bonuses help against
 * STRONGER_ENEMIES and WARRIOR_MODE, the two hardest challenges?
 *
 * Reports per-battle win rate AND 10-battle survival (winRate^10)
 * so we can see if any prestige combo makes these challenges playable.
 */
class PrestigeVsChallengeSimulation {

    data class EquipmentTier(val name: String, val level: Int, val weapon: Weapon, val armor: Armor)

    private val tiers = listOf(
        EquipmentTier("L1  Dagger+Cloth (starter)",      1,  Weapon.DAGGER,          Armor.CLOTH),
        EquipmentTier("L1  LongSword+Cloth (rich start)",1,  Weapon.LONG_SWORD,      Armor.CLOTH),
        EquipmentTier("L1  Dagger+Leather (rich start)", 1,  Weapon.DAGGER,          Armor.LEATHER),
        EquipmentTier("L2  ShortSword+Leather",          2,  Weapon.SHORT_SWORD,     Armor.LEATHER),
        EquipmentTier("L3  ShortSword+Leather",          3,  Weapon.SHORT_SWORD,     Armor.LEATHER),
        EquipmentTier("L4  LongSword+PlatedLeather",     4,  Weapon.LONG_SWORD,      Armor.PLATED_LEATHER),
        EquipmentTier("L5  LongSword+PlatedLeather",     5,  Weapon.LONG_SWORD,      Armor.PLATED_LEATHER),
        EquipmentTier("L6  BroadSword+ChainMail",        6,  Weapon.BROAD_SWORD,     Armor.CHAIN_MAIL),
        EquipmentTier("L7  BroadSword+ChainMail",        7,  Weapon.BROAD_SWORD,     Armor.CHAIN_MAIL),
        EquipmentTier("L8  AtlanteanSword+Platemail",    8,  Weapon.ATLANTEAN_SWORD, Armor.PLATEMAIL),
        EquipmentTier("L9  AtlanteanSword+Platemail",    9,  Weapon.ATLANTEAN_SWORD, Armor.PLATEMAIL),
        EquipmentTier("L10 AtlanteanSword+Platemail",    10, Weapon.ATLANTEAN_SWORD, Armor.PLATEMAIL)
    )

    private val iterations = 2000
    private val levelingSystem = LevelingSystem()

    private fun createCharacter(level: Int, weapon: Weapon, armor: Armor): CharacterStats {
        var character = CharacterStats(weaponBonus = weapon.attackBonus, armorBonus = armor.defenseBonus)
        for (lev in 1 until level) {
            character = levelingSystem.levelUp(character)
        }
        return character.copy(weaponBonus = weapon.attackBonus, armorBonus = armor.defenseBonus)
    }

    private fun winRate(tier: EquipmentTier, simulator: BattleSimulator, random: Random,
                        config: ChallengeConfig?, prestige: PrestigeData?): Double {
        var wins = 0
        repeat(iterations) {
            val character = createCharacter(tier.level, tier.weapon, tier.armor)
            val monsterType = SimulationHelpers.selectRandomMonster(tier.level, random)
            val monster = SimulationHelpers.createMonsterWithRandomScaling(monsterType, tier.level, random, config)
            val log = simulator.simulateBattle(character, monster, config, prestige)
            if (log.outcome == BattleSimulator.BattleOutcome.PlayerVictory) wins++
        }
        return wins.toDouble() / iterations
    }

    private fun makePrestige(vararg bonuses: PrestigeBonus): PrestigeData {
        val s = bonuses.toSet()
        return PrestigeData(unlockedBonuses = s, activeBonuses = s)
    }

    private fun survival10(wr: Double) = wr.pow(10)

    private fun fmt(wr: Double): String {
        val s10 = survival10(wr) * 100
        return String.format("  %5.1f%%  (10-battle: %5.1f%%)", wr * 100, s10)
    }

    @Test
    fun `prestige bonuses vs STRONGER_ENEMIES and WARRIOR_MODE`() {
        val random = Random(42)
        val simulator = BattleSimulator(random)

        val gw  = PrestigeBonus.GREAT_WEAPONS
        val ga  = PrestigeBonus.GREATER_ARMOR
        val ms  = PrestigeBonus.MASTER_SPELLBOOK

        val strongerConfig = ChallengeConfig(challenges = setOf(Challenge.STRONGER_ENEMIES))
        val warriorConfig  = ChallengeConfig(challenges = setOf(Challenge.WARRIOR_MODE))

        println()
        println("=".repeat(72))
        println("PRESTIGE BONUSES vs STRONGER_ENEMIES and WARRIOR_MODE")
        println("Iterations: $iterations  |  Format: per-battle win%  (10-battle survival%)")
        println("=".repeat(72))

        for (tier in tiers) {
            println()
            println("--- ${tier.name} ---")
            println("Scenario                                  WinRate   10-Battle")

            // Baseline (no challenge, no prestige)
            val base = winRate(tier, simulator, random, null, null)
            println("Baseline (no challenge)              ${fmt(base)}")

            println()
            println("  STRONGER_ENEMIES:")
            val se0  = winRate(tier, simulator, random, strongerConfig, null)
            val seGW = winRate(tier, simulator, random, strongerConfig, makePrestige(gw))
            val seGA = winRate(tier, simulator, random, strongerConfig, makePrestige(ga))
            val seMS = winRate(tier, simulator, random, strongerConfig, makePrestige(ms))
            val seGWGA   = winRate(tier, simulator, random, strongerConfig, makePrestige(gw, ga))
            val seGWMS   = winRate(tier, simulator, random, strongerConfig, makePrestige(gw, ms))
            val seGAMS   = winRate(tier, simulator, random, strongerConfig, makePrestige(ga, ms))
            val seAll    = winRate(tier, simulator, random, strongerConfig, makePrestige(gw, ga, ms))
            println("  No prestige                          ${fmt(se0)}")
            println("  + GREAT_WEAPONS                      ${fmt(seGW)}")
            println("  + GREATER_ARMOR                      ${fmt(seGA)}")
            println("  + MASTER_SPELLBOOK                   ${fmt(seMS)}")
            println("  + GW + GA                            ${fmt(seGWGA)}")
            println("  + GW + SPELLBOOK                     ${fmt(seGWMS)}")
            println("  + GA + SPELLBOOK                     ${fmt(seGAMS)}")
            println("  + GW + GA + SPELLBOOK (all)          ${fmt(seAll)}")

            println()
            println("  WARRIOR_MODE:")
            val wm0  = winRate(tier, simulator, random, warriorConfig, null)
            val wmGW = winRate(tier, simulator, random, warriorConfig, makePrestige(gw))
            val wmGA = winRate(tier, simulator, random, warriorConfig, makePrestige(ga))
            val wmMS = winRate(tier, simulator, random, warriorConfig, makePrestige(ms))
            val wmGWGA   = winRate(tier, simulator, random, warriorConfig, makePrestige(gw, ga))
            val wmGWMS   = winRate(tier, simulator, random, warriorConfig, makePrestige(gw, ms))
            val wmGAMS   = winRate(tier, simulator, random, warriorConfig, makePrestige(ga, ms))
            val wmAll    = winRate(tier, simulator, random, warriorConfig, makePrestige(gw, ga, ms))
            println("  No prestige                          ${fmt(wm0)}")
            println("  + GREAT_WEAPONS                      ${fmt(wmGW)}")
            println("  + GREATER_ARMOR                      ${fmt(wmGA)}")
            println("  + MASTER_SPELLBOOK                   ${fmt(wmMS)}")
            println("  + GW + GA                            ${fmt(wmGWGA)}")
            println("  + GW + SPELLBOOK                     ${fmt(wmGWMS)}")
            println("  + GA + SPELLBOOK                     ${fmt(wmGAMS)}")
            println("  + GW + GA + SPELLBOOK (all)          ${fmt(wmAll)}")
        }

        println()
        println("=".repeat(72))
        println("SIMULATION COMPLETE")
        println("=".repeat(72))
    }
}
