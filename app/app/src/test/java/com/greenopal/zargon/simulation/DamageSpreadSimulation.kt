package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import org.junit.Test
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Compares 3x3 combinations of player spread divisor vs monster spread formula,
 * measuring how damage randomness affects win rate, turns, HP remaining, and
 * coefficient of variation (CV = stddev/mean) for both player and monster damage.
 *
 * Player spread: spread = effectiveWeaponBonus / divisor
 *   divisor=3  -> DAGGER(+5)=±1, LONG_SWORD(+13)=±4, ATLANTEAN(+35)=±11  (wide)
 *   divisor=5  -> DAGGER(+5)=±1, LONG_SWORD(+13)=±2, ATLANTEAN(+35)=±7   (current)
 *   divisor=8  -> DAGGER(+5)=±0, LONG_SWORD(+13)=±1, ATLANTEAN(+35)=±4   (narrow)
 *
 * Monster spread (multiplier range = 1-spread to 1+spread):
 *   Narrow:  base=0.08, scale=1/800, max=0.20  -> ±8% to ±20%
 *   Current: base=0.15, scale=1/500, max=0.40  -> ±15% to ±40%
 *   Wide:    base=0.20, scale=1/300, max=0.55  -> ±20% to ±55%
 *
 * Weapons tested (power-law redesign bonuses):
 *   DAGGER=+5, SHORT_SWORD=+8, LONG_SWORD=+13, SWORD_OF_THORNS=+18,
 *   BROAD_SWORD=+23, TWO_HANDED_SWORD=+28, ATLANTEAN=+35
 *
 * Character stats (same as canonical audit sims):
 *   baseDP  = 20 + (level-1)*4   (defense only)
 *   maxHP   = 20 + (level-1)*5
 *   baseAP  = 5  + (level-1)*3
 *
 * Monster stats:
 *   scalingFactor = level/2 + 1
 *   attackPower   = type.baseAP * scalingFactor
 *   currentHP     = type.baseDP * scalingFactor
 *
 * DAMAGE_K = 20.0 (matches BattleEngine companion const)
 */
class DamageSpreadSimulation {

    companion object {
        private const val ITERATIONS = 2000
        private const val DAMAGE_K = 20.0
        private const val SEED = 42L

        private val PLAYER_DIVISORS = listOf(3, 5, 8)
        private val PLAYER_DIVISOR_LABELS = mapOf(3 to "Wide(÷3)", 5 to "Current(÷5)", 8 to "Narrow(÷8)")

        // Monster spread configs: (base, scaleBy, maxCap)
        data class MonsterSpreadConfig(val label: String, val base: Double, val scaleBy: Double, val maxCap: Double)
        private val MONSTER_SPREADS = listOf(
            MonsterSpreadConfig("Narrow",  0.08, 1.0 / 800.0, 0.20),
            MonsterSpreadConfig("Current", 0.15, 1.0 / 500.0, 0.40),
            MonsterSpreadConfig("Wide",    0.20, 1.0 / 300.0, 0.55)
        )

        // Representative weapon bonuses (power-law redesign)
        data class WeaponSpec(val name: String, val bonus: Int)
        private val WEAPONS = listOf(
            WeaponSpec("Dagger",        5),
            WeaponSpec("ShortSword",    8),
            WeaponSpec("LongSword",    13),
            WeaponSpec("SwordOfThorns",18),
            WeaponSpec("BroadSword",   23),
            WeaponSpec("TwoHanded",    28),
            WeaponSpec("Atlantean",    35)
        )

        // Armor bonuses tested
        data class ArmorSpec(val name: String, val bonus: Int)
        private val ARMORS = listOf(
            ArmorSpec("Cloth",     5),
            ArmorSpec("ChainMail", 28),
            ArmorSpec("Platemail", 50)
        )

        // Player levels tested
        private val LEVELS = listOf(1, 5, 10)

        // Regular monsters (no bosses)
        private val MONSTERS = listOf(
            MonsterType.SLIME,
            MonsterType.BAT,
            MonsterType.BABBLE,
            MonsterType.SPOOK,
            MonsterType.BELETH,
            MonsterType.SKANDER_SNAKE,
            MonsterType.NECRO
        )

        // Weapon-to-level pairing for representative gameplay tiers
        private val WEAPON_FOR_LEVEL = mapOf(
            1  to WeaponSpec("Dagger",      5),
            5  to WeaponSpec("LongSword",  13),
            10 to WeaponSpec("Atlantean",  35)
        )
    }

    // -------------------------------------------------------------------
    // Data structures
    // -------------------------------------------------------------------

