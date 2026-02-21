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
    fun calculateEffect(playerLevel: Int, effectMultiplier: Float = 1.0f): Int {
        val randomComponent = if (randomBonus > 0) {
            Random.nextInt(1, randomBonus + 1)
        } else {
            0
        }
        val gain = randomComponent + playerLevel
        val baseEffect = baseDamage + gain
        return (baseEffect * effectMultiplier).toInt()
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
     * MP cost: 3 (30% of L1 pool)
     * Damage: 6 + (1-8) + level = 8-15 at L1
     * Situationally useful to finish wounded monsters; not dominant vs regular attack (7 dmg at L1)
     */
    val FLAME = Spell(
        id = 1,
        name = "Flame",
        mpCost = 3,
        requiredLevel = 1,
        baseDamage = 8,
        randomBonus = 12,
        isHealing = false
    )

    /**
     * Cure spell (ZARGON.BAS:2554)
     * MP cost: 4 (27% of L2 pool)
     * Heal: 2 + (1-3) + level = 5-7 at L2 (~24% of maxHP=25)
     * Light emergency heal; meaningful but not a free full restore
     */
    val CURE = Spell(
        id = 2,
        name = "Cure",
        mpCost = 4,
        requiredLevel = 2,
        baseDamage = 15,
        randomBonus = 5,
        isHealing = true
    )

    /**
     * Water spell (ZARGON.BAS:3322 - watersp)
     * MP cost: 8 (40% of L3 pool)
     * Damage: 14 + (1-12) + level = 18-29 at L3
     * ~1.7x regular attack (14 dmg at L3); clear step up from Flame
     */
    val WATER = Spell(
        id = 3,
        name = "Water",
        mpCost = 8,
        requiredLevel = 3,
        baseDamage = 18,
        randomBonus = 15,
        isHealing = false
    )

    /**
     * Lightning spell (ZARGON.BAS:2461)
     * MP cost: 12 (48% of L4 pool)
     * Damage: 22 + (1-14) + level = 27-40 at L4
     * ~2x regular attack (17 dmg at L4); clear step up from Water
     */
    val LIGHTNING = Spell(
        id = 4,
        name = "Lightning",
        mpCost = 12,
        requiredLevel = 4,
        baseDamage = 28,
        randomBonus = 18,
        isHealing = false
    )

    /**
     * BubbleBlast spell (ZARGON.BAS:634 - bubble)
     * MP cost: 25 (83% of L5 pool)
     * Damage: 42 + (1-20) + level = 48-67 at L5
     * ~2.6x regular attack (22 dmg at L5); major resource commitment
     */
    val BUBBLE_BLAST = Spell(
        id = 5,
        name = "BubbleBlast",
        mpCost = 25,
        requiredLevel = 5,
        baseDamage = 52,
        randomBonus = 25,
        isHealing = false
    )

    /**
     * Restore spell - Enhanced healing
     * MP cost: 15 (43% of L6 pool)
     * Heal: 12 + (1-10) + level = 19-28 at L6 (~47% of maxHP=50)
     * Substantial mid-battle recovery; meaningful MP investment
     */
    val RESTORE = Spell(
        id = 6,
        name = "Restore",
        mpCost = 15,
        requiredLevel = 6,
        baseDamage = 500,
        randomBonus = 0,
        isHealing = true
    )

    /**
     * Firestorm spell - Massive fire damage
     * MP cost: 35 (87% of L7 pool)
     * Damage: 58 + (1-24) + level = 66-89 at L7
     * ~2.4x regular attack (32 dmg at L7); near full-bar nuke
     */
    val FIRESTORM = Spell(
        id = 7,
        name = "Firestorm",
        mpCost = 35,
        requiredLevel = 7,
        baseDamage = 72,
        randomBonus = 30,
        isHealing = false
    )

    /**
     * Divine Light spell - Ultimate damage
     * MP cost: 50 (91% of L10 pool)
     * Damage: 100 + (1-30) + level = 111-140 at L10
     * ~3x regular attack (47 dmg at L10); full-bar commitment; decisive battle-ender
     */
    val DIVINE_LIGHT = Spell(
        id = 8,
        name = "Divine Light",
        mpCost = 50,
        requiredLevel = 10,
        baseDamage = 125,
        randomBonus = 40,
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
