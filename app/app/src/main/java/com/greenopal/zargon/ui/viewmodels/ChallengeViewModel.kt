package com.greenopal.zargon.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.greenopal.zargon.data.models.Challenge
import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.ChallengeResult
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.PrestigeBonus
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.data.repository.PrestigeRepository
import com.greenopal.zargon.domain.challenges.PrestigeSystem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ChallengeViewModel @Inject constructor(
    private val prestigeRepository: PrestigeRepository,
    private val prestigeSystem: PrestigeSystem
) : ViewModel() {

    private val _challengeConfig = MutableStateFlow(ChallengeConfig())
    val challengeConfig: StateFlow<ChallengeConfig> = _challengeConfig.asStateFlow()

    private val _prestigeData = MutableStateFlow(PrestigeData())
    val prestigeData: StateFlow<PrestigeData> = _prestigeData.asStateFlow()

    init {
        loadPrestige()
    }

    private fun loadPrestige() {
        _prestigeData.value = prestigeRepository.loadPrestige()
    }

    fun selectChallenge(challenge: Challenge) {
        val current = _challengeConfig.value
        val newChallenges = if (challenge in current.challenges) {
            emptySet()
        } else {
            setOf(challenge)
        }
        _challengeConfig.value = current.copy(challenges = newChallenges)
    }

    fun applyPreset(preset: ChallengeConfig) {
        _challengeConfig.value = preset
    }

    fun togglePrestigeBonus(bonus: PrestigeBonus) {
        val current = _prestigeData.value
        if (bonus !in current.unlockedBonuses) return
        val newActive = if (bonus in current.activeBonuses) {
            current.activeBonuses - bonus
        } else {
            current.activeBonuses + bonus
        }
        val updated = current.copy(activeBonuses = newActive)
        _prestigeData.value = updated
        prestigeRepository.savePrestige(updated)
    }

    fun getStartingGameState(saveSlot: Int): GameState {
        val config = _challengeConfig.value
        val prestige = _prestigeData.value

        val baseCharacter = CharacterStats()
        val prestigeCharacter = prestigeSystem.applyPrestigeBonusesToCharacter(
            baseCharacter,
            prestige
        )

        return GameState(
            saveSlot = saveSlot,
            character = prestigeCharacter,
            challengeConfig = if (config.challenges.isNotEmpty()) config else null,
            prestigeData = prestige
        )
    }

    /**
     * Merge prestige loaded from a save file into the live state.
     * Takes the union of completed challenges so no progress is ever lost.
     */
    fun syncPrestige(fromSave: PrestigeData) {
        val current = _prestigeData.value
        val merged = current.copy(
            completedChallenges = current.completedChallenges + fromSave.completedChallenges,
            unlockedBonuses = current.unlockedBonuses + fromSave.unlockedBonuses,
            activeBonuses = current.activeBonuses + fromSave.activeBonuses,
            totalCompletions = maxOf(current.totalCompletions, fromSave.totalCompletions)
        )
        if (merged != current) {
            _prestigeData.value = merged
            prestigeRepository.savePrestige(merged)
        }
    }

    fun isChallengeCompleted(challengeId: String): Boolean {
        return challengeId in _prestigeData.value.completedChallenges
    }

    fun getRewardForChallenge(challenge: Challenge): PrestigeBonus? {
        return prestigeSystem.getRewardForConfig(ChallengeConfig(challenges = setOf(challenge)))
    }

    /** Returns the bonus that would be earned for completing this config, or null if already completed. */
    fun getEarnedBonusIfNew(config: ChallengeConfig): PrestigeBonus? {
        if (isChallengeCompleted(config.getChallengeId())) return null
        return prestigeSystem.getRewardForConfig(config)
    }

    /** Marks the challenge complete, persists prestige, and refreshes in-memory state. */
    fun completeChallenge(config: ChallengeConfig, result: ChallengeResult) {
        val newPrestige = prestigeSystem.calculatePrestigeRewards(config, _prestigeData.value)
        prestigeRepository.savePrestige(newPrestige)
        prestigeRepository.saveChallengeResult(result)
        _prestigeData.value = newPrestige
    }
}