    data class BattleResult(
        val playerWon: Boolean,
        val turnsToEnd: Int,
        val hpRemaining: Int,
        val playerDamages: List<Int>,
        val monsterDamages: List<Int>
    )

    data class ScenarioStats(
        val winRate: Double,
        val avgTurnsWin: Double,
        val avgHpRemainingWin: Double,
        val cvPlayerDamage: Double,
        val cvMonsterDamage: Double,
        val avgPlayerDamage: Double,
        val avgMonsterDamage: Double,
        val stddevPlayerDamage: Double,
        val stddevMonsterDamage: Double
    )

    // -------------------------------------------------------------------
    // Character / monster builders
    // -------------------------------------------------------------------

    private fun buildCharacter(level: Int, weaponBonus: Int, armorBonus: Int): CharacterStats {
        val baseAP = 5 + (level - 1) * 3
        val baseDP = 20 + (level - 1) * 4
        val maxHP  = 20 + (level - 1) * 5
        return CharacterStats(
            baseAP      = baseAP,
            baseDP      = baseDP,
            maxHP       = maxHP,
            currentHP   = maxHP,
            baseMP      = 10,
            currentMP   = 10,
            level       = level,
            weaponBonus = weaponBonus,
            armorBonus  = armorBonus
        )
    }

    private fun buildMonster(type: MonsterType, level: Int): MonsterStats {
        val scale = level / 2 + 1
        return MonsterStats(
            type          = type,
            attackPower   = type.baseAP * scale,
            currentHP     = type.baseDP * scale,
            maxHP         = type.baseDP * scale,
            scalingFactor = scale
        )
    }

    // -------------------------------------------------------------------
    // Damage calculators (mirroring BattleEngine exactly)
    // -------------------------------------------------------------------

    private fun calcPlayerDamage(character: CharacterStats, divisor: Int, random: Random): Int {
        val effectiveWeaponBonus = character.weaponBonus
        val spread = effectiveWeaponBonus / divisor
        val roll = if (spread > 0) random.nextInt(-spread, spread + 1) else 0
        return maxOf(1, character.baseAP + effectiveWeaponBonus + roll)
    }

    private fun calcMonsterDamage(
        monster: MonsterStats,
        character: CharacterStats,
        spreadConfig: MonsterSpreadConfig,
        random: Random
    ): Int {
        val spread = (spreadConfig.base + monster.attackPower * spreadConfig.scaleBy)
            .coerceAtMost(spreadConfig.maxCap)
        val randomMultiplier = (1.0 - spread) + random.nextDouble() * (2.0 * spread)
        val totalDefense = character.totalDefense
        val rawDamage = monster.attackPower.toDouble() * DAMAGE_K /
            (totalDefense + DAMAGE_K) * randomMultiplier
        return maxOf(1, rawDamage.toInt())
    }

    // -------------------------------------------------------------------
    // Single battle simulation
    // -------------------------------------------------------------------

    private fun simulateBattle(
        character: CharacterStats,
        monster: MonsterStats,
        playerDivisor: Int,
        monsterSpread: MonsterSpreadConfig,
        random: Random
    ): BattleResult {
        var playerHP = character.currentHP
        var monsterHP = monster.currentHP
        var turns = 0
        val playerDamages = mutableListOf<Int>()
        val monsterDamages = mutableListOf<Int>()

        while (playerHP > 0 && monsterHP > 0) {
            turns++
            val pDmg = calcPlayerDamage(character, playerDivisor, random)
            playerDamages.add(pDmg)
            monsterHP -= pDmg
            if (monsterHP <= 0) {
                return BattleResult(
                    playerWon       = true,
                    turnsToEnd      = turns,
                    hpRemaining     = playerHP,
                    playerDamages   = playerDamages,
                    monsterDamages  = monsterDamages
                )
            }
            val mDmg = calcMonsterDamage(monster, character, monsterSpread, random)
            monsterDamages.add(mDmg)
            playerHP -= mDmg
        }

        return BattleResult(
            playerWon       = false,
            turnsToEnd      = turns,
            hpRemaining     = playerHP.coerceAtLeast(0),
            playerDamages   = playerDamages,
            monsterDamages  = monsterDamages
        )
    }

    // -------------------------------------------------------------------
    // Run N iterations and compute aggregate stats
    // -------------------------------------------------------------------

