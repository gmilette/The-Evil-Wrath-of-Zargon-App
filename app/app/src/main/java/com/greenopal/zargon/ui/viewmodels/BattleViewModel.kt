package com.greenopal.zargon.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.domain.battle.BattleAction
import com.greenopal.zargon.domain.battle.BattleResult
import com.greenopal.zargon.domain.battle.BattleState
import com.greenopal.zargon.domain.battle.MonsterSelector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for battle screen
 * Implements combat logic from ZARGON.BAS (battleset, HitBeast, Hitback procedures)
 */
@HiltViewModel
class BattleViewModel @Inject constructor(
    private val monsterSelector: MonsterSelector
) : ViewModel() {

    private val _battleState = MutableStateFlow<BattleState?>(null)
    val battleState: StateFlow<BattleState?> = _battleState.asStateFlow()

    /**
     * Start a new battle
     */
    fun startBattle(gameState: GameState) {
        val monster = monsterSelector.selectMonster(gameState)
        _battleState.value = BattleState(
            character = gameState.character,
            monster = monster,
            messages = listOf("A ${monster.name} appears!")
        )
    }

    /**
     * Handle player action
     */
    fun onAction(action: BattleAction) {
        val state = _battleState.value ?: return

        when (action) {
            is BattleAction.Attack -> performAttack(state)
            is BattleAction.Magic -> showMagicMenu(state)
            is BattleAction.Run -> attemptRun(state)
            is BattleAction.CastSpell -> castSpell(state, action.spellIndex)
        }
    }

    /**
     * Player attacks monster (HitBeast from ZARGON.BAS:1779)
     */
    private fun performAttack(state: BattleState) {
        viewModelScope.launch {
            // Calculate damage: cAP + wgain (weapon bonus)
            val damage = state.character.totalAP

            // Apply damage to monster
            val newMonster = state.monster.takeDamage(damage)

            // Add message
            var newState = state
                .updateMonster(newMonster)
                .addMessage("You hit ${state.monster.name} for $damage damage!")

            // Check if monster is defeated
            if (!newMonster.isAlive) {
                newState = newState.addMessage("${state.monster.name} is defeated!")
                    .checkBattleEnd()
                _battleState.value = newState
                return@launch
            }

            _battleState.value = newState

            // Monster counterattack after delay
            delay(500)
            monsterCounterattack(newState)
        }
    }

    /**
     * Monster attacks player (Hitback from ZARGON.BAS:1685)
     */
    private fun monsterCounterattack(state: BattleState) {
        viewModelScope.launch {
            // Calculate damage: mAP - defense + random(monster level equivalent)
            // In QBASIC: mloss = INT(RND * whatlev)
            val randomFactor = Random.nextInt(0, state.monster.scalingFactor + 1)
            val rawDamage = state.monster.attackPower - state.character.totalDefense + randomFactor
            val damage = maxOf(1, rawDamage) // Minimum 1 damage

            // Apply damage to character
            val newCharacter = state.character.takeDamage(damage)

            // Add message
            var newState = state
                .updateCharacter(newCharacter)
                .addMessage("${state.monster.name} hits you for $damage damage!")

            // Check if player is defeated
            newState = newState.checkBattleEnd()
            _battleState.value = newState
        }
    }

    /**
     * Attempt to run from battle (ZARGON.BAS:582-587)
     * Note: Fixed bug where QBASIC falls through to Hitback on successful flee
     */
    private fun attemptRun(state: BattleState) {
        viewModelScope.launch {
            // 25% chance to escape (QBASIC: Ch = INT(RND * 4) + 1; IF Ch = 1)
            val escaped = Random.nextInt(1, 5) == 1

            if (escaped) {
                val newState = state
                    .addMessage("You run away!")
                    .copy(battleResult = BattleResult.Fled)
                _battleState.value = newState
            } else {
                val newState = state.addMessage("Can't escape!")
                _battleState.value = newState

                // Monster gets free attack
                delay(500)
                monsterCounterattack(newState)
            }
        }
    }

    /**
     * Show magic menu (placeholder for now)
     */
    private fun showMagicMenu(state: BattleState) {
        _battleState.value = state.copy(showMagicMenu = true)
    }

    /**
     * Cast a spell (Magix from ZARGON.BAS:2515)
     * To be implemented in Phase 3
     */
    private fun castSpell(state: BattleState, spellIndex: Int) {
        // Placeholder - will implement in Phase 3, Step 7
        val newState = state
            .copy(showMagicMenu = false)
            .addMessage("Magic not yet implemented!")
        _battleState.value = newState
    }

    /**
     * Close magic menu
     */
    fun closeMagicMenu() {
        val state = _battleState.value ?: return
        _battleState.value = state.copy(showMagicMenu = false)
    }

    /**
     * Reset battle state
     */
    fun endBattle() {
        _battleState.value = null
    }

    /**
     * Get updated character stats (for saving back to game state)
     */
    fun getUpdatedCharacter(): CharacterStats? {
        return _battleState.value?.character
    }
}
