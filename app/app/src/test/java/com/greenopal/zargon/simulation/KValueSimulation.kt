package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import org.junit.Test
import kotlin.math.abs
import kotlin.random.Random

/**
 * Simulates the proposed hyperbolic damage formula across K values.
 *
 * New formula: damage = monsterAP * K / (totalDefense + K)
 *   where monsterAP = baseAP * scalingFactor  (already scaled, same as attackPower)
 *
 * This replaces: damage = max(1, monsterAP - totalDefense + random(0..scalingFactor))
 *
 * Tested across:
 *   - K values: 15, 20, 30, 40, 60
 *   - Player levels: 1, 5, 10
 *   - All monster types (Slime through Necro — no bosses)
 *   - Scaling factors: 1-10
 *   - Armor loadouts: No Armor (0), Plated Leather (15), Chain Mail (30), Platemail (42)
 *
 * Reports per K value:
 *   - % of scenarios in 70-90% win rate target band
 *   - Early game survivability (lvl1, scale 1-3): win rates
 *   - Endgame tension (lvl10, Platemail, scale 8-10): win rates
 *   - Max single-step win rate drop (cliff metric)
 */
class KValueSimulation {

    companion object {
        private const val ITERATIONS = 1000
        private val RANDOM_SEED = 42L

        // Armor loadouts to test (armorBonus values matching actual game armor)
        data class ArmorLoadout(val name: String, val bonus: Int)
        val ARMORS = listOf(
            ArmorLoadout("No Armor", 0),
            ArmorLoadout("Plated Leather", 15),
            ArmorLoadout("Chain Mail", 30),
            ArmorLoadout("Platemail", 42)
        )

        // Player levels to test
        val LEVELS = listOf(1, 5, 10)

        // Scaling factors to test
        val SCALING_FACTORS = (1..10).toList()

        // Monster types to test (exclude bosses Kraken and ZARGON)
        val MONSTERS = listOf(
            MonsterType.SLIME,
            MonsterType.BAT,
            MonsterType.BABBLE,
            MonsterType.SPOOK,
            MonsterType.BELETH,
            MonsterType.SKANDER_SNAKE,
            MonsterType.NECRO
        )

        // K values to evaluate
        val K_VALUES = listOf(15, 20, 30, 40, 60)
    }

    /** Build character stats for a given level and armor bonus.
     *  baseDP: starts at 20, grows +4/level (defense only)
     *  maxHP: starts at 20, grows +5/level (average)
     *  baseAP: starts at 5, grows +3/level (average)
     */
    private fun buildCharacter(level: Int, armorBonus: Int): CharacterStats {
        val baseDP = 20 + (level - 1) * 4
        val maxHP = 20 + (level - 1) * 5
        val baseAP = 5 + (level - 1) * 3
        return CharacterStats(
            baseAP = baseAP,
            baseDP = baseDP,
            maxHP = maxHP,
            currentHP = maxHP,
            baseMP = 10,
            currentMP = 10,
            level = level,
            weaponBonus = 0,   // Weapon AP not relevant to damage-received calculation
            armorBonus = armorBonus
        )
    }

    /** Build a monster with the given type and scaling factor. */
    private fun buildMonster(type: MonsterType, scalingFactor: Int): MonsterStats {
        return MonsterStats(
            type = type,
            attackPower = type.baseAP * scalingFactor,
            currentHP = type.baseDP * scalingFactor,
            maxHP = type.baseDP * scalingFactor,
            scalingFactor = scalingFactor
        )
    }

