package com.greenopal.zargon.domain.battle

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class BattleEngine @Inject constructor() {

    fun calculatePlayerDamage(
        character: CharacterStats,
        effectiveWeaponBonus: Int,
        random: Random = Random
    ): Int {
        // More powerful weapons have a wider damage swing.
        // spread = weaponBonus / 5 (DAGGER±1, LONG_SWORD±2, ATLANTEAN±7)
        val spread = effectiveWeaponBonus / 5
        val roll = if (spread > 0) random.nextInt(-spread, spread + 1) else 0
        return maxOf(1, character.baseAP + effectiveWeaponBonus + roll)
    }

    fun calculateMonsterDamage(
        monster: MonsterStats,
        character: CharacterStats,
        effectiveArmorBonus: Int,
        random: Random = Random
    ): Int {
        // More powerful monsters have a wider damage spread.
        // spread grows from ±15% (weak) up to ±40% (boss-tier), driven by scaled attackPower.
        val spread = (0.05 + monster.attackPower / 1000.0).coerceAtMost(0.15)
        val randomMultiplier = (1.0 - spread) + random.nextDouble() * (2.0 * spread)
        val totalDefense = character.baseDP + effectiveArmorBonus
        val rawDamage = monster.attackPower.toDouble() * DAMAGE_K /
            (totalDefense + DAMAGE_K) * randomMultiplier
        return maxOf(1, rawDamage.toInt())
    }

    companion object {
        const val DAMAGE_K = 20.0
    }
}
