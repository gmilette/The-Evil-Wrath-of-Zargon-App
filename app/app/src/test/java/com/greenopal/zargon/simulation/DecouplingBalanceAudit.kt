package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.MonsterType
import com.greenopal.zargon.ui.screens.Armor
import com.greenopal.zargon.ui.screens.Weapon
import org.junit.Test
import kotlin.random.Random

/**
 * Comprehensive balance audit for the HP/Defense decoupling changes.
 *
 * Before decoupling (old):
 *   baseDP served as both max HP and damage absorption.
 *   baseDP grew at 4 + random(0..level+1) + 1 per level (avg ~level/2 + 5 per level).
 *   currentHP = baseDP (so HP and defense were the same stat).
 *
 * After decoupling (new):
 *   baseDP = damage absorption only, grows flat +4 per level, starts at 20.
 *   maxHP  = separate hit point pool, grows 3-7 per level (avg 5), starts at 20.
 *   totalDefense = baseDP + armorBonus.
 *   damage = max(1, monsterAP - totalDefense + random(0..scalingFactor)).
 *
 * Key question: did decoupling fix or meaningfully reduce the bi-modal difficulty cliff?
 *
 * Also tests: the new monster AP values (Slime baseAP=3, Bat baseAP=4 — raised from 1 and 2),
 * the new encounter table (~3 slots each, 14% per monster), and the corrected totalDefense formula.
 */
class DecouplingBalanceAudit {

    private val ITERATIONS = 2000
    private val SEED = 42L

    // --- Armor loadouts ---
    private val armorLoadouts = listOf(
        Armor.CLOTH,
        Armor.CHAIN_MAIL,
        Armor.PLATEMAIL
    )

    // Representative weapons per level bracket
    private val weaponForLevel = mapOf(
        1  to Weapon.DAGGER,
        5  to Weapon.LONG_SWORD,
        10 to Weapon.BROAD_SWORD
    )

    private val targetLevels   = listOf(1, 5, 10)
    private val scalingFactors = (1..10).toList()

    private val regularMonsters = listOf(
        MonsterType.SLIME,
        MonsterType.BAT,
        MonsterType.BABBLE,
        MonsterType.SPOOK,
        MonsterType.BELETH,
        MonsterType.SKANDER_SNAKE,
        MonsterType.NECRO
    )

