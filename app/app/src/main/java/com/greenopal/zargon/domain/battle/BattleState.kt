package com.greenopal.zargon.domain.battle

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.MonsterStats

/**
 * Represents the current state of a battle
 */
data class BattleState(
    val character: CharacterStats,
    val monster: MonsterStats,
    val messages: List<String> = emptyList(),
    val isPlayerTurn: Boolean = true,
    val battleResult: BattleResult = BattleResult.InProgress,
    val showMagicMenu: Boolean = false
) {
    /**
     * Add a message to the battle log
     */
    fun addMessage(message: String): BattleState {
        val newMessages = (messages + message).takeLast(5) // Keep last 5 messages
        return copy(messages = newMessages)
    }

    /**
     * Update character stats
     */
    fun updateCharacter(newStats: CharacterStats): BattleState {
        return copy(character = newStats)
    }

    /**
     * Update monster stats
     */
    fun updateMonster(newStats: MonsterStats): BattleState {
        return copy(monster = newStats)
    }

    /**
     * Check if battle is over
     * IMPORTANT: Character death takes priority over monster death to prevent
     * the bug where player can "win and die" simultaneously.
     * If Joe dies, it's always a Defeat - no rewards should be given.
     */
    fun checkBattleEnd(): BattleState {
        return when {
            !character.isAlive -> copy(battleResult = BattleResult.Defeat)
            !monster.isAlive -> copy(battleResult = BattleResult.Victory)
            else -> this
        }
    }
}

/**
 * Battle outcome
 */
sealed class BattleResult {
    object InProgress : BattleResult()
    object Victory : BattleResult()
    object Defeat : BattleResult()
    object Fled : BattleResult()
}

/**
 * Battle actions player can take
 */
sealed class BattleAction {
    object Attack : BattleAction()
    object Magic : BattleAction()
    object Run : BattleAction()
    data class CastSpell(val spellIndex: Int) : BattleAction()
}
