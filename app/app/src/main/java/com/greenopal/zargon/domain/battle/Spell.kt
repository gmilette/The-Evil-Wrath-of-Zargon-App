package com.greenopal.zargon.domain.battle

import kotlin.random.Random

/**
 * Magic spell system from QBASIC Magix procedure (ZARGON.BAS:2515)
 */
data class Spell(
    val id: Int,
    val name: String,
    val mpCost: Int,
    val requiredLevel: Int,
    val baseDamage: Int = 0, // 0 for healing spells
    val randomBonus: Int = 0,
    val isHealing: Boolean = false
) {
    /**
     * Calculate spell damage/healing
     * @param playerLevel Character level for scaling
     * @return Damage dealt or HP healed
     */
    fun calculateEffect(playerLevel: Int): Int {
        // QBASIC formula: Gain = INT(RND * randomBonus) + 1 * lev
        // Fix: Handle randomBonus == 0 to prevent Random.nextInt crash
        val randomComponent = if (randomBonus > 0) {
            Random.nextInt(1, randomBonus + 1)
        } else {
            0
        }
        val gain = randomComponent + playerLevel
        return baseDamage + gain
    }

    /**
     * Check if player has enough MP
     */
    fun canCast(currentMP: Int): Boolean {
        return currentMP >= mpCost
    }
}

/**
 * All available spells from QBASIC
 */
object Spells {
    /**
     * Flame spell (ZARGON.BAS:1386 - flamesp)
     * MP cost: 3
     * Damage: 10 + (1-10) + level
     */
    val FLAME = Spell(
        id = 1,
        name = "Flame",
        mpCost = 3,
        requiredLevel = 1,
        baseDamage = 10,
        randomBonus = 10,
        isHealing = false
    )

    /**
     * Cure spell (ZARGON.BAS:2554)
     * MP cost: 4
     * Heal: 6 + (1-4) + level
     */
    val CURE = Spell(
        id = 2,
        name = "Cure",
        mpCost = 4,
        requiredLevel = 2,
        baseDamage = 6, // Used for healing amount
        randomBonus = 4,
        isHealing = true
    )

    /**
     * Water spell (ZARGON.BAS:3322 - watersp)
     * MP cost: 8
     * Damage: 25 + (1-15) + level
     */
    val WATER = Spell(
        id = 3,
        name = "Water",
        mpCost = 8,
        requiredLevel = 3,
        baseDamage = 25,
        randomBonus = 15,
        isHealing = false
    )

    /**
     * Lightning spell (ZARGON.BAS:2461)
     * MP cost: 12
     * Damage: 25 + (1-15) + level
     */
    val LIGHTNING = Spell(
        id = 4,
        name = "Lightning",
        mpCost = 12,
        requiredLevel = 4,
        baseDamage = 25,
        randomBonus = 15,
        isHealing = false
    )

    /**
     * BubbleBlast spell (ZARGON.BAS:634 - bubble)
     * MP cost: 25
     * Damage: 45 + (1-20) + level
     */
    val BUBBLE_BLAST = Spell(
        id = 5,
        name = "BubbleBlast",
        mpCost = 25,
        requiredLevel = 5,
        baseDamage = 45,
        randomBonus = 20,
        isHealing = false
    )

    /**
     * Restore spell - Enhanced healing
     * MP cost: 15
     * Heal: 20 + (1-10) + level
     */
    val RESTORE = Spell(
        id = 6,
        name = "Restore",
        mpCost = 15,
        requiredLevel = 6,
        baseDamage = 20,
        randomBonus = 10,
        isHealing = true
    )

    /**
     * Firestorm spell - Massive fire damage
     * MP cost: 35
     * Damage: 60 + (1-25) + level
     */
    val FIRESTORM = Spell(
        id = 7,
        name = "Firestorm",
        mpCost = 35,
        requiredLevel = 7,
        baseDamage = 60,
        randomBonus = 25,
        isHealing = false
    )

    /**
     * Divine Light spell - Ultimate damage
     * MP cost: 50
     * Damage: 100 + (1-30) + level
     */
    val DIVINE_LIGHT = Spell(
        id = 8,
        name = "Divine Light",
        mpCost = 50,
        requiredLevel = 10,
        baseDamage = 100,
        randomBonus = 30,
        isHealing = false
    )

    /**
     * Get all spells in order
     */
    val ALL = listOf(FLAME, CURE, WATER, LIGHTNING, BUBBLE_BLAST, RESTORE, FIRESTORM, DIVINE_LIGHT)

    /**
     * Get spells available at a given level
     * Based on splev in QBASIC (Magix:2530)
     */
    fun getAvailableSpells(playerLevel: Int): List<Spell> {
        return ALL.filter { it.requiredLevel <= playerLevel }
    }

    /**
     * Get spell by index (1-5)
     */
    fun getByIndex(index: Int): Spell? {
        return ALL.getOrNull(index - 1)
    }
}