    // -------------------------------------------------------------------
    // Main test
    // -------------------------------------------------------------------
    @Test
    fun `full balance audit decoupled HP vs old coupled model`() {
        val random = Random(SEED)

        println("================================================================")
        println("DECOUPLING BALANCE AUDIT — Old Coupled vs New Decoupled System")
        println("================================================================")
        println("Iterations per scenario : $ITERATIONS")
        println("Player levels           : $targetLevels")
        println("Scaling factors         : 1-10")
        println("Armor loadouts          : ${armorLoadouts.map { it.displayName }}")
        println("Monsters (regular)      : ${regularMonsters.map { it.displayName }}")
        println()
        println("OLD model: baseDP grows +6/level avg (was also max HP), Slime AP=1, Bat AP=2")
        println("NEW model: baseDP grows flat +4/level (defense only), maxHP grows +5/level avg")
        println("           Slime AP=3, Bat AP=4, totalDefense = baseDP + armorBonus (corrected)")
        println()

        data class Key(val level: Int, val armor: Armor, val monster: MonsterType, val scale: Int)
        data class Row(
            val key: Key,
            val weapon: Weapon,
            val oldWR: Double,
            val newWR: Double,
            val oldHP: Int,
            val newHP: Int,
            val oldDef: Int,
            val newDef: Int
        )

        val rows = mutableListOf<Row>()

        for (level in targetLevels) {
            val weapon = weaponForLevel[level]!!
            for (armor in armorLoadouts) {
                val oldChar = buildOldCharacter(level, weapon, armor)
                val newChar = buildNewCharacter(level, weapon, armor)
                for (monster in regularMonsters) {
                    for (scale in scalingFactors) {
                        val monsterStats = buildMonster(monster, scale)

                        val oldWR = simulateBattle(oldChar, monsterStats, ITERATIONS, random)
                        val newWR = simulateBattle(newChar, monsterStats, ITERATIONS, random)

                        rows.add(Row(
                            key = Key(level, armor, monster, scale),
                            weapon = weapon,
                            oldWR = oldWR,
                            newWR = newWR,
                            oldHP = oldChar.currentHP,
                            newHP = newChar.currentHP,
                            oldDef = oldChar.totalDefense,
                            newDef = newChar.totalDefense
                        ))
                    }
                }
            }
        }

        // ---- REPORT A: Character stat comparison across levels ----
        printHeader("REPORT A — Character stat comparison: Old vs New model per level")
        println("  Level | Weapon          | Armor           | Old HP | New HP | Old Defense | New Defense | Old AP | New AP")
        println("  ------|-----------------|-----------------|--------|--------|-------------|-------------|--------|-------")
        for (level in targetLevels) {
            for (armor in armorLoadouts) {
                val weapon = weaponForLevel[level]!!
                val oldChar = buildOldCharacter(level, weapon, armor)
                val newChar = buildNewCharacter(level, weapon, armor)
                println("  %5d | %-15s | %-15s | %6d | %6d | %11d | %11d | %6d | %6d"
                    .format(level, weapon.displayName, armor.displayName,
                        oldChar.currentHP, newChar.currentHP,
                        oldChar.totalDefense, newChar.totalDefense,
                        oldChar.totalAP, newChar.totalAP))
            }
        }
        println()

        // ---- REPORT B: Win-rate by scaling factor (all combos averaged) ----
        printHeader("REPORT B — Win-rate by scaling factor (all level/armor/monster combos averaged)")
        println("  Scaling | Old (coupled) | New (decoupled) | Delta")
        println("  --------|---------------|-----------------|------")
        for (scale in scalingFactors) {
            val sub = rows.filter { it.key.scale == scale }
            val oldAvg = sub.map { it.oldWR }.average()
            val newAvg = sub.map { it.newWR }.average()
            val delta  = newAvg - oldAvg
            println("  scale=%-2d | %11.1f%% | %13.1f%% | %+6.1f%%"
                .format(scale, oldAvg * 100, newAvg * 100, delta * 100))
        }
        println()

        // ---- REPORT C: Win-rate by level (averaged across all monsters/armor) ----
        printHeader("REPORT C — Win-rate by level and scaling (averaged across all monsters and armor)")
        println("  Level | Scaling | Old (coupled) | New (decoupled) | Delta")
        println("  ------|---------|---------------|-----------------|------")
        for (level in targetLevels) {
            for (scale in scalingFactors) {
                val sub = rows.filter { it.key.level == level && it.key.scale == scale }
                val oldAvg = sub.map { it.oldWR }.average()
                val newAvg = sub.map { it.newWR }.average()
                val delta  = newAvg - oldAvg
                println("  %5d | %7d | %11.1f%% | %13.1f%% | %+6.1f%%"
                    .format(level, scale, oldAvg * 100, newAvg * 100, delta * 100))
            }
        }
        println()

        // ---- REPORT D: Mid-game chain mail detail (level 5) ----
        printHeader("REPORT D — Mid-game detail (level 5, chain mail) — in-band = 70-90% target")
        println("  Monster        | Scale | Old WR  | New WR  | Delta  | Old in-band | New in-band")
        println("  ---------------|-------|---------|---------|--------|-------------|------------")
        val midRows = rows.filter { it.key.level == 5 && it.key.armor == Armor.CHAIN_MAIL }
            .sortedWith(compareBy({ it.key.monster.name }, { it.key.scale }))
        for (row in midRows) {
            val oldBand = if (row.oldWR in 0.70..0.90) "YES" else "   "
            val newBand = if (row.newWR in 0.70..0.90) "YES" else "   "
            println("  %-14s | %5d | %5.1f%%  | %5.1f%%  | %+5.1f%% | %-11s | %s"
                .format(row.key.monster.displayName, row.key.scale,
                    row.oldWR * 100, row.newWR * 100,
                    (row.newWR - row.oldWR) * 100,
                    oldBand, newBand))
        }
        println()

        // ---- REPORT E: Cliff analysis — max single-step drop ----
        printHeader("REPORT E — Bi-modal cliff analysis (max single-step win-rate drop per combo)")
        println("  A smaller value = smoother curve = less cliff.")
        println()
        println("  Level | Armor           | Monster        | Old max drop | New max drop | Improved?")
        println("  ------|-----------------|----------------|--------------|--------------|----------")
        var totalOldDrop = 0.0
        var totalNewDrop = 0.0
        var count = 0
        var improvedCount = 0
        for (level in targetLevels) {
            for (armor in armorLoadouts) {
                for (monster in regularMonsters) {
                    val series = rows.filter {
                        it.key.level == level && it.key.armor == armor && it.key.monster == monster
                    }.sortedBy { it.key.scale }
                    if (series.size < 2) continue
                    val oldDrop = maxDrop(series.map { it.oldWR })
                    val newDrop = maxDrop(series.map { it.newWR })
                    val improved = if (newDrop < oldDrop - 0.005) "YES" else if (newDrop > oldDrop + 0.005) "WORSE" else "SAME"
                    totalOldDrop += oldDrop
                    totalNewDrop += newDrop
                    count++
                    if (improved == "YES") improvedCount++
                    println("  %5d | %-15s | %-14s | %10.1f%% | %10.1f%% | %s"
                        .format(level, armor.displayName, monster.displayName,
                            oldDrop * 100, newDrop * 100, improved))
                }
            }
        }
        println()
        println("  Summary: $improvedCount / $count combos improved")
        println("  Old avg max drop: ${"%.1f".format(totalOldDrop / count * 100)}%")
        println("  New avg max drop: ${"%.1f".format(totalNewDrop / count * 100)}%")
        println()

        // ---- REPORT F: Per-monster win rates across all scales (new model) ----
        printHeader("REPORT F — Per-monster win rates by level (NEW model, chain mail, avg across scales)")
        println("  Monster        | Lv1 avg | Lv5 avg | Lv10 avg")
        println("  ---------------|---------|---------|----------")
        for (monster in regularMonsters) {
            val lv1  = rows.filter { it.key.monster == monster && it.key.level == 1  && it.key.armor == Armor.CHAIN_MAIL }.map { it.newWR }.average()
            val lv5  = rows.filter { it.key.monster == monster && it.key.level == 5  && it.key.armor == Armor.CHAIN_MAIL }.map { it.newWR }.average()
            val lv10 = rows.filter { it.key.monster == monster && it.key.level == 10 && it.key.armor == Armor.CHAIN_MAIL }.map { it.newWR }.average()
            println("  %-14s | %5.1f%%  | %5.1f%%  | %5.1f%%"
                .format(monster.displayName, lv1 * 100, lv5 * 100, lv10 * 100))
        }
        println()

        // ---- REPORT G: Early-game spotlight (level 1) — Slime/Bat AP change impact ----
        printHeader("REPORT G — Early-game spotlight (level 1) — Slime and Bat AP buff impact")
        println("  Slime baseAP raised 1 -> 3, Bat baseAP raised 2 -> 4")
        println()
        println("  Monster | Scale | Armor     | Old WR | New WR | Delta")
        println("  --------|-------|-----------|--------|--------|------")
        for (monster in listOf(MonsterType.SLIME, MonsterType.BAT)) {
            for (armor in armorLoadouts) {
                for (scale in 1..5) {
                    val row = rows.find {
                        it.key.monster == monster && it.key.level == 1 &&
                        it.key.armor == armor && it.key.scale == scale
                    } ?: continue
                    println("  %-7s | %5d | %-9s | %4.1f%% | %4.1f%% | %+5.1f%%"
                        .format(monster.displayName, scale, armor.displayName,
                            row.oldWR * 100, row.newWR * 100,
                            (row.newWR - row.oldWR) * 100))
                }
            }
        }
        println()

        // ---- REPORT H: Armor effectiveness check (does armor matter more now?) ----
        printHeader("REPORT H — Armor effectiveness (does armor matter in new model?)")
        println("  For each level and monster at scale 5, compare cloth vs platemail win rate.")
        println()
        println("  Level | Monster        | Cloth WR (old) | Cloth WR (new) | Plate WR (old) | Plate WR (new) | Armor delta (new)")
        println("  ------|----------------|----------------|----------------|----------------|----------------|------------------")
        for (level in targetLevels) {
            for (monster in regularMonsters) {
                val clothOld  = rows.find { it.key.level == level && it.key.monster == monster && it.key.armor == Armor.CLOTH       && it.key.scale == 5 }?.oldWR ?: Double.NaN
                val clothNew  = rows.find { it.key.level == level && it.key.monster == monster && it.key.armor == Armor.CLOTH       && it.key.scale == 5 }?.newWR ?: Double.NaN
                val plateOld  = rows.find { it.key.level == level && it.key.monster == monster && it.key.armor == Armor.PLATEMAIL   && it.key.scale == 5 }?.oldWR ?: Double.NaN
                val plateNew  = rows.find { it.key.level == level && it.key.monster == monster && it.key.armor == Armor.PLATEMAIL   && it.key.scale == 5 }?.newWR ?: Double.NaN
                val armorDelta = if (!plateNew.isNaN() && !clothNew.isNaN()) plateNew - clothNew else Double.NaN
                println("  %5d | %-14s | %12.1f%% | %12.1f%% | %12.1f%% | %12.1f%% | %+15.1f%%"
                    .format(level, monster.displayName,
                        clothOld * 100, clothNew * 100,
                        plateOld * 100, plateNew * 100,
                        armorDelta * 100))
            }
        }
        println()

        // ---- REPORT I: High-level invincibility check ----
        printHeader("REPORT I — High-level invincibility check (level 10 + platemail vs all monsters all scales)")
        println("  Are players invincible at level 10 with best armor in new model?")
        println()
        println("  Monster        | Scale | Old WR | New WR | Dmg per hit (new model)")
        println("  ---------------|-------|--------|--------|------------------------")
        val newChar10 = buildNewCharacter(10, weaponForLevel[10]!!, Armor.PLATEMAIL)
        for (monster in regularMonsters) {
            for (scale in scalingFactors) {
                val mon = buildMonster(monster, scale)
                val monAP = mon.attackPower.toDouble()
                val def   = newChar10.totalDefense.toDouble()
                val avgDmg = maxOf(1.0, monAP - def + scale / 2.0)
                val row = rows.find { it.key.level == 10 && it.key.monster == monster && it.key.armor == Armor.PLATEMAIL && it.key.scale == scale }
                println("  %-14s | %5d | %4.1f%% | %4.1f%% | %.1f"
                    .format(monster.displayName, scale,
                        (row?.oldWR ?: Double.NaN) * 100,
                        (row?.newWR ?: Double.NaN) * 100,
                        avgDmg))
            }
        }
        println()

        // ---- REPORT J: 70-90% target band proportion ----
        printHeader("REPORT J — Proportion of scenarios in the 70-90% target win-rate band")
        val total = rows.size.toDouble()
        val oldInBand = rows.count { it.oldWR in 0.70..0.90 }
        val newInBand = rows.count { it.newWR in 0.70..0.90 }
        println("  Old (coupled)   : $oldInBand / ${rows.size} scenarios (${(oldInBand / total * 100).toInt()}%) in 70-90% band")
        println("  New (decoupled) : $newInBand / ${rows.size} scenarios (${(newInBand / total * 100).toInt()}%) in 70-90% band")
        println()

        // ---- REPORT K: Overall verdict ----
        printHeader("REPORT K — Verdict summary")
        val oldAvgAll = rows.map { it.oldWR }.average()
        val newAvgAll = rows.map { it.newWR }.average()
        val oldCliff  = totalOldDrop / count * 100
        val newCliff  = totalNewDrop / count * 100
        println("  Overall avg win rate — Old: ${"%.1f".format(oldAvgAll * 100)}%   New: ${"%.1f".format(newAvgAll * 100)}%")
        println("  Avg cliff severity   — Old: ${"%.1f".format(oldCliff)}%   New: ${"%.1f".format(newCliff)}%")
        println("  Scenarios in 70-90%  — Old: ${(oldInBand / total * 100).toInt()}%   New: ${(newInBand / total * 100).toInt()}%")
        println()

        println("================================================================")
        println("END OF AUDIT")
        println("================================================================")
    }

