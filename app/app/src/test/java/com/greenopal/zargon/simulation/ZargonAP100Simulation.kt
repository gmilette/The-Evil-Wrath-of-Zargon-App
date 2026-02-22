package com.greenopal.zargon.simulation

import org.junit.Test
import kotlin.random.Random

/**
 * ZARGON Boss Fight — AP=100, HP=400
 *
 * Fixed ZARGON stats: AP=100, HP=400.
 * Players: best gear (Atlantean Sword +35 AP bonus, Platemail +42 def bonus) at levels 10-15.
 * Damage formula: zargonDamage = max(1, (100 * 20.0 / (totalDef + 20.0) * rand[0.84..1.16]).toInt())
 * Player damage: totalAP flat, no variance.
 * Player attacks first each round.
 *
 * Three scenarios:
 *   No Spells     — pure physical only.
 *   One Restore   — cast Restore (full HP reset) when HP < 35%. One cast only. Casting costs a turn;
 *                   ZARGON retaliates after every player action including heals.
 *   Restore+Cure  — Restore when HP < 35% (once), then Cure (heals 15 + rand(1..5) + level HP)
 *                   when HP < 35% again after Restore is spent. Two casts total.
 *
 * Design targets — "good" results look like:
 *   L10 no-spells  : 20-40% win rate (boss is genuinely dangerous at entry level)
 *   L10 one-Restore: 50-75% win rate (single heal provides meaningful but not guaranteed survival)
 *   L10 Restore+Cure: 65-90% win rate (two heals pushes player over the hump)
 *   L15 no-spells  : 60-80% win rate (high-level player has a real shot without healing)
 *   AvgTurns       : 6-10 turns per fight (feels epic, not instant)
 *
 * Iterations: 5000 per scenario.
 */
class ZargonAP100Simulation {

    companion object {
        private const val ITERATIONS = 5000
        private const val K = 20.0
        private const val ZARGON_AP = 100
        private const val ZARGON_HP = 400

        private const val RESTORE_MP_COST = 15
        private const val CURE_MP_COST = 4
        private const val HEAL_THRESHOLD = 0.35

        // Cure healing formula from Spell.kt: baseDamage=15, randomBonus=5
        // effect = 15 + random(1..5) + playerLevel
        private fun cureHeal(playerLevel: Int, random: Random): Int =
            15 + random.nextInt(1, 6) + playerLevel

        data class PlayerConfig(
            val label: String,
            val level: Int,
            val totalAP: Int,
            val totalDef: Int,
            val maxHP: Int,
            val baseMP: Int
        )

        // Canonical stats: Atlantean Sword (+35 AP bonus), Platemail (+42 def bonus)
        val PLAYERS = listOf(
            PlayerConfig("L10", level = 10, totalAP = 63, totalDef = 98,  maxHP = 65,  baseMP = 55),
            PlayerConfig("L11", level = 11, totalAP = 65, totalDef = 102, maxHP = 70,  baseMP = 60),
            PlayerConfig("L12", level = 12, totalAP = 67, totalDef = 106, maxHP = 75,  baseMP = 65),
            PlayerConfig("L13", level = 13, totalAP = 69, totalDef = 110, maxHP = 80,  baseMP = 70),
            PlayerConfig("L15", level = 15, totalAP = 73, totalDef = 118, maxHP = 90,  baseMP = 80)
        )

        data class BattleResult(val won: Boolean, val turns: Int, val hpRemaining: Int)

        data class ScenarioStats(val winRate: Double, val avgTurns: Double, val avgHPLeft: Double)
    }

    private fun zargonHit(totalDef: Int, random: Random): Int {
        val mult = 0.84 + random.nextDouble() * 0.32
        val raw = ZARGON_AP.toDouble() * K / (totalDef.toDouble() + K) * mult
        return maxOf(1, raw.toInt())
    }

    // Scenario 1: No spells — pure physical
    private fun simulateNoSpells(player: PlayerConfig, random: Random): BattleResult {
        var playerHP = player.maxHP
        var monsterHP = ZARGON_HP
        var turns = 0

        while (playerHP > 0 && monsterHP > 0) {
            turns++
            monsterHP -= player.totalAP
            if (monsterHP <= 0) break
            playerHP -= zargonHit(player.totalDef, random)
        }

        return BattleResult(won = playerHP > 0, turns = turns, hpRemaining = maxOf(0, playerHP))
    }

