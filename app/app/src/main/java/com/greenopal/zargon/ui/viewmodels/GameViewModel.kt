package com.greenopal.zargon.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.greenopal.zargon.data.models.GameState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

/**
 * Main game state ViewModel
 */
@HiltViewModel
class GameViewModel @Inject constructor() : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    /**
     * Initialize new game
     */
    fun newGame(saveSlot: Int = 1) {
        _gameState.value = GameState(saveSlot = saveSlot)
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
}