    /**
     * Simulate a single battle using the hyperbolic K formula.
     * Returns true if player wins.
     *
     * Player AP is set to a fixed value sufficient to kill in a reasonable number of turns,
     * so we can focus on survival (damage received). We use the actual totalAP which includes
     * weapon. For this formula test we want to isolate the damage formula, so we grant a
     * fixed weapon bonus of 5 to make the player competitive.
     */
    private fun simulateBattleK(
        character: CharacterStats,
        monster: MonsterStats,
        k: Int,
        random: Random
    ): Boolean {
        var playerHP = character.currentHP
        var monsterHP = monster.currentHP

        // Player AP: use totalAP. For this sim we added weaponBonus=0 to character,
        // so totalAP = baseAP. That's fine — we care about damage received, not turns.
        // But we need the battle to end. Add a reasonable weapon bonus here.
        val playerDamagePerHit = character.baseAP + 5  // +5 simulates a mid-game weapon

        while (playerHP > 0 && monsterHP > 0) {
            // Player attacks
            monsterHP -= playerDamagePerHit
            if (monsterHP <= 0) break

            // Monster attacks using hyperbolic formula
            // damage = attackPower * K / (totalDefense + K)  [rounded to nearest int, min 1]
            val totalDefense = character.totalDefense
            val rawDamage = monster.attackPower.toDouble() * k / (totalDefense + k)
            // Add randomness using the scalingFactor as the random range (mirrors original)
            val randomOffset = if (monster.scalingFactor > 0) random.nextInt(0, monster.scalingFactor + 1) else 0
            val damage = maxOf(1, (rawDamage + randomOffset * 0.3).toInt())
            playerHP -= damage
        }

        return playerHP > 0
    }

    data class ScenarioWinRate(
        val level: Int,
        val armorName: String,
        val armorBonus: Int,
        val monsterType: MonsterType,
        val scalingFactor: Int,
        val winRate: Double
    )

    data class KReport(
        val k: Int,
        val scenarioResults: List<ScenarioWinRate>,
        val pctInTargetBand: Double,       // % of scenarios with 70-90% win rate
        val earlyGameWinRates: List<Double>, // level1, scale 1-3
        val endgameWinRates: List<Double>,   // level10, Platemail, scale 8-10
        val maxCliff: Double                 // max drop in win rate between adjacent scaling steps
    )

