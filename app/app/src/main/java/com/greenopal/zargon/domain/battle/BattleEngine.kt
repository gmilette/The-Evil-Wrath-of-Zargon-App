package com.greenopal.zargon.domain.battle

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class BattleEngine @Inject constructor() {

    fun calculatePlayerDamage(character: CharacterStats, effectiveWeaponBonus: Int): Int {
        return character.baseAP + effectiveWeaponBonus
    }

    fun calculateMonsterDamage(
        monster: MonsterStats,
        character: CharacterStats,
        effectiveArmorBonus: Int,
        random: Random = Random
    ): Int {
        val randomMultiplier = 0.84 + random.nextDouble() * 0.32
        val totalDefense = character.baseDP + effectiveArmorBonus
        val rawDamage = monster.attackPower.toDouble() * DAMAGE_K /
            (totalDefense + DAMAGE_K) * randomMultiplier
        return maxOf(1, rawDamage.toInt())
    }

    companion object {
        private const val DAMAGE_K = 20.0
    }
}