    // Scenario 2: One Restore — full HP reset when HP < 35%, one cast only
    // Casting a spell takes the player's turn; ZARGON always retaliates after any player action.
    private fun simulateOneRestore(player: PlayerConfig, random: Random): BattleResult {
        var playerHP = player.maxHP
        var monsterHP = ZARGON_HP
        var turns = 0
        var restoreAvailable = player.baseMP >= RESTORE_MP_COST

        while (playerHP > 0 && monsterHP > 0) {
            turns++

            if (restoreAvailable && playerHP.toDouble() / player.maxHP < HEAL_THRESHOLD) {
                // Spend the turn casting Restore
                playerHP = player.maxHP
                restoreAvailable = false
                // ZARGON retaliates after the heal cast
                playerHP -= zargonHit(player.totalDef, random)
                continue
            }

            // Player attacks
            monsterHP -= player.totalAP
            if (monsterHP <= 0) break

            // ZARGON retaliates
            playerHP -= zargonHit(player.totalDef, random)
        }

        return BattleResult(won = playerHP > 0, turns = turns, hpRemaining = maxOf(0, playerHP))
    }

    // Scenario 3: One Restore + One Cure
    // Restore when HP < 35% (full reset), then Cure when HP < 35% again after Restore spent.
    // Cure heals 15 + rand(1..5) + playerLevel HP (capped at maxHP), costs a turn, ZARGON retaliates.
    private fun simulateRestoreAndCure(player: PlayerConfig, random: Random): BattleResult {
        var playerHP = player.maxHP
        var monsterHP = ZARGON_HP
        var turns = 0
        var restoreAvailable = player.baseMP >= RESTORE_MP_COST
        // Cure costs only 4 MP; after spending 15 on Restore, remaining MP > 4 at all listed levels
        var cureAvailable = !restoreAvailable.not() && player.baseMP >= RESTORE_MP_COST + CURE_MP_COST

        while (playerHP > 0 && monsterHP > 0) {
            turns++

            val belowThreshold = playerHP.toDouble() / player.maxHP < HEAL_THRESHOLD

            if (belowThreshold && restoreAvailable) {
                // First heal: Restore (full HP reset)
                playerHP = player.maxHP
                restoreAvailable = false
                playerHP -= zargonHit(player.totalDef, random)
                continue
            }

            if (belowThreshold && !restoreAvailable && cureAvailable) {
                // Second heal: Cure — heals a partial amount
                val healed = cureHeal(player.level, random)
                playerHP = minOf(player.maxHP, playerHP + healed)
                cureAvailable = false
                playerHP -= zargonHit(player.totalDef, random)
                continue
            }

            // Player attacks
            monsterHP -= player.totalAP
            if (monsterHP <= 0) break

            // ZARGON retaliates
            playerHP -= zargonHit(player.totalDef, random)
        }

        return BattleResult(won = playerHP > 0, turns = turns, hpRemaining = maxOf(0, playerHP))
    }

    private fun stats(results: List<BattleResult>): ScenarioStats {
        val wins = results.filter { it.won }
        return ScenarioStats(
            winRate = wins.size.toDouble() / results.size,
            avgTurns = results.sumOf { it.turns }.toDouble() / results.size,
            avgHPLeft = if (wins.isNotEmpty()) wins.sumOf { it.hpRemaining }.toDouble() / wins.size else 0.0
        )
    }