    @Test
    fun `simulate K values for hyperbolic damage formula`() {
        println("=" .repeat(80))
        println("K-VALUE SIMULATION FOR HYPERBOLIC DAMAGE FORMULA")
        println("Formula: damage = monsterAP * K / (totalDefense + K)  [+ random jitter, min 1]")
        println("=" .repeat(80))
        println()
        println("Setup:")
        println("  Levels tested: ${LEVELS.joinToString()}")
        println("  Scaling factors: 1-10")
        println("  Armor loadouts: ${ARMORS.joinToString { "${it.name}(+${it.bonus})" }}")
        println("  Monsters: ${MONSTERS.joinToString { it.displayName }}")
        println("  Iterations per scenario: $ITERATIONS")
        println("  Character baseDP: 20 + (level-1)*4  |  maxHP: 20 + (level-1)*5")
        println()

        val reports = mutableListOf<KReport>()

        for (k in K_VALUES) {
            val random = Random(RANDOM_SEED)
            val allResults = mutableListOf<ScenarioWinRate>()

            for (level in LEVELS) {
                for (armor in ARMORS) {
                    for (monster in MONSTERS) {
                        for (scale in SCALING_FACTORS) {
                            val character = buildCharacter(level, armor.bonus)
                            val mon = buildMonster(monster, scale)

                            val wins = (1..ITERATIONS).count { simulateBattleK(character, mon, k, random) }
                            val winRate = wins.toDouble() / ITERATIONS

                            allResults.add(ScenarioWinRate(level, armor.name, armor.bonus, monster, scale, winRate))
                        }
                    }
                }
            }

            // Compute metrics
            val inTargetBand = allResults.count { it.winRate in 0.70..0.90 }
            val pctInBand = inTargetBand.toDouble() / allResults.size * 100.0

            // Early game: level 1, scaling factors 1-3
            val earlyGame = allResults.filter { it.level == 1 && it.scalingFactor in 1..3 }
            val earlyRates = earlyGame.map { it.winRate }

            // Endgame: level 10, Platemail, scaling factors 8-10
            val endgame = allResults.filter { it.level == 10 && it.armorName == "Platemail" && it.scalingFactor in 8..10 }
            val endRates = endgame.map { it.winRate }

            // Cliff metric: max drop in win rate for adjacent scaling steps within same level/armor/monster combo
            var maxCliff = 0.0
            for (level in LEVELS) {
                for (armor in ARMORS) {
                    for (monster in MONSTERS) {
                        val ratesForCombo = SCALING_FACTORS.map { scale ->
                            allResults.first { it.level == level && it.armorName == armor.name && it.monsterType == monster && it.scalingFactor == scale }.winRate
                        }
                        for (i in 0 until ratesForCombo.size - 1) {
                            val drop = ratesForCombo[i] - ratesForCombo[i + 1]
                            if (drop > maxCliff) maxCliff = drop
                        }
                    }
                }
            }

            reports.add(KReport(k, allResults, pctInBand, earlyRates, endRates, maxCliff))
        }

        // Print summary table
        println("=" .repeat(80))
        println("SUMMARY TABLE")
        println("=" .repeat(80))
        println("%-6s | %-20s | %-25s | %-25s | %-12s".format(
            "K", "% in 70-90% band", "Early(lvl1,sc1-3) avgWin", "End(lvl10,PM,sc8-10) avgWin", "MaxCliff"))
        println("-".repeat(100))
        for (r in reports) {
            val earlyAvg = if (r.earlyGameWinRates.isNotEmpty()) r.earlyGameWinRates.average() else 0.0
            val endAvg = if (r.endgameWinRates.isNotEmpty()) r.endgameWinRates.average() else 0.0
            println("%-6d | %18.1f%% | %23.1f%% | %23.1f%% | %10.1f%%".format(
                r.k, r.pctInTargetBand, earlyAvg * 100, endAvg * 100, r.maxCliff * 100))
        }
        println()

        // Per-K detailed breakdown
        for (r in reports) {
            println("=" .repeat(80))
            println("K = ${r.k} — DETAILED BREAKDOWN")
            println("=" .repeat(80))

            // Win rate by level and armor (averaged over all monsters and scaling factors)
            println()
            println("Win Rate by Level x Armor (avg over all monsters, all scaling factors 1-10):")
            println("%-12s | %-15s | %-15s | %-15s | %-15s".format("Level", "No Armor", "Plated Leather", "Chain Mail", "Platemail"))
            println("-".repeat(80))
            for (level in LEVELS) {
                val row = ARMORS.map { armor ->
                    val filtered = r.scenarioResults.filter { it.level == level && it.armorName == armor.name }
                    if (filtered.isNotEmpty()) filtered.map { it.winRate }.average() * 100 else 0.0
                }
                println("%-12s | %13.1f%% | %13.1f%% | %13.1f%% | %13.1f%%".format(
                    "Level $level", row[0], row[1], row[2], row[3]))
            }

            // Early game detail: level 1, all armors, scaling 1-3
            println()
            println("Early Game (Level 1, Scaling 1-3) — Win Rates by Monster:")
            println("%-20s | %-10s | %-14s | %-10s | %-10s".format("Monster", "No Armor", "Plated Leather", "Chain Mail", "Platemail"))
            println("-".repeat(72))
            for (monster in MONSTERS) {
                val row = ARMORS.map { armor ->
                    val filtered = r.scenarioResults.filter {
                        it.level == 1 && it.armorName == armor.name && it.monsterType == monster && it.scalingFactor in 1..3
                    }
                    if (filtered.isNotEmpty()) filtered.map { it.winRate }.average() * 100 else 0.0
                }
                println("%-20s | %8.1f%% | %12.1f%% | %8.1f%% | %8.1f%%".format(
                    monster.displayName, row[0], row[1], row[2], row[3]))
            }

            // Endgame detail: level 10, Platemail, all monsters, scaling 6-10
            println()
            println("Endgame (Level 10, Platemail, Scaling 6-10) — Win Rates by Monster:")
            println("%-20s | %-10s | %-10s | %-10s | %-10s | %-10s".format("Monster", "Scale 6", "Scale 7", "Scale 8", "Scale 9", "Scale 10"))
            println("-".repeat(72))
            for (monster in MONSTERS) {
                val row = (6..10).map { scale ->
                    val entry = r.scenarioResults.firstOrNull {
                        it.level == 10 && it.armorName == "Platemail" && it.monsterType == monster && it.scalingFactor == scale
                    }
                    entry?.winRate?.times(100) ?: 0.0
                }
                println("%-20s | %8.1f%% | %8.1f%% | %8.1f%% | %8.1f%% | %8.1f%%".format(
                    monster.displayName, row[0], row[1], row[2], row[3], row[4]))
            }

            // Cliff analysis: worst drops per armor loadout
            println()
            println("Largest win rate drops between adjacent scaling steps (all levels, per armor):")
            for (armor in ARMORS) {
                var biggestDrop = 0.0
                var dropContext = ""
                for (level in LEVELS) {
                    for (monster in MONSTERS) {
                        val ratesForCombo = SCALING_FACTORS.map { scale ->
                            r.scenarioResults.first {
                                it.level == level && it.armorName == armor.name && it.monsterType == monster && it.scalingFactor == scale
                            }.winRate
                        }
                        for (i in 0 until ratesForCombo.size - 1) {
                            val drop = ratesForCombo[i] - ratesForCombo[i + 1]
                            if (drop > biggestDrop) {
                                biggestDrop = drop
                                dropContext = "Level $level, ${monster.displayName}, scale ${SCALING_FACTORS[i]}->${SCALING_FACTORS[i+1]}"
                            }
                        }
                    }
                }
                println("  ${armor.name}: %.1f%% drop  ($dropContext)".format(biggestDrop * 100))
            }
            println()
        }

        // Recommendation
        println("=" .repeat(80))
        println("RECOMMENDATION")
        println("=" .repeat(80))
        val best = reports.maxByOrNull { r ->
            // Score = % in target band (heavily weighted) minus cliff penalty minus early-death penalty
            val earlyAvg = r.earlyGameWinRates.average()
            val endAvg = if (r.endgameWinRates.isNotEmpty()) r.endgameWinRates.average() else 1.0
            // We want early game survivable (>60%), endgame with tension (<95%), small cliff
            val earlyPenalty = if (earlyAvg < 0.60) (0.60 - earlyAvg) * 200 else 0.0
            val endPenalty = if (endAvg > 0.95) (endAvg - 0.95) * 100 else 0.0
            val cliffPenalty = r.maxCliff * 50
            r.pctInTargetBand - earlyPenalty - endPenalty - cliffPenalty
        }
        println("Best K value by scoring: K=${best?.k}")
        println()
        println("Scoring rationale: maximises % in 70-90% target band, penalises early-game death rate <60%,")
        println("endgame triviality >95%, and large cliff drops between adjacent scaling steps.")
        println()
    }