    // -------------------------------------------------------------------
    // Old model character builder (pre-decoupling)
    // baseDP grows +6/level (avg of old formula), currentHP = baseDP
    // Slime/Bat have old AP values (AP=1, AP=2)
    // -------------------------------------------------------------------
    private fun buildOldCharacter(level: Int, weapon: Weapon, armor: Armor): CharacterStats {
        val baseAP = 5 + (level - 1) * 3
        val baseDP = 20 + (level - 1) * 6   // old growth rate ~+6 per level
        val baseMP = 10 + (level - 1) * 4
        return CharacterStats(
            baseAP      = baseAP,
            baseDP      = baseDP,
            maxHP       = baseDP,            // old: HP = baseDP
            currentHP   = baseDP,
            baseMP      = baseMP,
            currentMP   = baseMP,
            level       = level,
            weaponBonus = weapon.attackBonus,
            armorBonus  = armor.defenseBonus
        )
    }

    // -------------------------------------------------------------------
    // New model character builder (post-decoupling)
    // baseDP grows flat +4/level (defense only), maxHP separate +5/level avg
    // -------------------------------------------------------------------
    private fun buildNewCharacter(level: Int, weapon: Weapon, armor: Armor): CharacterStats {
        val baseAP  = 5 + (level - 1) * 3
        val baseDP  = 20 + (level - 1) * 4  // flat +4 defense only
        val maxHP   = 20 + (level - 1) * 5  // avg 3-7 = 5 per level
        val baseMP  = 10 + (level - 1) * 4
        return CharacterStats(
            baseAP      = baseAP,
            baseDP      = baseDP,
            maxHP       = maxHP,
            currentHP   = maxHP,
            baseMP      = baseMP,
            currentMP   = baseMP,
            level       = level,
            weaponBonus = weapon.attackBonus,
            armorBonus  = armor.defenseBonus
        )
    }

