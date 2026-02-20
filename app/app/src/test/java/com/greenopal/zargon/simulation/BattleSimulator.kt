package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.domain.battle.BattleEngine
import com.greenopal.zargon.domain.challenges.ChallengeModifiers
import kotlin.random.Random

class BattleSimulator(
    private val random: Random = Random.Default
) {
    private val challengeModifiers = ChallengeModifiers()
    private val battleEngine = BattleEngine()

    sealed class BattleOutcome {
        object PlayerVictory : BattleOutcome()
        object PlayerDefeat : BattleOutcome()
    }

    data class BattleLog(
        val outcome: BattleOutcome,
        val turnsElapsed: Int,
        val playerDamageDealt: Int,
        val playerDamageTaken: Int,
        val playerHPRemaining: Int,
        val monsterHPRemaining: Int
    )

    fun simulateBattle(
        character: CharacterStats,
        monster: MonsterStats,
        config: ChallengeConfig? = null,
        prestige: PrestigeData? = null
    ): BattleLog {
        var currentPlayerHP = character.currentDP
        var currentMonsterHP = monster.currentHP
        var turns = 0
        var totalPlayerDamageDealt = 0
        var totalPlayerDamageTaken = 0

        val effectiveWeaponBonus = challengeModifiers.getEffectiveWeaponBonus(
            character.weaponBonus, config, prestige
        )
        val effectiveArmorBonus = challengeModifiers.getEffectiveArmorBonus(
            character.armorBonus, config, prestige
        )

        while (currentPlayerHP > 0 && currentMonsterHP > 0) {
            turns++

            val playerDamage = battleEngine.calculatePlayerDamage(character, effectiveWeaponBonus)
            currentMonsterHP -= playerDamage
            totalPlayerDamageDealt += playerDamage

            if (currentMonsterHP <= 0) break

            val monsterDamage = battleEngine.calculateMonsterDamage(
                monster, character, effectiveArmorBonus, random
            )
            currentPlayerHP -= monsterDamage
            totalPlayerDamageTaken += monsterDamage
        }

        val outcome = when {
            currentPlayerHP <= 0 -> BattleOutcome.PlayerDefeat
            currentMonsterHP <= 0 -> BattleOutcome.PlayerVictory
            else -> throw IllegalStateException("Battle ended without resolution")
        }

        return BattleLog(
            outcome = outcome,
            turnsElapsed = turns,
            playerDamageDealt = totalPlayerDamageDealt,
            playerDamageTaken = totalPlayerDamageTaken,
            playerHPRemaining = maxOf(0, currentPlayerHP),
            monsterHPRemaining = maxOf(0, currentMonsterHP)
        )
    }
}
