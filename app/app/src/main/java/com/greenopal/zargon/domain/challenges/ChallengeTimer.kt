package com.greenopal.zargon.domain.challenges

import com.greenopal.zargon.data.models.GameState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeTimer @Inject constructor() {

    fun getRemainingTimeMs(gameState: GameState): Long? {
        return null
    }

    fun isTimeExpired(gameState: GameState): Boolean {
        return false
    }

    fun formatRemainingTime(gameState: GameState): String? {
        return null
    }

    fun getElapsedTimeMs(gameState: GameState): Long {
        val startTime = gameState.challengeStartTime ?: return gameState.playtime
        return System.currentTimeMillis() - startTime - gameState.totalPauseTime
    }
}
