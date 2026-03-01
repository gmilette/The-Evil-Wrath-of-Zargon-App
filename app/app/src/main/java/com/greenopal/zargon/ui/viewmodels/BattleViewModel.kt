package com.greenopal.zargon.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.domain.battle.BattleAction
import com.greenopal.zargon.domain.battle.BattleResult
import com.greenopal.zargon.domain.battle.BattleState
import com.greenopal.zargon.domain.battle.BattleUseCase
import com.greenopal.zargon.domain.battle.MonsterSelector
import com.greenopal.zargon.domain.battle.Spells
import com.greenopal.zargon.domain.progression.BattleRewards
import com.greenopal.zargon.domain.progression.LevelingSystem
import com.greenopal.zargon.domain.progression.RewardSystem
import com.greenopal.zargon.domain.challenges.PrestigeSystem
import com.greenopal.zargon.domain.story.StoryProgressionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BattleViewModel @Inject constructor(
    private val monsterSelector: MonsterSelector,
    private val rewardSystem: RewardSystem,
    private val levelingSystem: LevelingSystem,
    private val battleUseCase: BattleUseCase,
    private val prestigeSystem: PrestigeSystem
) : ViewModel() {

    private val _battleState = MutableStateFlow<BattleState?>(null)
    val battleState: StateFlow<BattleState?> = _battleState.asStateFlow()

    private val _battleRewards = MutableStateFlow<BattleRewards?>(null)
    val battleRewards: StateFlow<BattleRewards?> = _battleRewards.asStateFlow()

    private var currentGameState: GameState? = null

    fun startBattle(gameState: GameState) {
        android.util.Log.d("BattleViewModel", "Starting battle - Character gold: ${gameState.character.gold}, XP: ${gameState.character.experience}")
        currentGameState = gameState
        val monster = monsterSelector.selectMonster(gameState)
        _battleState.value = BattleState(
            character = gameState.character,
            monster = monster,
            messages = listOf("A ${monster.name} appears!")
        )
        _battleRewards.value = null
    }

    private var isProcessingAction = false

    fun canUseMagic(): Boolean {
        return battleUseCase.canUseMagic(currentGameState?.challengeConfig)
    }

    fun onAction(action: BattleAction) {
        val state = _battleState.value ?: return

        if (state.battleResult != BattleResult.InProgress) return
        if (!state.character.isAlive) return
        if (action !is BattleAction.Magic && isProcessingAction) return

        when (action) {
            is BattleAction.Attack -> performAttack(state)
            is BattleAction.Magic -> {
                if (canUseMagic()) {
                    _battleState.value = state.copy(showMagicMenu = true)
                } else {
                    _battleState.value = state.addMessage("Magic is forbidden in this challenge!")
                }
            }
            is BattleAction.Run -> performRun(state)
            is BattleAction.CastSpell -> castSpell(state, action.spellIndex)
        }
    }

    private fun prestige(): PrestigeData = currentGameState?.prestigeData ?: PrestigeData()
    private fun config() = currentGameState?.challengeConfig

    private fun performAttack(state: BattleState) {
        viewModelScope.launch {
            isProcessingAction = true
            try {
                var newState = battleUseCase.executePlayerAttack(state, config(), prestige())
                _battleState.value = newState

                if (newState.battleResult == BattleResult.Victory) {
                    newState = newState.addMessage("${state.monster.name} is defeated!")
                    _battleState.value = newState
                    delay(500)
                    calculateVictoryRewards(newState)
                    _battleState.value?.let { _battleState.value = it.checkBattleEnd() }
                } else if (newState.battleResult == BattleResult.InProgress) {
                    delay(500)
                    newState = battleUseCase.executeMonsterCounterattack(newState, config(), prestige())
                    _battleState.value = newState
                }
            } finally {
                isProcessingAction = false
            }
        }
    }

    private fun performRun(state: BattleState) {
        viewModelScope.launch {
            isProcessingAction = true
            try {
                val newState = battleUseCase.attemptRun(state)
                _battleState.value = newState

                if (newState.battleResult == BattleResult.InProgress) {
                    delay(500)
                    _battleState.value = battleUseCase.executeMonsterCounterattack(newState, config(), prestige())
                }
            } finally {
                isProcessingAction = false
            }
        }
    }

    private fun castSpell(state: BattleState, spellIndex: Int) {
        viewModelScope.launch {
            isProcessingAction = true
            try {
                val spell = Spells.getByIndex(spellIndex)

                if (spell == null) {
                    _battleState.value = state.copy(showMagicMenu = false).addMessage("Invalid spell!")
                    return@launch
                }

                if (spell !in Spells.getAvailableSpells(state.character.level)) {
                    _battleState.value = state.addMessage("Spell not yet learned!")
                    return@launch
                }

                if (!spell.canCast(state.character.currentMP)) {
                    _battleState.value = state.addMessage("Not enough MP!")
                    return@launch
                }

                var newState = battleUseCase.executeSpell(
                    state.copy(showMagicMenu = false), spell, config(), prestige()
                )
                _battleState.value = newState

                if (newState.battleResult == BattleResult.Victory) {
                    newState = newState.addMessage("${state.monster.name} is defeated!")
                    _battleState.value = newState
                    delay(500)
                    calculateVictoryRewards(newState)
                    _battleState.value?.let { _battleState.value = it.checkBattleEnd() }
                } else if (newState.battleResult == BattleResult.InProgress) {
                    delay(500)
                    _battleState.value = battleUseCase.executeMonsterCounterattack(newState, config(), prestige())
                }
            } finally {
                isProcessingAction = false
            }
        }
    }

    fun closeMagicMenu() {
        val state = _battleState.value ?: return
        _battleState.value = state.copy(showMagicMenu = false)
    }

    fun endBattle() {
        _battleState.value = null
    }

    fun getUpdatedCharacter(): CharacterStats? {
        return _battleState.value?.character
    }

    private fun calculateVictoryRewards(state: BattleState) {
        val gameState = currentGameState ?: return

        if (!state.character.isAlive) {
            android.util.Log.w("BattleViewModel", "calculateVictoryRewards called with dead character - skipping rewards")
            return
        }

        val prestige = prestige()
        val monster = state.monster

        val baseXP = rewardSystem.calculateXP(monster.type, monster.scalingFactor)
        val baseGold = rewardSystem.calculateGold(monster.type, monster.scalingFactor)

        val xpGained = (baseXP * prestigeSystem.getXPMultiplier(prestige)).toInt()
        val goldGained = (baseGold * prestigeSystem.getGoldMultiplier(prestige)).toInt()

        val itemDropped = rewardSystem.getSpecialDrop(
            monster.type,
            gameState.worldX,
            gameState.worldY,
            gameState.storyStatus
        )

        var updatedCharacter = state.character
            .gainExperience(xpGained)
            .gainGold(goldGained)

        val (leveledCharacter, didLevelUp) = levelingSystem.checkAndApplyLevelUp(
            updatedCharacter,
            gameState.nextLevelXP
        )

        val rewards = if (didLevelUp) {
            updatedCharacter = leveledCharacter
            val apGain = leveledCharacter.baseAP - state.character.baseAP
            val hpGain = leveledCharacter.maxHP - state.character.maxHP
            val dpGain = leveledCharacter.baseDP - state.character.baseDP
            val mpGain = leveledCharacter.baseMP - state.character.baseMP

            BattleRewards(
                xpGained = xpGained,
                goldGained = goldGained,
                itemDropped = itemDropped,
                leveledUp = true,
                newLevel = leveledCharacter.level,
                apGain = apGain,
                hpGain = hpGain,
                dpGain = dpGain,
                mpGain = mpGain
            )
        } else {
            BattleRewards(
                xpGained = xpGained,
                goldGained = goldGained,
                itemDropped = itemDropped
            )
        }

        _battleState.value = state.updateCharacter(updatedCharacter)
        _battleRewards.value = rewards
    }

    fun getUpdatedGameState(): GameState? {
        val gameState = currentGameState ?: return null
        val battleState = _battleState.value ?: return null
        val character = battleState.character
        val rewards = _battleRewards.value
        val isVictory = battleState.battleResult == BattleResult.Victory

        if (!isVictory) {
            return gameState
        }

        var updatedState = gameState.updateCharacter(character)

        rewards?.itemDropped?.let { item ->
            updatedState = updatedState.addItem(item)
            updatedState = StoryProgressionChecker.checkAndAdvanceStory(updatedState)
        }

        if (rewards?.leveledUp == true) {
            val newNextLevelXP = rewardSystem.calculateXPForNextLevel(
                character.level,
                gameState.nextLevelXP
            )
            updatedState = updatedState.copy(nextLevelXP = newNextLevelXP)
        }

        return updatedState
    }
}
