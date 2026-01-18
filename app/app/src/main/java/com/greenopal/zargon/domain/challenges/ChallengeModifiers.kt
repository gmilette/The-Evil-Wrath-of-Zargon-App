package com.greenopal.zargon.domain.challenges

import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.DifficultyLevel
import com.greenopal.zargon.data.models.EquipmentMode
import com.greenopal.zargon.data.models.MonsterStats
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeModifiers @Inject constructor() {

    fun applyDifficultyToMonster(
        baseStats: MonsterStats,
        difficulty: DifficultyLevel
    ): MonsterStats {
        if (difficulty == DifficultyLevel.NORMAL) return baseStats

        val multiplier = difficulty.monsterMultiplier.toInt()
        return baseStats.copy(
            attackPower = (baseStats.attackPower * multiplier),
            currentHP = (baseStats.currentHP * multiplier),
            maxHP = (baseStats.maxHP * multiplier)
        )
    }

    fun getEffectiveWeaponBonus(
        baseBonus: Int,
        weaponMode: EquipmentMode
    ): Int {
        if (!weaponMode.enabled) return 0
        return (baseBonus * weaponMode.powerMultiplier).toInt()
    }

    fun getEffectiveArmorBonus(
        baseBonus: Int,
        armorMode: EquipmentMode
    ): Int {
        if (!armorMode.enabled) return 0
        return (baseBonus * armorMode.powerMultiplier).toInt()
    }

    fun getAdjustedPrice(
        basePrice: Int,
        equipmentMode: EquipmentMode
    ): Int {
        return (basePrice * equipmentMode.costMultiplier).toInt()
    }

    fun canPurchaseWeapons(config: ChallengeConfig?): Boolean {
        return config?.weaponMode?.enabled ?: true
    }

    fun canPurchaseArmor(config: ChallengeConfig?): Boolean {
        return config?.armorMode?.enabled ?: true
    }

    fun getDifficultyLabel(difficulty: DifficultyLevel): String? {
        return difficulty.label
    }
}
