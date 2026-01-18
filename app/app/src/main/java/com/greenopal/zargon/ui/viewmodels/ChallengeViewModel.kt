package com.greenopal.zargon.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.DifficultyLevel
import com.greenopal.zargon.data.models.EquipmentMode
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.data.models.TimedChallenge
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

    fun setDifficulty(difficulty: DifficultyLevel) {
        _challengeConfig.value = _challengeConfig.value.copy(
            difficulty = difficulty,
            presetName = null
        )
    }

    fun setWeaponMode(mode: EquipmentMode) {
        _challengeConfig.value = _challengeConfig.value.copy(
            weaponMode = mode,
            presetName = null
        )
    }

    fun setArmorMode(mode: EquipmentMode) {
        _challengeConfig.value = _challengeConfig.value.copy(
            armorMode = mode,
            presetName = null
        )
    }

    fun setPermanentDeath(enabled: Boolean) {
        _challengeConfig.value = _challengeConfig.value.copy(
            permanentDeath = enabled,
            presetName = null
        )
    }

    fun setTimedChallenge(timed: TimedChallenge) {
        _challengeConfig.value = _challengeConfig.value.copy(
            timedChallenge = timed,
            presetName = null
        )
    }

    fun applyPreset(preset: ChallengeConfig) {
        _challengeConfig.value = preset
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
            challengeConfig = config,
            challengeStartTime = if (config.timedChallenge != TimedChallenge.NONE) {
                System.currentTimeMillis()
            } else null
        )
    }

    fun isChallengeCompleted(challengeId: String): Boolean {
        return challengeId in _prestigeData.value.completedChallenges
    }
}
