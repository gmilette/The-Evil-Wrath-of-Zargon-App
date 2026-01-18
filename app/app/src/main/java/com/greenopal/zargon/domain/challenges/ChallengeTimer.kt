package com.greenopal.zargon.domain.challenges

import com.greenopal.zargon.data.models.GameState
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChallengeTimer @Inject constructor() {

    fun getRemainingTimeMs(gameState: GameState): Long? {
        val config = gameState.challengeConfig ?: return null
        val durationMs = config.timedChallenge.durationMinutes?.let { it * 60 * 1000L }
            ?: return null
        val startTime = gameState.challengeStartTime ?: return null

        val elapsedMs = System.currentTimeMillis() - startTime - gameState.totalPauseTime
        val remainingMs = durationMs - elapsedMs

        return maxOf(0L, remainingMs)
    }

    fun isTimeExpired(gameState: GameState): Boolean {
        val remaining = getRemainingTimeMs(gameState) ?: return false
        return remaining <= 0
    }

    fun formatRemainingTime(gameState: GameState): String? {
        val remainingMs = getRemainingTimeMs(gameState) ?: return null
        val totalSeconds = remainingMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "%02d:%02d".format(minutes, seconds)
    }

    fun getElapsedTimeMs(gameState: GameState): Long {
        val startTime = gameState.challengeStartTime ?: return gameState.playtime
        return System.currentTimeMillis() - startTime - gameState.totalPauseTime
    }
}
