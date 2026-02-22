package com.greenopal.zargon.data.models

import kotlinx.serialization.Serializable

/**
 * Character statistics and state.
 * Maps to QBASIC COMMON SHARED variables: cAP, cDP, cMP, lev, gold, ex, etc.
 */
@Serializable
data class CharacterStats(
    // Combat stats (QBASIC: cAP, cDP, cMP)
    val baseAP: Int = 5,        // Base Attack Power (BcAP in QBASIC)
    val baseDP: Int = 20,       // Base Defense — damage absorption only (BcDP in QBASIC)
    val maxHP: Int = 20,        // Maximum hit points — separate from defense
    val currentHP: Int = 20,    // Current HP (cDP in QBASIC)
    val baseMP: Int = 10,       // Base Magic Power (BcMP in QBASIC)
    val currentMP: Int = 10,    // Current MP (cMP in QBASIC)

    // Progression
    val level: Int = 1,         // Character level (lev in QBASIC)
    val experience: Int = 0,    // Experience points (ex in QBASIC)
    val gold: Int = 0,          // Gold (gold in QBASIC)

    // Equipment bonuses (QBASIC: wgain, again)
    val weaponBonus: Int = 0,   // Weapon attack bonus (wgain)
    val armorBonus: Int = 0,    // Armor defense bonus (again)
    val weaponStatus: Int = 0,  // Weapon level (wstatus)
    val armorStatus: Int = 0,   // Armor level (astatus)
) {
    // Computed properties
    val totalAP: Int get() = baseAP + weaponBonus
    val totalDefense: Int get() = baseDP + armorBonus
    val armorDefenseBonus: Int get() = armorBonus
    val maxMP: Int get() = baseMP

    val isAlive: Boolean get() = currentHP > 0
    val hpPercentage: Float get() = currentHP.toFloat() / maxHP.toFloat()
    val mpPercentage: Float get() = currentMP.toFloat() / maxMP.toFloat()

    /**
     * Take damage and return updated stats
     */
    fun takeDamage(damage: Int): CharacterStats {
        return copy(currentHP = maxOf(0, currentHP - damage))
    }

    /**
     * Restore HP and return updated stats
     */
    fun heal(amount: Int): CharacterStats {
        return copy(currentHP = minOf(maxHP, currentHP + amount))
    }

    /**
     * Use MP and return updated stats
     */
    fun useMagic(cost: Int): CharacterStats {
        return copy(currentMP = maxOf(0, currentMP - cost))
    }

    /**
     * Restore MP and return updated stats
     */
    fun restoreMagic(amount: Int): CharacterStats {
        return copy(currentMP = minOf(maxMP, currentMP + amount))
    }

    /**
     * Fully restore HP and MP (for level-up)
     */
    fun fullRestore(): CharacterStats {
        return copy(currentHP = maxHP, currentMP = maxMP)
    }

    /**
     * Add experience and return updated stats
     * Prevents integer overflow
     */
    fun gainExperience(amount: Int): CharacterStats {
        val newXP = (experience.toLong() + amount.toLong()).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        return copy(experience = maxOf(0, newXP))
    }

    /**
     * Add gold and return updated stats
     * Prevents integer overflow
     */
    fun gainGold(amount: Int): CharacterStats {
        val newGold = (gold.toLong() + amount.toLong()).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
        return copy(gold = maxOf(0, newGold))
    }

    /**
     * Spend gold and return updated stats
     */
    fun spendGold(amount: Int): CharacterStats {
        return if (gold >= amount) {
            copy(gold = maxOf(0, gold - amount))
        } else {
            this
        }
    }

    /**
     * Level up with stat increases
     * Based on CheckLevel procedure (ZARGON.BAS:836)
     */
    fun levelUp(apGain: Int, hpGain: Int, dpGain: Int, mpGain: Int): CharacterStats {
        return copy(
            level = level + 1,
            baseAP = baseAP + apGain,
            maxHP = maxHP + hpGain,
            baseDP = baseDP + dpGain,
            baseMP = baseMP + mpGain,
            currentHP = maxHP + hpGain,  // Full restore on level-up
            currentMP = baseMP + mpGain
        )
    }
}
