package com.greenopal.zargon.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.domain.battle.BattleAction
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

/**
 * ViewModel for battle screen
 * Implements combat logic from ZARGON.BAS (battleset, HitBeast, Hitback procedures)
 */
@HiltViewModel
class BattleViewModel @Inject constructor(
    private val monsterSelector: MonsterSelector,
    private val rewardSystem: RewardSystem,
    private val levelingSystem: LevelingSystem
) : ViewModel() {

    private val _battleState = MutableStateFlow<BattleState?>(null)
    val battleState: StateFlow<BattleState?> = _battleState.asStateFlow()

    private val _battleRewards = MutableStateFlow<BattleRewards?>(null)
    val battleRewards: StateFlow<BattleRewards?> = _battleRewards.asStateFlow()

    private var currentGameState: GameState? = null

    /**
     * Start a new battle
     */
    fun startBattle(gameState: GameState) {
        android.util.Log.d("BattleViewModel", "Starting battle - Character gold: ${gameState.character.gold}, XP: ${gameState.character.experience}")
        android.util.Log.d("BattleViewModel", "Battle position - World: (${gameState.worldX}, ${gameState.worldY}), Char: (${gameState.characterX}, ${gameState.characterY})")
        currentGameState = gameState
        val monster = monsterSelector.selectMonster(gameState)
        _battleState.value = BattleState(
            character = gameState.character,
            monster = monster,
            messages = listOf("A ${monster.name} appears!")
        )
        _battleRewards.value = null
    }

    // Flag to prevent concurrent action processing
    private var isProcessingAction = false

    /**
     * Handle player action
     */
    fun onAction(action: BattleAction) {
        val state = _battleState.value ?: return

        // Prevent concurrent actions (except showing magic menu)
        if (action !is BattleAction.Magic && isProcessingAction) {
            android.util.Log.w("BattleViewModel", "Ignoring action - already processing")
            return
        }

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
            isProcessingAction = true
            try {
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
                    android.util.Log.d("BattleViewModel", "Monster defeated! Starting reward calculation...")
                    newState = newState.addMessage("${state.monster.name} is defeated!")
                    _battleState.value = newState

                    // Calculate rewards
                    delay(500)
                    calculateVictoryRewards(newState)

                    // Get the UPDATED state (safe version)
                    val updatedState = _battleState.value
                    if (updatedState != null) {
                        newState = updatedState.checkBattleEnd()
                        android.util.Log.d("BattleViewModel", "Setting battle result to: ${newState.battleResult}, rewards: ${_battleRewards.value}")
                        _battleState.value = newState
                    }
                    return@launch
                }

                _battleState.value = newState

                // Monster counterattack after delay
                delay(500)
                monsterCounterattack(newState)
            } finally {
                isProcessingAction = false
            }
        }
    }

    /**
     * Monster attacks player (Hitback from ZARGON.BAS:1685)
     */
    private fun monsterCounterattack(state: BattleState) {
        viewModelScope.launch {
            // Calculate damage: mAP - defense + random(monster level equivalent)
            // In QBASIC: mloss = INT(RND * whatlev)
            // Fix: Handle scalingFactor == 0 to prevent crash
            val randomFactor = if (state.monster.scalingFactor > 0) {
                Random.nextInt(0, state.monster.scalingFactor + 1)
            } else {
                0
            }
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
            isProcessingAction = true
            try {
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
            } finally {
                isProcessingAction = false
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
     */
    private fun castSpell(state: BattleState, spellIndex: Int) {
        viewModelScope.launch {
            isProcessingAction = true
            try {
                // Get the spell (convert 1-based index to 0-based)
                val spell = com.greenopal.zargon.domain.battle.Spells.getByIndex(spellIndex)

                if (spell == null) {
                    // Invalid spell index
                    val newState = state
                        .copy(showMagicMenu = false)
                        .addMessage("Invalid spell!")
                    _battleState.value = newState
                    return@launch
                }

                // Check if player has unlocked this spell
                val availableSpells = com.greenopal.zargon.domain.battle.Spells.getAvailableSpells(state.character.level)
                if (spell !in availableSpells) {
                    val newState = state
                        .addMessage("Spell not yet learned!")
                    _battleState.value = newState
                    return@launch
                }

                // Check MP cost
                if (!spell.canCast(state.character.currentMP)) {
                    val newState = state
                        .addMessage("Not enough MP!")
                    _battleState.value = newState
                    return@launch
                }

                // Close magic menu
                var newState = state.copy(showMagicMenu = false)

                // Deduct MP cost
                val newCharacter = state.character.useMagic(spell.mpCost)
                newState = newState.updateCharacter(newCharacter)

                if (spell.isHealing) {
                    // Cure spell - heal player
                    val healAmount = spell.calculateEffect(state.character.level)
                    val healedCharacter = newCharacter.heal(healAmount)
                    newState = newState
                        .updateCharacter(healedCharacter)
                        .addMessage("${spell.name} restores $healAmount HP!")

                    _battleState.value = newState

                    // Monster counterattack after healing
                    delay(500)
                    monsterCounterattack(newState)
                } else {
                    // Damage spell - attack monster
                    val damage = spell.calculateEffect(state.character.level)
                    val damagedMonster = state.monster.takeDamage(damage)

                    newState = newState
                        .updateMonster(damagedMonster)
                        .addMessage("${spell.name} deals $damage damage!")

                    // Check if monster is defeated
                    if (!damagedMonster.isAlive) {
                        newState = newState
                            .addMessage("${state.monster.name} is defeated!")
                        _battleState.value = newState

                        // Calculate rewards
                        delay(500)
                        calculateVictoryRewards(newState)

                        // Get the UPDATED state (safe version)
                        val updatedState = _battleState.value
                        if (updatedState != null) {
                            newState = updatedState.checkBattleEnd()
                            _battleState.value = newState
                        }
                        return@launch
                    }

                    _battleState.value = newState

                    // Monster counterattack
                    delay(500)
                    monsterCounterattack(newState)
                }
            } finally {
                isProcessingAction = false
            }
        }
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

    /**
     * Calculate victory rewards (WinBattle from ZARGON.BAS:3658)
     */
    private fun calculateVictoryRewards(state: BattleState) {
        val gameState = currentGameState ?: return
        val monster = state.monster

        // Calculate XP and gold
        val xpGained = rewardSystem.calculateXP(monster.type, monster.scalingFactor)
        val goldGained = rewardSystem.calculateGold(monster.type, monster.scalingFactor)

        android.util.Log.d("BattleViewModel", "Rewards calculated - XP: $xpGained, Gold: $goldGained for ${monster.type}")

        // Check for special item drops
        val itemDropped = rewardSystem.getSpecialDrop(
            monster.type,
            gameState.worldX,
            gameState.worldY,
            gameState.storyStatus
        )

        // Update character with XP and gold
        var updatedCharacter = state.character
            .gainExperience(xpGained)
            .gainGold(goldGained)

        android.util.Log.d("BattleViewModel", "Gold before level check: ${updatedCharacter.gold}, XP: ${updatedCharacter.experience}")

        // Check for level up
        val (leveledCharacter, didLevelUp) = levelingSystem.checkAndApplyLevelUp(
            updatedCharacter,
            gameState.nextLevelXP
        )

        android.util.Log.d("BattleViewModel", "After level check - Gold: ${leveledCharacter.gold}, Did level up: $didLevelUp")

        val rewards = if (didLevelUp) {
            // Character leveled up!
            updatedCharacter = leveledCharacter
            val apGain = leveledCharacter.baseAP - state.character.baseAP
            val dpGain = leveledCharacter.baseDP - state.character.baseDP
            val mpGain = leveledCharacter.baseMP - state.character.baseMP

            BattleRewards(
                xpGained = xpGained,
                goldGained = goldGained,
                itemDropped = itemDropped,
                leveledUp = true,
                newLevel = leveledCharacter.level,
                apGain = apGain,
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

        // Update battle state with new character stats
        val newState = state.updateCharacter(updatedCharacter)
        android.util.Log.d("BattleViewModel", "Final character gold in battle state: ${newState.character.gold}")
        _battleState.value = newState
        _battleRewards.value = rewards
    }

    /**
     * Get updated game state with battle results
     */
    fun getUpdatedGameState(): GameState? {
        val gameState = currentGameState ?: return null
        val character = _battleState.value?.character ?: return null
        val rewards = _battleRewards.value

        android.util.Log.d("BattleViewModel", "getUpdatedGameState - Character gold: ${character.gold}, XP: ${character.experience}")
        android.util.Log.d("BattleViewModel", "getUpdatedGameState - Original position - World: (${gameState.worldX}, ${gameState.worldY}), Char: (${gameState.characterX}, ${gameState.characterY})")

        var updatedState = gameState.updateCharacter(character)

        android.util.Log.d("BattleViewModel", "getUpdatedGameState - Updated state character gold: ${updatedState.character.gold}")

        // Add item to inventory if dropped
        rewards?.itemDropped?.let { item ->
            updatedState = updatedState.addItem(item)
            android.util.Log.d("BattleViewModel", "Item added: ${item.name} - Checking story progression")

            // Check if story should auto-advance based on inventory
            updatedState = StoryProgressionChecker.checkAndAdvanceStory(updatedState)
        }

        // Update next level XP if leveled up
        if (rewards?.leveledUp == true) {
            val newNextLevelXP = rewardSystem.calculateXPForNextLevel(
                character.level,
                gameState.nextLevelXP
            )
            updatedState = updatedState.copy(nextLevelXP = newNextLevelXP)
        }

        android.util.Log.d("BattleViewModel", "getUpdatedGameState - Returning position - World: (${updatedState.worldX}, ${updatedState.worldY}), Char: (${updatedState.characterX}, ${updatedState.characterY})")

        return updatedState
    }
}
