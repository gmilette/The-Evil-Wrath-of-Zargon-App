package com.greenopal.zargon.simulation

import org.junit.Test
import kotlin.random.Random

/**
 * ZARGON Boss Fight Simulation
 *
 * Tests five ZARGON stat variants against level 10-15 players with best equipment
 * (Atlantean Sword +35 AP, Platemail +42 def) using the K=20 hyperbolic damage formula.
 *
 * Damage formula: monsterDamage = max(1, (zargonAP * 20.0 / (totalDef + 20.0) * rand[0.84..1.16]).toInt())
 * Player damage: totalAP (flat, no variance)
 * Player attacks first each round.
 *
 * Two scenarios per level/variant:
 *   - No Spells: pure physical
 *   - Optimal Healing: cast Restore (full HP reset, 15 MP) when HP < 30%, then resume attacking.
 *     baseMP=55 at level 10+.
 *
 * Design targets:
 *   - L10 no-spells: 30-50% win rate
 *   - L10 with healing: 60-80% win rate
 *   - L13-15 no-spells: 60-80% win rate
 *   - Fight takes 6-10 turns
 */
class ZargonBossFightSimulation {

    companion object {
        private const val ITERATIONS = 5000
        private const val K = 20.0
        private const val RESTORE_MP_COST = 15
        private const val CURE_BASE_HEAL = 20   // approximate mid-roll for Cure at level 10+
        private const val CURE_MP_COST = 4
        private const val HEAL_THRESHOLD = 0.30 // cast Restore when HP drops below 30%

        data class ZargonVariant(
            val label: String,
            val ap: Int,
            val hp: Int
        )

        val VARIANTS = listOf(
            ZargonVariant("Current",   ap = 60,  hp = 300),
            ZargonVariant("Variant A", ap = 80,  hp = 400),
            ZargonVariant("Variant B", ap = 100, hp = 500),
            ZargonVariant("Variant C", ap = 120, hp = 600),
            ZargonVariant("Variant D", ap = 150, hp = 700)
        )

        data class PlayerConfig(
            val label: String,
            val totalAP: Int,
            val totalDef: Int,
            val maxHP: Int,
            val baseMP: Int = 55
        )

        // Canonical player stats: best gear at each level
        // Atlantean Sword: +35 AP bonus (weapon bonus). Platemail: +42 def bonus.
        // baseAP + 35 = totalAP; baseDP + 42 = totalDef
        val PLAYERS = listOf(
            PlayerConfig("L10", totalAP = 63, totalDef = 98,  maxHP = 65),
            PlayerConfig("L11", totalAP = 65, totalDef = 102, maxHP = 70),
            PlayerConfig("L12", totalAP = 67, totalDef = 106, maxHP = 75),
            PlayerConfig("L13", totalAP = 69, totalDef = 110, maxHP = 80),
            PlayerConfig("L15", totalAP = 73, totalDef = 118, maxHP = 90)
        )

        data class BattleResult(
            val won: Boolean,
            val turns: Int,
            val hpRemaining: Int,
            val healUsed: Boolean
        )
    }

    // -----------------------------------------------------------------------
    // Core battle loop — no spells
    // -----------------------------------------------------------------------
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

            // Player attacks first
            monsterHP -= player.totalAP
            if (monsterHP <= 0) break