    @Test
    fun `simulate ZARGON AP100 HP400 boss fight across levels 10-15 three spell scenarios`() {
        val random = Random(42L)

        val sep = "=".repeat(100)
        val dash = "-".repeat(100)

        println(sep)
        println("ZARGON BOSS FIGHT  AP=100, HP=400")
        println("Iterations : $ITERATIONS per scenario")
        println("Formula    : zargonDmg = max(1, (100 * 20.0 / (totalDef + 20.0) * rand[0.84..1.16]).toInt())")
        println("Player dmg : totalAP (flat)")
        println("Turn order : Player attacks first; ZARGON retaliates after every player action incl. heals")
        println("Gear       : Atlantean Sword (+35 AP bonus), Platemail (+42 def bonus)")
        println("Restore    : full HP reset, 15 MP, triggers at HP < 35%, one cast, counts as full turn")
        println("Cure       : 15 + rand(1..5) + level HP healed, 4 MP, triggers at HP < 35% post-Restore, one cast")
        println(sep)
        println()

        // Analytical: avg damage per ZARGON hit at each defense level
        println(dash)
        println("ANALYTICAL  avg ZARGON damage per hit (mid-roll, K=20)")
        println(dash)
        println("%-6s | %-8s | %-8s | %-12s | %-12s".format("Level", "TotalDef", "MinHit", "MidHit", "MaxHit"))
        println("-".repeat(58))
        for (p in PLAYERS) {
            val minHit = maxOf(1, (ZARGON_AP.toDouble() * K / (p.totalDef + K) * 0.84).toInt())
            val midHit = maxOf(1, (ZARGON_AP.toDouble() * K / (p.totalDef + K) * 1.00).toInt())
            val maxHit = maxOf(1, (ZARGON_AP.toDouble() * K / (p.totalDef + K) * 1.16).toInt())
            val turnsToKill = (ZARGON_HP + p.totalAP - 1) / p.totalAP
            println("%-6s | %-8d | %-8d | %-12d | %-12d | turnsToKillZargon=%d".format(
                p.label, p.totalDef, minHit, midHit, maxHit, turnsToKill))
        }
        println()

        // -----------------------------------------------------------------------
        // Compact summary table in the requested format
        // -----------------------------------------------------------------------
        data class LevelRow(
            val label: String,
            val ns: ScenarioStats,
            val hr: ScenarioStats,
            val rc: ScenarioStats
        )

        val rows = mutableListOf<LevelRow>()

        // Collect detailed per-level output first
        for (p in PLAYERS) {
            val nsResults = (1..ITERATIONS).map { simulateNoSpells(p, random) }
            val hrResults = (1..ITERATIONS).map { simulateOneRestore(p, random) }
            val rcResults = (1..ITERATIONS).map { simulateRestoreAndCure(p, random) }
            rows.add(LevelRow(p.label, stats(nsResults), stats(hrResults), stats(rcResults)))
        }

        // Requested report format
        println(sep)
        println("ZARGON AP=100, HP=400")
        println("%-8s | %-12s | %-26s | %-26s".format(
            "", "No Spells", "One Restore", "Restore+Cure"))
        println("%-8s | %-12s | %-12s %-12s | %-12s %-12s".format(
            "Level", "WinRate", "WinRate", "AvgTurns", "WinRate", "AvgTurns"))
        println("-".repeat(78))

        for (r in rows) {
            println("%-8s | %-12s | %-12s %-12s | %-12s %-12s".format(
                r.label,
                "%.1f%%".format(r.ns.winRate * 100),
                "%.1f%%".format(r.hr.winRate * 100),
                "%.1f".format(r.hr.avgTurns),
                "%.1f%%".format(r.rc.winRate * 100),
                "%.1f".format(r.rc.avgTurns)
            ))
        }
        println()

        // Extended detail table
        println(sep)
        println("EXTENDED DETAIL  (AvgHPLeft shown for winning battles only)")
        println("%-8s | %-10s %-8s | %-10s %-8s %-8s | %-10s %-8s %-8s".format(
            "Level",
            "NS WinRate", "NS HPLft",
            "HR WinRate", "HR HPLft", "HR Turns",
            "RC WinRate", "RC HPLft", "RC Turns"))
        println("-".repeat(90))

        for (r in rows) {
            println("%-8s | %9.1f%% %8.1f | %9.1f%% %8.1f %8.1f | %9.1f%% %8.1f %8.1f".format(
                r.label,
                r.ns.winRate * 100, r.ns.avgHPLeft,
                r.hr.winRate * 100, r.hr.avgHPLeft, r.hr.avgTurns,
                r.rc.winRate * 100, r.rc.avgHPLeft, r.rc.avgTurns
            ))
        }
        println()

        // Design target assessment
        println(sep)
        println("DESIGN TARGET ASSESSMENT")
        println("  T1: L10 no-spells win rate 20-40% (boss is genuinely dangerous)")
        println("  T2: L10 one-Restore win rate 50-75% (heal helps but not guaranteed)")
        println("  T3: L10 Restore+Cure win rate 65-90% (two heals provide real uplift)")
        println("  T4: L15 no-spells win rate 60-80% (high-level can solo)")
        println("  T5: AvgTurns with healing 6-10 (fight feels epic)")
        println(sep)
        println()

        val l10 = rows.first { it.label == "L10" }
        val l15 = rows.first { it.label == "L15" }
        val hrTurnsOk = rows.all { it.hr.avgTurns in 6.0..10.0 }

        val t1ok = l10.ns.winRate in 0.20..0.40
        val t2ok = l10.hr.winRate in 0.50..0.75
        val t3ok = l10.rc.winRate in 0.65..0.90
        val t4ok = l15.ns.winRate in 0.60..0.80
        val t5ok = hrTurnsOk

        println("  T1 L10 no-spells  : %.1f%% %s".format(l10.ns.winRate * 100, if (t1ok) "[PASS]" else "[FAIL]"))
        println("  T2 L10 one-Restore: %.1f%% %s".format(l10.hr.winRate * 100, if (t2ok) "[PASS]" else "[FAIL]"))
        println("  T3 L10 Restore+Cure: %.1f%% %s".format(l10.rc.winRate * 100, if (t3ok) "[PASS]" else "[FAIL]"))
        println("  T4 L15 no-spells  : %.1f%% %s".format(l15.ns.winRate * 100, if (t4ok) "[PASS]" else "[FAIL]"))
        println("  T5 all HR avgTurns in 6-10: %s".format(if (t5ok) "[PASS]" else "[FAIL]"))
        val score = listOf(t1ok, t2ok, t3ok, t4ok, t5ok).count { it }
        println()
        println("  Score: $score/5")
        println()
        println(sep)
        println("END OF SIMULATION")
        println(sep)
    }
}
