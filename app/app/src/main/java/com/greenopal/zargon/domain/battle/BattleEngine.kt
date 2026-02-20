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
        val randomFactor = if (monster.scalingFactor > 0) {
            random.nextInt(0, monster.scalingFactor + 1)
        } else {
            0
        }
        val totalDefense = character.baseDP + effectiveArmorBonus
        val rawDamage = monster.attackPower - totalDefense + randomFactor
        return maxOf(1, rawDamage)
    }
}