            // Monster attacks
            val mult = 0.84 + random.nextDouble() * 0.32  // [0.84 .. 1.16]
            val rawDmg = variant.ap.toDouble() * K / (player.totalDef.toDouble() + K) * mult
            playerHP -= maxOf(1, rawDmg.toInt())
        }

        return BattleResult(
            won = playerHP > 0,
            turns = turns,
            hpRemaining = maxOf(0, playerHP),
            healUsed = false
        )
    }

    // -----------------------------------------------------------------------
    // Core battle loop — optimal healing
    //
    // Strategy: cast Restore (full HP reset) when current HP falls below 30% of maxHP.
    // Player has baseMP=55. Restore costs 15 MP.  That means up to 3 Restores.
    // After spending MP on Restores, if MP >= 4 also use Cure once as a buffer, but
    // the dominant strategy is Restore since it resets to full.
    //
    // Simplified: heal triggers BEFORE the monster's attack in the round where HP < threshold
    // (i.e. after player attacks and before monster attacks). This represents the player
    // recognising danger and casting the spell in the same round.
    // -----------------------------------------------------------------------
    private fun simulateWithHealing(
        player: PlayerConfig,
        variant: ZargonVariant,
        random: Random
    ): BattleResult {
        var playerHP = player.maxHP
        var currentMP = player.baseMP
        var monsterHP = variant.hp
        var turns = 0
        var healUsed = false

        while (playerHP > 0 && monsterHP > 0) {
            turns++

            // Player attacks first
            monsterHP -= player.totalAP
            if (monsterHP <= 0) break

            // Healing decision: before taking monster damage, check if we should heal
            if (playerHP.toDouble() / player.maxHP < HEAL_THRESHOLD && currentMP >= RESTORE_MP_COST) {
                playerHP = player.maxHP          // Restore = full HP reset
                currentMP -= RESTORE_MP_COST
                healUsed = true
            }

            // Monster attacks
            val mult = 0.84 + random.nextDouble() * 0.32
            val rawDmg = variant.ap.toDouble() * K / (player.totalDef.toDouble() + K) * mult
            playerHP -= maxOf(1, rawDmg.toInt())
        }

        return BattleResult(
            won = playerHP > 0,
            turns = turns,
            hpRemaining = maxOf(0, playerHP),
            healUsed = healUsed
        )
    }

    // -----------------------------------------------------------------------
    // Analytical helper: avg damage per hit (no variance)
    // -----------------------------------------------------------------------
    private fun avgDmgPerHit(variantAP: Int, totalDef: Int): Double {
        return variantAP.toDouble() * K / (totalDef.toDouble() + K)
    }

    // -----------------------------------------------------------------------
    // Analytical: turns needed to kill ZARGON (ceil(hp / totalAP))
    // -----------------------------------------------------------------------
    private fun turnsToKillZargon(variantHP: Int, totalAP: Int): Int {
        return (variantHP + totalAP - 1) / totalAP
    }

    // -----------------------------------------------------------------------
    // Analytical: HP survived without healing (hitsToKillPlayer * dmgPerHit)
    //   hitsBeforeMonsterDies = turnsToKillZargon (player attacks first, same turn count)
    //   except the monster doesn't attack on the turn it dies
    // -----------------------------------------------------------------------
    private fun analyticalHPTaken(variantAP: Int, variantHP: Int, totalAP: Int, totalDef: Int): Double {
        // Turns to kill = ceil(hp / totalAP)
        // Monster gets (turns-1) attacks in because player lands the killing blow first on turn N
        val turns = turnsToKillZargon(variantHP, totalAP)
        val monsterAttacks = turns - 1
        return monsterAttacks.toDouble() * avgDmgPerHit(variantAP, totalDef)
    }

    // -----------------------------------------------------------------------
    // Run a full set of 5000 iterations for one variant × one player config
    // Returns: (noSpells stats, withHealing stats)
    // -----------------------------------------------------------------------
    data class ScenarioStats(
        val winRate: Double,
        val avgHPLeft: Double,
        val avgTurns: Double,
        val needsHeal: Boolean        // true if avg damage taken >= maxHP without a heal
    )

    private fun runScenario(
        player: PlayerConfig,
        variant: ZargonVariant,
        random: Random
    ): Pair<ScenarioStats, ScenarioStats> {
        // No spells
        var nsWins = 0; var nsHPSum = 0; var nsTurnsSum = 0; var nsDmgSum = 0
        repeat(ITERATIONS) {
            val r = simulateNoSpells(player, variant, random)
            if (r.won) { nsWins++; nsHPSum += r.hpRemaining }
            nsTurnsSum += r.turns
            nsDmgSum += (player.maxHP - r.hpRemaining).coerceAtLeast(0)
        }
        val nsWinRate = nsWins.toDouble() / ITERATIONS
        val nsAvgHP = if (nsWins > 0) nsHPSum.toDouble() / nsWins else 0.0
        val nsAvgTurns = nsTurnsSum.toDouble() / ITERATIONS
        val nsAvgDmg = nsDmgSum.toDouble() / ITERATIONS
        val needsHeal = nsAvgDmg >= player.maxHP

        // With healing
        var whWins = 0; var whHPSum = 0; var whTurnsSum = 0
        repeat(ITERATIONS) {
            val r = simulateWithHealing(player, variant, random)
            if (r.won) { whWins++; whHPSum += r.hpRemaining }
            whTurnsSum += r.turns
        }
        val whWinRate = whWins.toDouble() / ITERATIONS
        val whAvgHP = if (whWins > 0) whHPSum.toDouble() / whWins else 0.0
        val whAvgTurns = whTurnsSum.toDouble() / ITERATIONS

        return Pair(
            ScenarioStats(nsWinRate, nsAvgHP, nsAvgTurns, needsHeal),
            ScenarioStats(whWinRate, whAvgHP, whAvgTurns, needsHeal)
        )
    }

    // -----------------------------------------------------------------------
    // Main test entry point
    // -----------------------------------------------------------------------
    @Test
    fun `simulate ZARGON boss fights at levels 10-15 with best equipment`() {
        val random = Random(42L)

        println("=".repeat(100))
        println("ZARGON BOSS FIGHT SIMULATION")
        println("Iterations: $ITERATIONS per scenario | K=$K | Player attacks first")
        println("Best gear: Atlantean Sword (+35 AP), Platemail (+42 def)")
        println("Healing: Restore (full HP, 15 MP) when HP < 30% of max. baseMP=55.")
        println("=".repeat(100))
        println()

        // ----- preliminary: analytical avg damage per hit table -----
        println("-".repeat(100))
        println("ANALYTICAL: Avg damage per hit (K=20, no variance)")
        println("-".repeat(100))
        println("%-12s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s".format(
            "Variant", "Zargon AP", "L10 def98", "L11 def102", "L12 def106", "L13 def110", "L15 def118"))
        println("-".repeat(85))
        for (v in VARIANTS) {
            val dmgs = PLAYERS.map { p -> "%-10.1f".format(avgDmgPerHit(v.ap, p.totalDef)) }
            println("%-12s | %-10d | %s".format(v.label, v.ap, dmgs.joinToString(" | ")))
        }
        println()

        // ----- preliminary: turns to kill ZARGON -----
        println("-".repeat(100))
        println("ANALYTICAL: Turns to kill ZARGON = ceil(ZargonHP / playerTotalAP)")
        println("-".repeat(100))
        println("%-12s | %-10s | %-10s | %-10s | %-10s | %-10s | %-10s".format(
            "Variant", "HP", "L10 ap63", "L11 ap65", "L12 ap67", "L13 ap69", "L15 ap73"))
        println("-".repeat(85))
        for (v in VARIANTS) {
            val turns = PLAYERS.map { p -> "%-10d".format(turnsToKillZargon(v.hp, p.totalAP)) }
            println("%-12s | %-10d | %s".format(v.label, v.hp, turns.joinToString(" | ")))
        }
        println()

        // ----- main simulation results -----
        // Collect all results first so we can also do a design target analysis
        data class Row(
            val playerLabel: String,
            val player: PlayerConfig,
            val ns: ScenarioStats,
            val wh: ScenarioStats
        )

        for (v in VARIANTS) {
            println("=".repeat(100))
            println("${v.label.uppercase()} (AP=${v.ap}, HP=${v.hp})")
            println("=".repeat(100))
            println()
            println("%-8s | %-26s | %-26s | %-10s".format(
                "", "--- No Spells ---", "--- With Healing ---", ""))
            println("%-8s | %-8s | %-8s | %-8s | %-8s | %-8s | %-8s | %-10s".format(
                "Level", "WinRate", "AvgHPLft", "AvgTurns", "WinRate", "AvgHPLft", "AvgTurns", "NeedsHeal?"))
            println("-".repeat(82))

            for (p in PLAYERS) {
                val (ns, wh) = runScenario(p, v, random)
                val healFlag = if (ns.needsHeal) "YES" else "no"
                println("%-8s | %-8s | %-8.1f | %-8.1f | %-8s | %-8.1f | %-8.1f | %-10s".format(
                    p.label,
                    "%5.1f%%".format(ns.winRate * 100),
                    ns.avgHPLeft,
                    ns.avgTurns,
                    "%5.1f%%".format(wh.winRate * 100),
                    wh.avgHPLeft,
                    wh.avgTurns,
                    healFlag
                ))
            }
            println()
        }

        // ----- design target assessment -----
        println("=".repeat(100))
        println("DESIGN TARGET ASSESSMENT")
        println("  Target 1: L10 no-spells win rate 30-50%")
        println("  Target 2: L10 with-healing win rate 60-80%")
        println("  Target 3: L13-15 no-spells win rate 60-80%")
        println("  Target 4: Fight takes 6-10 turns (use L10 avg turns as proxy)")
        println("=".repeat(100))
        println()
        println("%-12s | %-10s | %-10s | %-16s | %-10s | %-10s".format(
            "Variant", "L10-NS WR", "L10-H WR", "L13+L15 NS WR",  "L10 Turns", "Score/4"))
        println("-".repeat(78))

        for (v in VARIANTS) {
            val l10 = PLAYERS.first { it.label == "L10" }
            val l13 = PLAYERS.first { it.label == "L13" }
            val l15 = PLAYERS.first { it.label == "L15" }

            val (nsL10, whL10) = runScenario(l10, v, random)
            val (nsL13, _)     = runScenario(l13, v, random)
            val (nsL15, _)     = runScenario(l15, v, random)

            val t1ok = nsL10.winRate in 0.30..0.50
            val t2ok = whL10.winRate in 0.60..0.80
            val t3ok = (nsL13.winRate + nsL15.winRate) / 2.0 in 0.60..0.80
            val t4ok = nsL10.avgTurns in 6.0..10.0
            val score = listOf(t1ok, t2ok, t3ok, t4ok).count { it }

            val t3wr = "%.0f%%/%.0f%%".format(nsL13.winRate * 100, nsL15.winRate * 100)

            println("%-12s | %-10s | %-10s | %-16s | %-10s | %d/4 %s".format(
                v.label,
                "%5.1f%% %s".format(nsL10.winRate * 100, if (t1ok) "[OK]" else "    "),
                "%5.1f%% %s".format(whL10.winRate * 100, if (t2ok) "[OK]" else "    "),
                "$t3wr ${if (t3ok) "[OK]" else "    "}",
                "%.1f %s".format(nsL10.avgTurns, if (t4ok) "[OK]" else "    "),
                score,
                if (score == 4) "<-- BEST" else ""
            ))
        }

        println()
        println("=".repeat(100))
        println("END OF SIMULATION")
        println("=".repeat(100))
    }
}
