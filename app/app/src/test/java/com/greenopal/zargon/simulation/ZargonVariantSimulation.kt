package com.greenopal.zargon.simulation

import org.junit.Test
import kotlin.random.Random

/**
 * ZARGON Boss Fight Variant Simulation
 *
 * Tests 4 ZARGON stat combinations against level 10-15 players with best equipment
 * (Atlantean Sword +35 AP bonus, Platemail +42 def bonus) using the K=20 hyperbolic damage formula.
 *
 * Damage formula: zargonDamage = max(1, (zargonAP * 20.0 / (totalDef + 20.0) * rand[0.84..1.16]).toInt())
 * Player damage: totalAP (flat, no variance)
 * Player attacks first each round.
 *
 * Two scenarios per level/variant:
 *   - No Spells: pure physical combat
 *   - One Restore: cast Restore (full HP reset, 15 MP) ONCE when HP < 35% of maxHP.
 *     Restore takes the player turn; ZARGON retaliates after every player action including heals.
 *
 * Design targets:
 *   - L10 no-spell: low win rate (player should need to heal)
 *   - L10 one-Restore: 50-75% win rate (healing helps but does not guarantee win)
 *   - L13-15 no-spell: 60-85% win rate (veteran players can win without healing)
 *   - Fight lasts 6-10 turns with healing
 */
class ZargonVariantSimulation {

    companion object {
        private const val ITERATIONS = 5000
        private const val K = 20.0
        private const val RESTORE_MP_COST = 15
        private const val HEAL_THRESHOLD = 0.35

        data class ZargonVariant(val ap: Int, val hp: Int)

        val VARIANTS = listOf(
            ZargonVariant(ap = 60, hp = 400),
            ZargonVariant(ap = 60, hp = 500),
            ZargonVariant(ap = 80, hp = 400),
            ZargonVariant(ap = 80, hp = 500)
        )

        data class PlayerConfig(
            val label: String,
            val totalAP: Int,
            val totalDef: Int,
            val maxHP: Int,
            val baseMP: Int
        )

        val PLAYERS = listOf(
            PlayerConfig("L10", totalAP = 63, totalDef = 98,  maxHP = 65,  baseMP = 55),
            PlayerConfig("L11", totalAP = 65, totalDef = 102, maxHP = 70,  baseMP = 60),
            PlayerConfig("L12", totalAP = 67, totalDef = 106, maxHP = 75,  baseMP = 65),
            PlayerConfig("L13", totalAP = 69, totalDef = 110, maxHP = 80,  baseMP = 70),
            PlayerConfig("L15", totalAP = 73, totalDef = 118, maxHP = 90,  baseMP = 80)
        )

        data class BattleResult(
            val won: Boolean,
            val turns: Int,
            val hpRemaining: Int
        )
    }

    private fun monsterDamage(variantAP: Int, totalDef: Int, random: Random): Int {
        val mult = 0.84 + random.nextDouble() * 0.32
        val raw = variantAP.toDouble() * K / (totalDef.toDouble() + K) * mult
        return maxOf(1, raw.toInt())
    }

    private fun simulateNoSpells(
        player: PlayerConfig,
        variant: ZargonVariant,
        random: Random
    ): BattleResult {
        var playerHP = player.maxHP
        var monsterHP = variant.hp
        var turns = 0

        while (playerHP > 0 && monsterHP > 0) {
            turns++
            monsterHP -= player.totalAP
            if (monsterHP <= 0) break
            playerHP -= monsterDamage(variant.ap, player.totalDef, random)
        }

        return BattleResult(won = playerHP > 0, turns = turns, hpRemaining = maxOf(0, playerHP))
    }

    private fun simulateOneRestore(
        player: PlayerConfig,
        variant: ZargonVariant,
        random: Random
    ): BattleResult {
        var playerHP = player.maxHP
        var monsterHP = variant.hp
        var turns = 0
        var restoreAvailable = player.baseMP >= RESTORE_MP_COST

        while (playerHP > 0 && monsterHP > 0) {
            turns++

            // Check if we should cast Restore INSTEAD of attacking this turn
            // Casting takes the player turn; ZARGON retaliates after
            if (restoreAvailable && playerHP.toDouble() / player.maxHP < HEAL_THRESHOLD) {
                playerHP = player.maxHP
                restoreAvailable = false
                // Monster retaliates after the heal
                playerHP -= monsterDamage(variant.ap, player.totalDef, random)
                continue
            }

            // Player attacks
            monsterHP -= player.totalAP
            if (monsterHP <= 0) break

            // Monster retaliates
            playerHP -= monsterDamage(variant.ap, player.totalDef, random)
        }

        return BattleResult(won = playerHP > 0, turns = turns, hpRemaining = maxOf(0, playerHP))
    }

    data class ScenarioStats(
        val winRate: Double,
        val avgHPLeft: Double,
        val avgTurns: Double
    )

    private fun runStats(results: List<BattleResult>): ScenarioStats {
        val wins = results.filter { it.won }
        return ScenarioStats(
            winRate = wins.size.toDouble() / results.size,
            avgHPLeft = if (wins.isNotEmpty()) wins.sumOf { it.hpRemaining }.toDouble() / wins.size else 0.0,
            avgTurns = results.sumOf { it.turns }.toDouble() / results.size
        )
    }