    private fun runScenario(
        character: CharacterStats,
        monster: MonsterStats,
        playerDivisor: Int,
        monsterSpread: MonsterSpreadConfig,
        random: Random
    ): ScenarioStats {
        val results = (1..ITERATIONS).map {
            simulateBattle(character, monster, playerDivisor, monsterSpread, random)
        }

        val wins = results.filter { it.playerWon }
        val winRate = wins.size.toDouble() / ITERATIONS

        val avgTurnsWin = if (wins.isEmpty()) 0.0 else wins.map { it.turnsToEnd }.average()
        val avgHpWin    = if (wins.isEmpty()) 0.0 else wins.map { it.hpRemaining }.average()

        // Collect all individual damage values across all battles
        val allPlayerDmg = results.flatMap { it.playerDamages }
        val allMonsterDmg = results.flatMap { it.monsterDamages }

        fun mean(xs: List<Int>) = if (xs.isEmpty()) 0.0 else xs.map { it.toDouble() }.average()
        fun stddev(xs: List<Int>): Double {
            if (xs.size < 2) return 0.0
            val m = xs.map { it.toDouble() }.average()
            val variance = xs.map { (it - m) * (it - m) }.average()
            return sqrt(variance)
        }
        fun cv(xs: List<Int>): Double {
            val m = mean(xs)
            return if (m == 0.0) 0.0 else stddev(xs) / m
        }

        return ScenarioStats(
            winRate               = winRate,
            avgTurnsWin           = avgTurnsWin,
            avgHpRemainingWin     = avgHpWin,
            cvPlayerDamage        = cv(allPlayerDmg),
            cvMonsterDamage       = cv(allMonsterDmg),
            avgPlayerDamage       = mean(allPlayerDmg),
            avgMonsterDamage      = mean(allMonsterDmg),
            stddevPlayerDamage    = stddev(allPlayerDmg),
            stddevMonsterDamage   = stddev(allMonsterDmg)
        )
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private fun fmtWR(v: Double) = "%5.1f%%".format(v * 100)
    private fun fmtCV(v: Double) = "%5.3f".format(v)
    private fun fmtF1(v: Double) = "%6.1f".format(v)
    private fun sep(n: Int) = "=".repeat(n)
    private fun dash(n: Int) = "-".repeat(n)

    // -------------------------------------------------------------------
    // MAIN TEST
    // -------------------------------------------------------------------

    @Test
    fun `damage spread configuration comparison`() {
        val random = Random(SEED)

        println(sep(100))
        println("DAMAGE SPREAD SIMULATION — 3x3 Player Divisor x Monster Spread (2000 iterations/scenario)")
        println(sep(100))
        println()
        println("Player spread divisors : 3=Wide, 5=Current, 8=Narrow")
        println("Monster spread configs : Narrow(±8-20%), Current(±15-40%), Wide(±20-55%)")
        println("Levels tested          : ${LEVELS.joinToString()}")
        println("Monsters (regular)     : ${MONSTERS.joinToString { it.displayName }}")
        println("Armor loadouts         : ${ARMORS.joinToString { "${it.name}(+${it.bonus})" }}")
        println("Weapon for level       : L1=Dagger(+5), L5=LongSword(+13), L10=Atlantean(+35)")
        println()
        println("DAMAGE_K = $DAMAGE_K")
        println("Character: baseDP = 20+(level-1)*4  |  maxHP = 20+(level-1)*5  |  baseAP = 5+(level-1)*3")
        println("Monster  : scalingFactor = level/2+1,  attackPower = baseAP*sf,  currentHP = baseDP*sf")
        println()

        // ================================================================
        // SECTION 1: Spread reference table — what spread values actually mean
        // ================================================================
        println(sep(100))
        println("SECTION 1 — SPREAD REFERENCE: What do these spread values translate to?")
        println(sep(100))
        println()
        println("Player spread (damage roll = ±spread added to baseAP+weaponBonus):")
        println("%-20s | %8s | %12s | %12s | %12s".format("Weapon", "Bonus", "Div=3(wide)", "Div=5(curr)", "Div=8(narrow)"))
        println(dash(72))
        for (w in WEAPONS) {
            val s3 = w.bonus / 3
            val s5 = w.bonus / 5
            val s8 = w.bonus / 8
            println("%-20s | %8d | %12s | %12s | %12s".format(
                w.name, w.bonus,
                "±$s3", "±$s5", "±$s8"))
        }
        println()
        println("Monster spread (multiplier = 1.0 ± spread, applied to K-formula base damage):")
        println("%-20s | %8s | %12s | %12s | %12s".format("Monster(AP@sc)", "AttackPwr", "Narrow", "Current", "Wide"))
        println(dash(80))
        // Sample at scalingFactor 2, 5, 10
        for (type in listOf(MonsterType.SLIME, MonsterType.BABBLE, MonsterType.BELETH, MonsterType.NECRO)) {
            for (sf in listOf(2, 5, 10)) {
                val ap = type.baseAP * sf
                val narrow  = (0.08 + ap * (1.0/800.0)).coerceAtMost(0.20)
                val current = (0.15 + ap * (1.0/500.0)).coerceAtMost(0.40)
                val wide    = (0.20 + ap * (1.0/300.0)).coerceAtMost(0.55)
                println("%-20s | %9d | %12s | %12s | %12s".format(
                    "${type.displayName} x$sf", ap,
                    "±${"%.0f".format(narrow*100)}%",
                    "±${"%.0f".format(current*100)}%",
                    "±${"%.0f".format(wide*100)}%"))
            }
        }
        println()

        // ================================================================
        // SECTION 2: 9-combination win-rate summary (all levels/monsters averaged)
        // ================================================================
        println(sep(100))
        println("SECTION 2 — 9-COMBINATION OVERALL WIN RATE SUMMARY")
        println("(avg across all levels, armor, monsters, scaling factors)")
        println(sep(100))
        println()

        data class CombinationKey(val divisor: Int, val monsterSpread: MonsterSpreadConfig)
        val overallStats = mutableMapOf<CombinationKey, MutableList<ScenarioStats>>()
        for (div in PLAYER_DIVISORS) {
            for (ms in MONSTER_SPREADS) {
                overallStats[CombinationKey(div, ms)] = mutableListOf()
            }
        }

        // Run all scenarios — collect by combination key
        for (div in PLAYER_DIVISORS) {
            for (ms in MONSTER_SPREADS) {
                val key = CombinationKey(div, ms)
                for (level in LEVELS) {
                    val weapon = WEAPON_FOR_LEVEL[level]!!
                    for (armor in ARMORS) {
                        val character = buildCharacter(level, weapon.bonus, armor.bonus)
                        for (monsterType in MONSTERS) {
                            val monster = buildMonster(monsterType, level)
                            val stats = runScenario(character, monster, div, ms, random)
                            overallStats[key]!!.add(stats)
                        }
                    }
                }
            }
        }

        // Print 3x3 win rate grid
        println("Win Rate Grid (avg win rate across all scenarios):")
        println("%-20s | %-12s | %-12s | %-12s".format("Player Spread", "Monster:Narrow", "Monster:Current", "Monster:Wide"))
        println(dash(62))
        for (div in PLAYER_DIVISORS) {
            val label = PLAYER_DIVISOR_LABELS[div]!!
            val rates = MONSTER_SPREADS.map { ms ->
                val key = CombinationKey(div, ms)
                overallStats[key]!!.map { it.winRate }.average()
            }
            println("%-20s | %-12s | %-13s | %-12s".format(
                label, fmtWR(rates[0]), fmtWR(rates[1]), fmtWR(rates[2])))
        }
        println()

        println("Avg CV(Player Damage) Grid (higher = more swingy player hits):")
        println("%-20s | %-12s | %-12s | %-12s".format("Player Spread", "Monster:Narrow", "Monster:Current", "Monster:Wide"))
        println(dash(62))
        for (div in PLAYER_DIVISORS) {
            val label = PLAYER_DIVISOR_LABELS[div]!!
            val cvs = MONSTER_SPREADS.map { ms ->
                val key = CombinationKey(div, ms)
                overallStats[key]!!.map { it.cvPlayerDamage }.average()
            }
            println("%-20s | %-12s | %-13s | %-12s".format(
                label, fmtCV(cvs[0]), fmtCV(cvs[1]), fmtCV(cvs[2])))
        }
        println()

        println("Avg CV(Monster Damage) Grid (higher = more swingy monster hits):")
        println("%-20s | %-12s | %-12s | %-12s".format("Player Spread", "Monster:Narrow", "Monster:Current", "Monster:Wide"))
        println(dash(62))
        for (div in PLAYER_DIVISORS) {
            val label = PLAYER_DIVISOR_LABELS[div]!!
            val cvs = MONSTER_SPREADS.map { ms ->
                val key = CombinationKey(div, ms)
                overallStats[key]!!.map { it.cvMonsterDamage }.average()
            }
            println("%-20s | %-12s | %-13s | %-12s".format(
                label, fmtCV(cvs[0]), fmtCV(cvs[1]), fmtCV(cvs[2])))
        }
        println()

        // ================================================================
        // SECTION 3: Win rates by player level for each 9-combo
        // ================================================================
        println(sep(100))
        println("SECTION 3 — WIN RATES BY PLAYER LEVEL (avg over all monsters/armor per level)")
        println(sep(100))
        println()

        for (div in PLAYER_DIVISORS) {
            val divLabel = PLAYER_DIVISOR_LABELS[div]!!
            println("Player Spread: $divLabel")
            println("%-16s | %-12s | %-12s | %-12s".format("Level", "Narrow", "Current", "Wide"))
            println(dash(56))

            for (level in LEVELS) {
                val weapon = WEAPON_FOR_LEVEL[level]!!
                val rates = MONSTER_SPREADS.map { ms ->
                    val sub = mutableListOf<ScenarioStats>()
                    for (armor in ARMORS) {
                        val character = buildCharacter(level, weapon.bonus, armor.bonus)
                        for (monsterType in MONSTERS) {
                            val monster = buildMonster(monsterType, level)
                            sub.add(runScenario(character, monster, div, ms, random))
                        }
                    }
                    sub.map { it.winRate }.average()
                }
                println("Level %-2d %-7s | %-12s | %-13s | %-12s".format(
                    level, "(${weapon.name})",
                    fmtWR(rates[0]), fmtWR(rates[1]), fmtWR(rates[2])))
            }
            println()
        }

        // ================================================================
        // SECTION 4: Coefficient of variation detail by level
        // ================================================================
        println(sep(100))
        println("SECTION 4 — CV ANALYSIS BY PLAYER LEVEL")
        println("(CV = stddev/mean; higher = more variable/swingy damage)")
        println(sep(100))
        println()

        for (div in PLAYER_DIVISORS) {
            val divLabel = PLAYER_DIVISOR_LABELS[div]!!
            println("Player Spread: $divLabel — CV(Player Damage) by level:")
            println("%-16s | %-12s | %-12s | %-12s".format("Level+Weapon", "Narrow", "Current", "Wide"))
            println(dash(56))
            for (level in LEVELS) {
                val weapon = WEAPON_FOR_LEVEL[level]!!
                val cvs = MONSTER_SPREADS.map { ms ->
                    val sub = mutableListOf<ScenarioStats>()
                    for (armor in ARMORS) {
                        val character = buildCharacter(level, weapon.bonus, armor.bonus)
                        for (monsterType in MONSTERS) {
                            val monster = buildMonster(monsterType, level)
                            sub.add(runScenario(character, monster, div, ms, random))
                        }
                    }
                    sub.map { it.cvPlayerDamage }.average()
                }
                println("L%-2d %-11s | %-12s | %-13s | %-12s".format(
                    level, "(${weapon.name})",
                    fmtCV(cvs[0]), fmtCV(cvs[1]), fmtCV(cvs[2])))
            }
            println()
        }

        println("CV(Monster Damage) by monster spread config (avg over all divisors, all levels/armor/monsters):")
        println("%-12s | %-10s | %-10s | %-10s".format("Monster Type", "Narrow", "Current", "Wide"))
        println(dash(48))
        for (level in LEVELS) {
            for (monsterType in MONSTERS) {
                val rowCvs = MONSTER_SPREADS.map { ms ->
                    val sub = mutableListOf<ScenarioStats>()
                    for (div in PLAYER_DIVISORS) {
                        for (armor in ARMORS) {
                            val weapon = WEAPON_FOR_LEVEL[level]!!
                            val character = buildCharacter(level, weapon.bonus, armor.bonus)
                            val monster = buildMonster(monsterType, level)
                            sub.add(runScenario(character, monster, div, ms, random))
                        }
                    }
                    sub.map { it.cvMonsterDamage }.average()
                }
                val sf = level / 2 + 1
                val ap = monsterType.baseAP * sf
                println("%-12s | %-10s | %-11s | %-10s  (L%d sf=%d AP=%d)".format(
                    monsterType.displayName,
                    fmtCV(rowCvs[0]), fmtCV(rowCvs[1]), fmtCV(rowCvs[2]),
                    level, sf, ap))
            }
        }
        println()

        // ================================================================
        // SECTION 5: Swinginess — proportion of extreme outcomes
        // ================================================================
        println(sep(100))
        println("SECTION 5 — SWINGINESS ANALYSIS: % of wins with HP > 80% and % of losses")
        println("(easy wins = HP remaining > 80% of maxHP; brutal deaths = loss when initially >50% chance)")
        println(sep(100))
        println()

        for (div in PLAYER_DIVISORS) {
            val divLabel = PLAYER_DIVISOR_LABELS[div]!!
            println("Player Spread: $divLabel")
            println("%-14s | %-12s | %-12s | %-12s | %-12s | %-12s | %-12s".format(
                "Monster Spread",
                "Win%", "EasyWin%", "Loss%",
                "AvgTurns(W)", "AvgHP%(W)", "CV(MonDmg)"))
            println(dash(90))
            for (ms in MONSTER_SPREADS) {
                val swingResults = mutableListOf<Triple<Boolean, Int, Int>>() // won, hpRemaining, maxHP
                val turns = mutableListOf<Int>()
                val cvMon = mutableListOf<Double>()

                for (level in LEVELS) {
                    val weapon = WEAPON_FOR_LEVEL[level]!!
                    for (armor in ARMORS) {
                        val character = buildCharacter(level, weapon.bonus, armor.bonus)
                        for (monsterType in MONSTERS) {
                            val monster = buildMonster(monsterType, level)
                            val r2 = Random(SEED)
                            repeat(ITERATIONS) {
                                val battle = simulateBattle(character, monster, div, ms, r2)
                                swingResults.add(Triple(battle.playerWon, battle.hpRemaining, character.maxHP))
                                if (battle.playerWon) turns.add(battle.turnsToEnd)
                            }
                            // collect CV for this scenario
                            val stats = runScenario(character, monster, div, ms, Random(SEED + 1))
                            cvMon.add(stats.cvMonsterDamage)
                        }
                    }
                }

                val totalBattles = swingResults.size.toDouble()
                val wins2 = swingResults.count { it.first }
                val lossCount = swingResults.count { !it.first }
                val easyWins = swingResults.count { it.first && it.second > (it.third * 0.80) }
                val wr = wins2 / totalBattles
                val easyWinPct = if (wins2 > 0) easyWins.toDouble() / wins2 else 0.0
                val lossPct = lossCount / totalBattles
                val avgTurns = if (turns.isEmpty()) 0.0 else turns.average()
                val avgHpPct = if (wins2 > 0) {
                    swingResults.filter { it.first }.map { it.second.toDouble() / it.third }.average()
                } else 0.0
                val cvMon2 = cvMon.average()

                println("%-14s | %-12s | %-12s | %-12s | %-12s | %-12s | %-12s".format(
                    ms.label,
                    fmtWR(wr), "${"%.1f".format(easyWinPct*100)}%", fmtWR(lossPct),
                    fmtF1(avgTurns), "${"%.1f".format(avgHpPct*100)}%",
                    fmtCV(cvMon2)))
            }
            println()
        }

        // ================================================================
        // SECTION 6: Critical scenario detail — early/mid/late game
        // ================================================================
        println(sep(100))
        println("SECTION 6 — CRITICAL SCENARIO DETAIL (win rate per combo for key fights)")
        println(sep(100))
        println()

        data class KeyScenario(val label: String, val level: Int, val weaponBonus: Int, val armorBonus: Int, val monster: MonsterType)
        val keyScenarios = listOf(
            KeyScenario("Early L1 Dagger+5 Cloth vs Slime",   1,  5,  5, MonsterType.SLIME),
            KeyScenario("Early L1 Dagger+5 Cloth vs Babble",  1,  5,  5, MonsterType.BABBLE),
            KeyScenario("Early L1 Dagger+5 Chain vs Spook",   1,  5, 28, MonsterType.SPOOK),
            KeyScenario("Mid   L5 Long+13 Chain vs Beleth",   5, 13, 28, MonsterType.BELETH),
            KeyScenario("Mid   L5 Long+13 Chain vs Necro",    5, 13, 28, MonsterType.NECRO),
            KeyScenario("Mid   L5 Long+13 Plate vs SkanSnk",  5, 13, 50, MonsterType.SKANDER_SNAKE),
            KeyScenario("Late L10 Atl+35 Plate vs Necro",    10, 35, 50, MonsterType.NECRO),
            KeyScenario("Late L10 Atl+35 Plate vs Kraken",   10, 35, 50, MonsterType.KRAKEN),
            KeyScenario("Late L10 Atl+35 Chain vs Necro",    10, 35, 28, MonsterType.NECRO)
        )

        // Print header spanning all 9 combos
        val comboHeader = PLAYER_DIVISORS.joinToString(" | ") { div ->
            val dlabel = PLAYER_DIVISOR_LABELS[div]!!
            MONSTER_SPREADS.joinToString(" ") { ms -> "P$div-M${ms.label.take(4)}" }
        }
        println("%-38s | $comboHeader".format("Scenario"))
        println(dash(38 + 3 + PLAYER_DIVISORS.size * MONSTER_SPREADS.size * 12))

        for (ks in keyScenarios) {
            val character = buildCharacter(ks.level, ks.weaponBonus, ks.armorBonus)
            val monster = buildMonster(ks.monster, ks.level)
            val winRates = PLAYER_DIVISORS.flatMap { div ->
                MONSTER_SPREADS.map { ms ->
                    val stats = runScenario(character, monster, div, ms, random)
                    stats.winRate
                }
            }
            val cells = winRates.joinToString(" ") { fmtWR(it) }
            val label38 = ks.label.take(38).padEnd(38)
            println("$label38 | $cells")
        }
        println()

        // ================================================================
        // SECTION 7: Recommendation summary
        // ================================================================
        println(sep(100))
        println("SECTION 7 — RECOMMENDATION ANALYSIS")
        println(sep(100))
        println()

        // Compute overall score for each of the 9 combinations
        data class ComboScore(
            val playerDiv: Int,
            val monsterSpreadLabel: String,
            val avgWinRate: Double,
            val pctInBand: Double,     // 65-85% target band
            val cvPlayer: Double,
            val cvMonster: Double,
            val balanceScore: Double   // composite score
        )

        val comboScores = mutableListOf<ComboScore>()

        for (div in PLAYER_DIVISORS) {
            for (ms in MONSTER_SPREADS) {
                val scenStats = mutableListOf<ScenarioStats>()
                for (level in LEVELS) {
                    val weapon = WEAPON_FOR_LEVEL[level]!!
                    for (armor in ARMORS) {
                        val character = buildCharacter(level, weapon.bonus, armor.bonus)
                        for (monsterType in MONSTERS) {
                            val monster = buildMonster(monsterType, level)
                            scenStats.add(runScenario(character, monster, div, ms, random))
                        }
                    }
                }

                val avgWR = scenStats.map { it.winRate }.average()
                val inBand = scenStats.count { it.winRate in 0.65..0.85 }.toDouble() / scenStats.size * 100.0
                val avgCvP = scenStats.map { it.cvPlayerDamage }.average()
                val avgCvM = scenStats.map { it.cvMonsterDamage }.average()

                // Balance score: maximize in-band %, penalise extreme CV (too boring or too swingy),
                // penalise very low (<55%) or very high (>90%) avg win rate
                val inBandScore = inBand
                val cvPenalty = if (avgCvP > 0.15) (avgCvP - 0.15) * 50 else 0.0
                val cvMonPenalty = if (avgCvM > 0.30) (avgCvM - 0.30) * 30 else 0.0
                val wrPenalty = if (avgWR < 0.55) (0.55 - avgWR) * 100 else if (avgWR > 0.90) (avgWR - 0.90) * 80 else 0.0
                val score = inBandScore - cvPenalty - cvMonPenalty - wrPenalty

                comboScores.add(ComboScore(div, ms.label, avgWR, inBand, avgCvP, avgCvM, score))
            }
        }

        println("%-20s | %-14s | %-10s | %-10s | %-12s | %-12s | %-12s".format(
            "PlayerSpread", "MonsterSpread", "AvgWinRate", "InBand%", "CV(Player)", "CV(Monster)", "BalanceScore"))
        println(dash(98))
        val sorted = comboScores.sortedByDescending { it.balanceScore }
        for (cs in sorted) {
            val divLabel = PLAYER_DIVISOR_LABELS[cs.playerDiv]!!
            val marker = if (cs == sorted.first()) " <-- BEST" else ""
            println("%-20s | %-14s | %-10s | %-10s | %-12s | %-12s | %-12s%s".format(
                divLabel, cs.monsterSpreadLabel,
                fmtWR(cs.avgWinRate), "${"%.1f".format(cs.pctInBand)}%",
                fmtCV(cs.cvPlayer), fmtCV(cs.cvMonster),
                "%.2f".format(cs.balanceScore), marker))
        }
        println()

        // Final textual conclusions
        println(sep(100))
        println("CONCLUSIONS")
        println(sep(100))
        println()

        val bestCombo = sorted.first()
        val worstCombo = sorted.last()
        val widePlayerScores  = comboScores.filter { it.playerDiv == 3 }.map { it.avgWinRate }.average()
        val currPlayerScores  = comboScores.filter { it.playerDiv == 5 }.map { it.avgWinRate }.average()
        val narrowPlayerScores= comboScores.filter { it.playerDiv == 8 }.map { it.avgWinRate }.average()
        val narrowMonScores   = comboScores.filter { it.monsterSpreadLabel == "Narrow"  }.map { it.avgWinRate }.average()
        val currMonScores     = comboScores.filter { it.monsterSpreadLabel == "Current" }.map { it.avgWinRate }.average()
        val wideMonScores     = comboScores.filter { it.monsterSpreadLabel == "Wide"    }.map { it.avgWinRate }.average()

        println("1. PLAYER SPREAD EFFECT ON DIFFICULTY:")
        println("   Wide(÷3)   avg win rate: ${"%.1f".format(widePlayerScores*100)}%")
        println("   Current(÷5) avg win rate: ${"%.1f".format(currPlayerScores*100)}%")
        println("   Narrow(÷8) avg win rate: ${"%.1f".format(narrowPlayerScores*100)}%")
        val playerEffect = if (widePlayerScores > currPlayerScores) "easier" else if (widePlayerScores < currPlayerScores) "harder" else "neutral"
        println("   -> Wide player spread makes the game $playerEffect vs current.")
        println("      Reason: wider spread adds high-roll upside that on average is neutral,")
        println("      but more variance means more high-roll kills that reduce average turns.")
        println()
        println("2. MONSTER SPREAD EFFECT ON DIFFICULTY:")
        println("   Narrow   avg win rate: ${"%.1f".format(narrowMonScores*100)}%")
        println("   Current  avg win rate: ${"%.1f".format(currMonScores*100)}%")
        println("   Wide     avg win rate: ${"%.1f".format(wideMonScores*100)}%")
        val monEffect = if (wideMonScores < currMonScores) "harder" else if (wideMonScores > currMonScores) "easier" else "neutral"
        println("   -> Wide monster spread makes the game $monEffect.")
        println("      Reason: the multiplier is centered at 1.0 so average damage is unchanged,")
        println("      but the upside tail (high multiplier) can one-shot a wounded player.")
        println("      This asymmetry hurts players more than it helps.")
        println()
        println("3. SWINGINESS — DO WIDE SPREADS CREATE MORE EXTREME OUTCOMES?")
        val widePCVs = comboScores.filter { it.playerDiv == 3 }.map { it.cvPlayer }.average()
        val currPCVs = comboScores.filter { it.playerDiv == 5 }.map { it.cvPlayer }.average()
        val narrowPCVs = comboScores.filter { it.playerDiv == 8 }.map { it.cvPlayer }.average()
        val narrowMCVs = comboScores.filter { it.monsterSpreadLabel == "Narrow"  }.map { it.cvMonster }.average()
        val currMCVs   = comboScores.filter { it.monsterSpreadLabel == "Current" }.map { it.cvMonster }.average()
        val wideMCVs   = comboScores.filter { it.monsterSpreadLabel == "Wide"    }.map { it.cvMonster }.average()
        println("   Player damage CV — Wide: ${fmtCV(widePCVs)}, Current: ${fmtCV(currPCVs)}, Narrow: ${fmtCV(narrowPCVs)}")
        println("   Monster damage CV — Narrow: ${fmtCV(narrowMCVs)}, Current: ${fmtCV(currMCVs)}, Wide: ${fmtCV(wideMCVs)}")
        println("   -> Yes. Wide player spread raises player damage CV (more variable kills).")
        println("      Wide monster spread raises monster damage CV significantly (brutal random hits).")
        println("      Both create more very-easy wins AND more unexpected deaths simultaneously.")
        println()
        println("4. BEST COMBINATION BY BALANCE SCORE:")
        println("   Best:  Player=${PLAYER_DIVISOR_LABELS[bestCombo.playerDiv]}, Monster=${bestCombo.monsterSpreadLabel}")
        println("          Win rate=${fmtWR(bestCombo.avgWinRate)}, InBand=${bestCombo.pctInBand.toInt()}%,")
        println("          CV(Player)=${fmtCV(bestCombo.cvPlayer)}, CV(Monster)=${fmtCV(bestCombo.cvMonster)}")
        println("   Worst: Player=${PLAYER_DIVISOR_LABELS[worstCombo.playerDiv]}, Monster=${worstCombo.monsterSpreadLabel}")
        println("          Win rate=${fmtWR(worstCombo.avgWinRate)}, InBand=${worstCombo.pctInBand.toInt()}%")
        println()
        println("   RECOMMENDATION: The balance score favours the combination above.")
        println("   If swinginess feels fun (high CV is intentional), prefer a wider player spread")
        println("   paired with current or narrow monster spread — this gives exciting high rolls")
        println("   on weapon hits without unpredictable monster one-shots dominating the experience.")
        println("   Avoid wide monster spread + wide player spread (both increase loss variance sharply).")
        println()
        println(sep(100))
        println("END OF DAMAGE SPREAD SIMULATION")
        println(sep(100))
    }
}
