package com.greenopal.zargon.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.domain.map.GameMap
import com.greenopal.zargon.domain.map.MapParser
import com.greenopal.zargon.ui.screens.Direction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/**
 * ViewModel for map exploration
 * Handles movement, collision detection, and random encounters
 */
@HiltViewModel
class MapViewModel @Inject constructor(
    private val mapParser: MapParser
) : ViewModel() {

    private val _currentMap = MutableStateFlow<GameMap?>(null)
    val currentMap: StateFlow<GameMap?> = _currentMap.asStateFlow()

    private val _gameState = MutableStateFlow<GameState?>(null)
    val gameState: StateFlow<GameState?> = _gameState.asStateFlow()

    private var pendingEncounter = false

    /**
     * Load a map by world coordinates
     */
    fun loadMap(worldX: Int, worldY: Int) {
        viewModelScope.launch {
            val map = mapParser.parseMap(worldX, worldY)
            _currentMap.value = map
        }
    }

    /**
     * Set the current game state
     */
    fun setGameState(state: GameState) {
        _gameState.value = state
    }

    /**
     * Move player in a direction
     * Based on QBASIC movement (ZARGON.BAS:1900-1950)
     */
    fun movePlayer(direction: Direction) {
        val state = _gameState.value ?: return
        val map = _currentMap.value ?: return

        // Calculate new position
        val (newX, newY) = when (direction) {
            Direction.UP -> Pair(state.characterX, state.characterY - 1)
            Direction.DOWN -> Pair(state.characterX, state.characterY + 1)
            Direction.LEFT -> Pair(state.characterX - 1, state.characterY)
            Direction.RIGHT -> Pair(state.characterX + 1, state.characterY)
        }

        // Check bounds
        if (newX < 0 || newX >= map.width || newY < 0 || newY >= map.height) {
            // Hit edge - in QBASIC this would trigger map transition
            handleMapTransition(direction, state)
            return
        }

        // Check if tile is walkable
        if (!map.isWalkable(newX, newY)) {
            // Hit obstacle - do nothing
            return
        }

        // Move player
        val newState = state.moveTo(newX, newY)
        _gameState.value = newState

        // Set pending encounter flag (will be checked by screen)
        val encounterRate = map.getEncounterRate(newX, newY)
        if (encounterRate > 0f && Random.nextFloat() < encounterRate) {
            pendingEncounter = true
        }
    }

    /**
     * Check for random encounter after movement
     * Returns updated GameState if encounter triggered, null otherwise
     */
    fun checkForEncounter(state: GameState): GameState? {
        if (pendingEncounter) {
            pendingEncounter = false
            return state // Return state to trigger battle
        }
        return null
    }

    /**
     * Handle map transition when player reaches edge
     * Based on QBASIC map system (mapx, mapy coordinates)
     */
    private fun handleMapTransition(direction: Direction, state: GameState) {
        val map = _currentMap.value ?: return

        // Calculate new world coordinates
        val (newWorldX, newWorldY, newCharX, newCharY) = when (direction) {
            Direction.UP -> {
                if (state.worldY > 1) {
                    // Move to map above
                    Pair(state.worldX, state.worldY - 1) to Pair(state.characterX, map.height - 1)
                } else {
                    return // Can't go further up
                }
            }
            Direction.DOWN -> {
                if (state.worldY < 4) {
                    // Move to map below
                    Pair(state.worldX, state.worldY + 1) to Pair(state.characterX, 0)
                } else {
                    return // Can't go further down
                }
            }
            Direction.LEFT -> {
                if (state.worldX > 1) {
                    // Move to map on left
                    Pair(state.worldX - 1, state.worldY) to Pair(map.width - 1, state.characterY)
                } else {
                    return // Can't go further left
                }
            }
            Direction.RIGHT -> {
                if (state.worldX < 4) {
                    // Move to map on right
                    Pair(state.worldX + 1, state.worldY) to Pair(0, state.characterY)
                } else {
                    return // Can't go further right
                }
            }
        }

        val (newWorldPos, newCharPos) = when (direction) {
            Direction.UP -> Pair(Pair(state.worldX, state.worldY - 1), Pair(state.characterX, map.height - 1))
            Direction.DOWN -> Pair(Pair(state.worldX, state.worldY + 1), Pair(state.characterX, 0))
            Direction.LEFT -> Pair(Pair(state.worldX - 1, state.worldY), Pair(map.width - 1, state.characterY))
            Direction.RIGHT -> Pair(Pair(state.worldX + 1, state.worldY), Pair(0, state.characterY))
        }

        // Update game state with new map and position
        val newState = state.changeMap(
            worldX = newWorldPos.first,
            worldY = newWorldPos.second,
            spawnX = newCharPos.first,
            spawnY = newCharPos.second
        )

        _gameState.value = newState

        // Load new map
        loadMap(newWorldPos.first, newWorldPos.second)
    }

    /**
     * Get the updated game state (for external consumers)
     */
    fun getUpdatedGameState(): GameState? {
        return _gameState.value
    }
}
