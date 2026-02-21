package com.greenopal.zargon.domain.challenges

import com.greenopal.zargon.data.models.Challenge
import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.PrestigeBonus
import com.greenopal.zargon.data.models.PrestigeData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeModifiers @Inject constructor() {

    fun applyToMonster(
        baseStats: MonsterStats,
        config: ChallengeConfig?
    ): MonsterStats {
        if (config == null) return baseStats

        val challenges = config.challenges
        val multiplier = when {
            Challenge.STRONGER_ENEMIES in challenges || Challenge.WARRIOR_MODE in challenges -> 2.5f
            Challenge.STRONG_ENEMIES in challenges || Challenge.IMPOSSIBLE_MISSION in challenges -> 2.0f
            else -> return baseStats
        }

        return baseStats.copy(
            attackPower = (baseStats.attackPower * multiplier).toInt(),
            currentHP = (baseStats.currentHP * multiplier).toInt(),
            maxHP = (baseStats.maxHP * multiplier).toInt()
        )
    }

    fun getEffectiveWeaponBonus(
        baseBonus: Int,
        config: ChallengeConfig?,
        prestige: PrestigeData?
    ): Int {
        var multiplier = 1.0f
        if (config != null) {
            val challenges = config.challenges
            if (Challenge.WEAK_WEAPONS in challenges || Challenge.MAGE_QUEST in challenges) multiplier *= 0.5f
            if (Challenge.WARRIOR_MODE in challenges) multiplier *= 1.25f
        }
        if (prestige?.isBonusActive(PrestigeBonus.GREAT_WEAPONS) == true) multiplier *= 2.5f
        return (baseBonus * multiplier).toInt()
    }

    fun getEffectiveArmorBonus(
        baseBonus: Int,
        config: ChallengeConfig?,
        prestige: PrestigeData?
    ): Int {
        var multiplier = 1.0f
        if (config != null) {
            val challenges = config.challenges
            if (Challenge.WEAK_ARMOR in challenges || Challenge.MAGE_QUEST in challenges) multiplier *= 0.5f
        }
        if (prestige?.isBonusActive(PrestigeBonus.GREATER_ARMOR) == true) multiplier *= 2.0f
        return (baseBonus * multiplier).toInt()
    }

    fun getSpellEffectMultiplier(prestige: PrestigeData?): Float {
        return if (prestige?.isBonusActive(PrestigeBonus.MASTER_SPELLBOOK) == true) 1.5f else 1.0f
    }

    fun canUseMagic(config: ChallengeConfig?): Boolean {
        if (config == null) return true
        return !config.isNoMagic
    }

    fun isPermadeath(config: ChallengeConfig?): Boolean {
        if (config == null) return false
        return config.isPermadeath
    }

    fun getWeaponDisplayName(
        baseName: String,
        config: ChallengeConfig?,
        prestige: PrestigeData?
    ): String {
        val challenges = config?.challenges ?: emptySet()
        val hasWeakWeapons = Challenge.WEAK_WEAPONS in challenges || Challenge.MAGE_QUEST in challenges
        val hasGreatBonus = Challenge.WARRIOR_MODE in challenges
        val hasPrestigeGreat = prestige?.isBonusActive(PrestigeBonus.GREAT_WEAPONS) == true

        return when {
            hasWeakWeapons && !hasGreatBonus && !hasPrestigeGreat -> "Lesser $baseName"
            hasGreatBonus || hasPrestigeGreat -> "Great $baseName"
            else -> baseName
        }
    }

    fun getArmorDisplayName(
        baseName: String,
        config: ChallengeConfig?,
        prestige: PrestigeData?
    ): String {
        val challenges = config?.challenges ?: emptySet()
        val hasWeakArmor = Challenge.WEAK_ARMOR in challenges || Challenge.MAGE_QUEST in challenges
        val hasPrestigeGreater = prestige?.isBonusActive(PrestigeBonus.GREATER_ARMOR) == true

        return when {
            hasWeakArmor && !hasPrestigeGreater -> "Lesser $baseName"
            hasPrestigeGreater -> "Greater $baseName"
            else -> baseName
        }
    }

    fun getMonsterDisplayName(
        baseName: String,
        config: ChallengeConfig?
    ): String {
        if (config == null) return baseName
        val challenges = config.challenges
        return when {
            Challenge.STRONGER_ENEMIES in challenges || Challenge.WARRIOR_MODE in challenges -> "Massive $baseName"
            Challenge.STRONG_ENEMIES in challenges || Challenge.IMPOSSIBLE_MISSION in challenges -> "Huge $baseName"
            else -> baseName
        }
    }
}
