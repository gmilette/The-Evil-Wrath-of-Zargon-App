package com.greenopal.zargon.simulation

import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.domain.battle.BattleEngine
import com.greenopal.zargon.domain.battle.BattleResult
import com.greenopal.zargon.domain.battle.BattleState
import com.greenopal.zargon.domain.battle.BattleUseCase
import com.greenopal.zargon.domain.challenges.ChallengeModifiers
import kotlin.random.Random

class BattleSimulator(
    private val random: Random = Random.Default
) {
    private val battleUseCase = BattleUseCase(BattleEngine(), ChallengeModifiers())

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
        val effectivePrestige = prestige ?: PrestigeData()
        var state = BattleState(
            character = character,
            monster = monster
        )
        var turns = 0
        var totalPlayerDamageDealt = 0
        var totalPlayerDamageTaken = 0

        val hpBefore = character.currentHP
        val monsterHPBefore = monster.currentHP

        while (state.battleResult == BattleResult.InProgress) {
            turns++

            val hpBeforeAttack = state.monster.currentHP
            state = battleUseCase.executePlayerAttack(state, config, effectivePrestige)
            totalPlayerDamageDealt += hpBeforeAttack - maxOf(0, state.monster.currentHP)

            if (state.battleResult != BattleResult.InProgress) break

            val playerHPBefore = state.character.currentHP
            state = battleUseCase.executeMonsterCounterattack(state, config, effectivePrestige)
            totalPlayerDamageTaken += playerHPBefore - maxOf(0, state.character.currentHP)
        }

        val outcome = when (state.battleResult) {
            is BattleResult.Victory -> BattleOutcome.PlayerVictory
            is BattleResult.Defeat -> BattleOutcome.PlayerDefeat
            else -> throw IllegalStateException("Battle ended without resolution: ${state.battleResult}")
        }

        return BattleLog(
            outcome = outcome,
            turnsElapsed = turns,
            playerDamageDealt = totalPlayerDamageDealt,
            playerDamageTaken = totalPlayerDamageTaken,
            playerHPRemaining = maxOf(0, state.character.currentHP),
            monsterHPRemaining = maxOf(0, state.monster.currentHP)
        )
    }
}
