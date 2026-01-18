package com.greenopal.zargon.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.domain.challenges.ChallengeTimer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Main game state ViewModel
 */
@HiltViewModel
class GameViewModel @Inject constructor(
    private val challengeTimer: ChallengeTimer
) : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _timerExpired = MutableStateFlow(false)
    val timerExpired: StateFlow<Boolean> = _timerExpired.asStateFlow()

    /**
     * Initialize new game
     */
    fun newGame(saveSlot: Int = 1, challengeConfig: ChallengeConfig? = null) {
        val startTime = if (challengeConfig?.timedChallenge?.durationMinutes != null) {
            System.currentTimeMillis()
        } else null

        _gameState.value = GameState(
            saveSlot = saveSlot,
            challengeConfig = challengeConfig,
            challengeStartTime = startTime
        )
        _timerExpired.value = false
    }

    /**
     * Update game state
     */
    fun updateGameState(newState: GameState) {
        _gameState.value = newState
    }

    /**
     * Update character stats
     */
    fun updateCharacterStats(updater: (GameState) -> GameState) {
        _gameState.value = updater(_gameState.value)
    }

    fun checkTimer() {
        if (challengeTimer.isTimeExpired(_gameState.value)) {
            _timerExpired.value = true
        }
    }

    fun getRemainingTimeFormatted(): String? {
        return challengeTimer.formatRemainingTime(_gameState.value)
    }

    fun incrementMonstersDefeated() {
        _gameState.value = _gameState.value.copy(
            monstersDefeated = _gameState.value.monstersDefeated + 1
        )
    }

    fun incrementDeathCount() {
        _gameState.value = _gameState.value.copy(
            deathCount = _gameState.value.deathCount + 1
        )
    }

    fun canSave(): Boolean {
        return _gameState.value.challengeConfig?.permanentDeath != true
    }
}
