package com.greenopal.zargon.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.domain.battle.BattleAction
import com.greenopal.zargon.domain.battle.BattleEngine
import com.greenopal.zargon.domain.battle.BattleResult
import com.greenopal.zargon.domain.battle.BattleState
import com.greenopal.zargon.domain.battle.MonsterSelector
import com.greenopal.zargon.domain.progression.BattleRewards
import com.greenopal.zargon.domain.progression.LevelingSystem
import com.greenopal.zargon.domain.progression.RewardSystem
import com.greenopal.zargon.domain.story.StoryProgressionChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class BattleViewModel @Inject constructor(
    private val monsterSelector: MonsterSelector,
    private val rewardSystem: RewardSystem,
    private val levelingSystem: LevelingSystem,
    private val battleEngine: BattleEngine,
    private val challengeModifiers: com.greenopal.zargon.domain.challenges.ChallengeModifiers,
    private val prestigeRepository: com.greenopal.zargon.data.repository.PrestigeRepository,
    private val prestigeSystem: com.greenopal.zargon.domain.challenges.PrestigeSystem
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
        val gameState = currentGameState ?: return true
        return challengeModifiers.canUseMagic(gameState.challengeConfig)
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
                    showMagicMenu(state)
                } else {
                    _battleState.value = state.addMessage("Magic is forbidden in this challenge!")
                }
            }
            is BattleAction.Run -> attemptRun(state)
            is BattleAction.CastSpell -> castSpell(state, action.spellIndex)
        }
    }

    private fun performAttack(state: BattleState) {
        viewModelScope.launch {
            isProcessingAction = true
            try {
                val gameState = currentGameState
                val prestige = prestigeRepository.loadPrestige()

                val effectiveWeaponBonus = challengeModifiers.getEffectiveWeaponBonus(
                    state.character.weaponBonus,
                    gameState?.challengeConfig,
                    prestige
                )

                val damage = battleEngine.calculatePlayerDamage(state.character, effectiveWeaponBonus)

                val newMonster = state.monster.takeDamage(damage)

                var newState = state
                    .updateMonster(newMonster)
                    .addMessage("You hit ${state.monster.name} for $damage damage!")

                if (!newMonster.isAlive) {
                    newState = newState.addMessage("${state.monster.name} is defeated!")
                    _battleState.value = newState

                    delay(500)
                    calculateVictoryRewards(newState)

                    val updatedState = _battleState.value
                    if (updatedState != null) {
                        newState = updatedState.checkBattleEnd()
                        _battleState.value = newState
                    }
                    return@launch
                }

                _battleState.value = newState

                delay(500)
                monsterCounterattack(newState)
            } finally {
                isProcessingAction = false
            }
        }
    }

    private suspend fun monsterCounterattack(state: BattleState) {
        val gameState = currentGameState
        val prestige = prestigeRepository.loadPrestige()

        val effectiveArmorBonus = challengeModifiers.getEffectiveArmorBonus(
            state.character.armorBonus,
            gameState?.challengeConfig,
            prestige
        )

        val damage = battleEngine.calculateMonsterDamage(
            state.monster, state.character, effectiveArmorBonus
        )

        val newCharacter = state.character.takeDamage(damage)

        var newState = state
            .updateCharacter(newCharacter)
            .addMessage("${state.monster.name} hits you for $damage damage!")

        newState = newState.checkBattleEnd()
        _battleState.value = newState
    }

    private fun attemptRun(state: BattleState) {
        viewModelScope.launch {
            isProcessingAction = true
            try {
                val escaped = Random.nextInt(1, 5) == 1

                if (escaped) {
                    val newState = state
                        .addMessage("You run away!")
                        .copy(battleResult = BattleResult.Fled)
                    _battleState.value = newState
                } else {
                    val newState = state.addMessage("Can't escape!")
                    _battleState.value = newState

                    delay(500)
                    monsterCounterattack(newState)
                }
            } finally {
                isProcessingAction = false
            }
        }
    }

    private fun showMagicMenu(state: BattleState) {
        _battleState.value = state.copy(showMagicMenu = true)
    }

    private fun castSpell(state: BattleState, spellIndex: Int) {
        viewModelScope.launch {
            isProcessingAction = true
            try {
                val spell = com.greenopal.zargon.domain.battle.Spells.getByIndex(spellIndex)

                if (spell == null) {
                    val newState = state
                        .copy(showMagicMenu = false)
                        .addMessage("Invalid spell!")
                    _battleState.value = newState
                    return@launch
                }

                val availableSpells = com.greenopal.zargon.domain.battle.Spells.getAvailableSpells(state.character.level)
                if (spell !in availableSpells) {
                    val newState = state
                        .addMessage("Spell not yet learned!")
                    _battleState.value = newState
                    return@launch
                }

                if (!spell.canCast(state.character.currentMP)) {
                    val newState = state
                        .addMessage("Not enough MP!")
                    _battleState.value = newState
                    return@launch
                }

                var newState = state.copy(showMagicMenu = false)

                val newCharacter = state.character.useMagic(spell.mpCost)
                newState = newState.updateCharacter(newCharacter)

                val prestige = prestigeRepository.loadPrestige()
                val spellMultiplier = challengeModifiers.getSpellEffectMultiplier(prestige)

                if (spell.isHealing) {
                    val healAmount = spell.calculateEffect(state.character.level, spellMultiplier)
                    val healedCharacter = newCharacter.heal(healAmount)
                    newState = newState
                        .updateCharacter(healedCharacter)
                        .addMessage("${spell.name} restores $healAmount HP!")

                    _battleState.value = newState

                    delay(500)
                    monsterCounterattack(newState)
                } else {
                    val damage = spell.calculateEffect(state.character.level, spellMultiplier)
                    val damagedMonster = state.monster.takeDamage(damage)

                    newState = newState
                        .updateMonster(damagedMonster)
                        .addMessage("${spell.name} deals $damage damage!")

                    if (!damagedMonster.isAlive) {
                        newState = newState
                            .addMessage("${state.monster.name} is defeated!")
                        _battleState.value = newState

                        delay(500)
                        calculateVictoryRewards(newState)

                        val updatedState = _battleState.value
                        if (updatedState != null) {
                            newState = updatedState.checkBattleEnd()
                            _battleState.value = newState
                        }
                        return@launch
                    }

                    _battleState.value = newState

                    delay(500)
                    monsterCounterattack(newState)
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

        val monster = state.monster
        val prestige = prestigeRepository.loadPrestige()

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

        val newState = state.updateCharacter(updatedCharacter)
        _battleState.value = newState
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