    /**
     * Head-to-head comparison: old formula vs best K formula for key scenarios.
     */
    @Test
    fun `compare old vs new formula for representative scenarios`() {
        data class Scenario(val label: String, val level: Int, val armorBonus: Int, val monsterType: MonsterType, val scalingFactor: Int)

        val scenarios = listOf(
            Scenario("Early: L1 No Armor vs Slime x1",      level = 1,  armorBonus = 0,  MonsterType.SLIME,  1),
            Scenario("Early: L1 No Armor vs Babble x2",     level = 1,  armorBonus = 0,  MonsterType.BABBLE, 2),
            Scenario("Early: L1 Plated Leather vs Spook x2",level = 1,  armorBonus = 15, MonsterType.SPOOK,  2),
            Scenario("Mid: L5 Chain Mail vs Beleth x4",     level = 5,  armorBonus = 30, MonsterType.BELETH, 4),
            Scenario("Mid: L5 Chain Mail vs Necro x3",      level = 5,  armorBonus = 30, MonsterType.NECRO,  3),
            Scenario("Late: L10 Platemail vs Necro x6",     level = 10, armorBonus = 42, MonsterType.NECRO,  6),
            Scenario("Late: L10 Platemail vs SkanderSnake x8",level = 10, armorBonus = 42, MonsterType.SKANDER_SNAKE, 8),
            Scenario("Late: L10 No Armor vs Necro x8",      level = 10, armorBonus = 0,  MonsterType.NECRO,  8)
        )

        val kBest = 30  // anticipated best K — we compare old vs K=30 and K=40
        val kValues = listOf(kBest, 40)
        val random = Random(RANDOM_SEED)

        println("=" .repeat(80))
        println("HEAD-TO-HEAD: OLD FORMULA vs NEW K FORMULA")
        println("=" .repeat(80))
        println("Old: damage = max(1, monsterAP - totalDefense + random(0..scalingFactor))")
        println("New: damage = monsterAP * K / (totalDefense + K) + partial random jitter, min 1")
        println()

        for (scenario in scenarios) {
            val character = buildCharacter(scenario.level, scenario.armorBonus)
            val monster = buildMonster(scenario.monsterType, scenario.scalingFactor)
            val totalDefense = character.totalDefense

            // Old formula win rate
            var oldWins = 0
            val oldRandom = Random(RANDOM_SEED)
            for (i in 1..ITERATIONS) {
                var pHP = character.currentHP
                var mHP = monster.currentHP
                val playerDmg = character.baseAP + 5
                while (pHP > 0 && mHP > 0) {
                    mHP -= playerDmg
                    if (mHP <= 0) break
                    val rf = if (monster.scalingFactor > 0) oldRandom.nextInt(0, monster.scalingFactor + 1) else 0
                    val dmg = maxOf(1, monster.attackPower - totalDefense + rf)
                    pHP -= dmg
                }
                if (pHP > 0) oldWins++
            }
            val oldWinRate = oldWins.toDouble() / ITERATIONS

            val newRates = kValues.map { k ->
                val kRandom = Random(RANDOM_SEED)
                val wins = (1..ITERATIONS).count { simulateBattleK(character, monster, k, kRandom) }
                k to wins.toDouble() / ITERATIONS
            }

            val avgMonsterDmgOld = maxOf(1.0, monster.attackPower.toDouble() - totalDefense + monster.scalingFactor / 2.0)
            val avgMonsterDmgK30 = maxOf(1.0, monster.attackPower.toDouble() * kBest / (totalDefense + kBest) + monster.scalingFactor * 0.15)
            val avgMonsterDmgK40 = maxOf(1.0, monster.attackPower.toDouble() * 40 / (totalDefense + 40) + monster.scalingFactor * 0.15)

            println("SCENARIO: ${scenario.label}")
            println("  Player: L${scenario.level}, totalDefense=$totalDefense (baseDP=${20 + (scenario.level-1)*4} + armor=${scenario.armorBonus}), HP=${character.currentHP}")
            println("  Monster: ${scenario.monsterType.displayName} x${scenario.scalingFactor}, AP=${monster.attackPower}")
            println("  Avg monster dmg/hit (old):  %.1f".format(avgMonsterDmgOld))
            println("  Avg monster dmg/hit (K=30): %.1f".format(avgMonsterDmgK30))
            println("  Avg monster dmg/hit (K=40): %.1f".format(avgMonsterDmgK40))
            println("  Win rate  OLD: %5.1f%%".format(oldWinRate * 100))
            for ((k, rate) in newRates) {
                println("  Win rate K=%2d: %5.1f%%".format(k, rate * 100))
            }
            println()
        }
    }
}