    // -------------------------------------------------------------------
    // Monster builder — uses current MonsterType (Slime AP=3, Bat AP=4)
    // Old monster AP is simulated below for the old model comparison
    // -------------------------------------------------------------------
    private fun buildMonster(type: MonsterType, scalingFactor: Int): MonsterStats {
        return MonsterStats(
            type          = type,
            attackPower   = type.baseAP * scalingFactor,
            currentHP     = type.baseDP * scalingFactor,
            maxHP         = type.baseDP * scalingFactor,
            scalingFactor = scalingFactor
        )
    }

    // -------------------------------------------------------------------
    // Battle simulator — uses exact game formula
    // damage = max(1, monsterAP - totalDefense + random(0..scalingFactor))
    // -------------------------------------------------------------------
    private fun simulateBattle(
        character: CharacterStats,
        monster: MonsterStats,
        iterations: Int,
        random: Random
    ): Double {
        var wins = 0
        repeat(iterations) {
            var playerHP  = character.currentHP
            var monsterHP = monster.currentHP
            while (playerHP > 0 && monsterHP > 0) {
                monsterHP -= character.totalAP
                if (monsterHP <= 0) { wins++; break }
                val rand = random.nextInt(0, monster.scalingFactor + 1)
                val dmg  = maxOf(1, monster.attackPower - character.totalDefense + rand)
                playerHP -= dmg
            }
        }
        return wins.toDouble() / iterations
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------
    private fun maxDrop(winRates: List<Double>): Double {
        if (winRates.size < 2) return 0.0
        return (0 until winRates.size - 1)
            .maxOf { winRates[it] - winRates[it + 1] }
            .coerceAtLeast(0.0)
    }

    private fun printHeader(title: String) {
        println("--- $title ---")
    }
}