    @Test
    fun `simulate ZARGON boss fights - 4 AP-HP variants with and without one Restore`() {
        val random = Random(42L)

        println("=".repeat(110))
        println("ZARGON BOSS FIGHT SIMULATION — 4 VARIANTS")
        println("Iterations: $ITERATIONS per scenario | K=$K | Player attacks first each round")
        println("Gear: Atlantean Sword (+35 AP bonus), Platemail (+42 def bonus)")
        println("Heal: One Restore (full HP reset, 15 MP) cast when HP < 35% of max. Counts as player turn.")
        println("Design targets:")
        println("  L10 no-spell  : low win rate (player needs to heal)")
        println("  L10 one-Restore: 50-75% win rate")
        println("  L13-15 no-spell: 60-85% win rate")
        println("  Fight duration : 6-10 turns (with healing)")
        println("=".repeat(110))
        println()

        // Collect all summary data for final assessment table
        data class SummaryRow(
            val variant: ZargonVariant,
            val l10ns: ScenarioStats,
            val l10hr: ScenarioStats,
            val l13ns: ScenarioStats,
            val l15ns: ScenarioStats,
            val l10hrTurns: Double
        )

        val summaryRows = mutableListOf<SummaryRow>()

        for (v in VARIANTS) {
            println("=".repeat(110))
            println("ZARGON AP=${v.ap}, HP=${v.hp}")
            println("=".repeat(110))
            println()
            println("%-8s | %-27s | %-27s".format("", "--- No Spells ---", "--- One Restore ---"))
            println("%-8s | %-8s %-9s %-8s | %-8s %-9s %-8s".format(
                "Level", "WinRate", "AvgHPLft", "AvgTurns",
                         "WinRate", "AvgHPLft", "AvgTurns"))
            println("-".repeat(74))

            var l10nsStats = ScenarioStats(0.0, 0.0, 0.0)
            var l10hrStats = ScenarioStats(0.0, 0.0, 0.0)
            var l13nsStats = ScenarioStats(0.0, 0.0, 0.0)
            var l15nsStats = ScenarioStats(0.0, 0.0, 0.0)

            for (p in PLAYERS) {
                val nsResults = (1..ITERATIONS).map { simulateNoSpells(p, v, random) }
                val hrResults = (1..ITERATIONS).map { simulateOneRestore(p, v, random) }
                val ns = runStats(nsResults)
                val hr = runStats(hrResults)

                println("%-8s | %6.1f%%  %8.1f %8.1f | %6.1f%%  %8.1f %8.1f".format(
                    p.label,
                    ns.winRate * 100, ns.avgHPLeft, ns.avgTurns,
                    hr.winRate * 100, hr.avgHPLeft, hr.avgTurns
                ))

                when (p.label) {
                    "L10" -> { l10nsStats = ns; l10hrStats = hr }
                    "L13" -> l13nsStats = ns
                    "L15" -> l15nsStats = ns
                }
            }
            println()

            summaryRows.add(SummaryRow(v, l10nsStats, l10hrStats, l13nsStats, l15nsStats, l10hrStats.avgTurns))
        }

        // Design target assessment
        println("=".repeat(110))
        println("DESIGN TARGET ASSESSMENT")
        println("  T1: L10 no-spell win rate low (below 40%) — player should need to heal")
        println("  T2: L10 one-Restore win rate 50-75%        — healing helps but not guaranteed")
        println("  T3: L13-15 no-spell avg win rate 60-85%    — veteran can win without healing")
        println("  T4: L10 one-Restore avg turns 6-10         — fight feels long enough")
        println("=".repeat(110))
        println()
        println("%-18s | %-14s | %-14s | %-17s | %-11s | %-6s".format(
            "Variant", "T1 L10-NS WR", "T2 L10-HR WR", "T3 L13+L15 NS WR", "T4 HR Turns", "Score"))
        println("-".repeat(90))

        for (row in summaryRows) {
            val t1ok = row.l10ns.winRate < 0.40
            val t2ok = row.l10hr.winRate in 0.50..0.75
            val avgVeteran = (row.l13ns.winRate + row.l15ns.winRate) / 2.0
            val t3ok = avgVeteran in 0.60..0.85
            val t4ok = row.l10hrTurns in 6.0..10.0
            val score = listOf(t1ok, t2ok, t3ok, t4ok).count { it }

            val t3str = "%.0f%%/%.0f%%".format(row.l13ns.winRate * 100, row.l15ns.winRate * 100)

            println("%-18s | %5.1f%% %-7s | %5.1f%% %-7s | %-17s | %4.1f %-6s | %d/4 %s".format(
                "AP=${row.variant.ap} HP=${row.variant.hp}",
                row.l10ns.winRate * 100, if (t1ok) "[OK]" else "    ",
                row.l10hr.winRate * 100, if (t2ok) "[OK]" else "    ",
                "$t3str ${if (t3ok) "[OK]" else "    "}",
                row.l10hrTurns, if (t4ok) "[OK]" else "    ",
                score,
                if (score == 4) "<-- BEST" else if (score == 3) "<-- GOOD" else ""
            ))
        }

        println()
        println("=".repeat(110))
        println("END OF SIMULATION")
        println("=".repeat(110))
    }
}
