package com.greenopal.zargon.data.models

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeConfig(
    val difficulty: DifficultyLevel = DifficultyLevel.NORMAL,
    val weaponMode: EquipmentMode = EquipmentMode.NORMAL,
    val armorMode: EquipmentMode = EquipmentMode.NORMAL,
    val permanentDeath: Boolean = false,
    val timedChallenge: TimedChallenge = TimedChallenge.NONE,
    val presetName: String? = null  // For tracking which preset was used
) {
    fun getChallengeId(): String {
        return "${difficulty.name}_${weaponMode.name}_${armorMode.name}_${permanentDeath}_${timedChallenge.name}"
    }

    fun getDisplayName(): String = presetName ?: "Custom Challenge"
}

@Serializable
enum class DifficultyLevel(
    val displayName: String,
    val monsterMultiplier: Float,
    val label: String?
) {
    NORMAL("Normal", 1f, null),
    HARD("Hard", 3f, "huge"),
    INSANE("Insane", 5f, "massive")
}

@Serializable
enum class EquipmentMode(
    val displayName: String,
    val weaponDisplayName: String,
    val armorDisplayName: String,
    val powerMultiplier: Float,
    val costMultiplier: Float,
    val enabled: Boolean = true
) {
    NONE("None", "No Weapons", "No Armor", 0f, 0f, enabled = false),
    NORMAL("Normal", "Normal Weapons", "Normal Armor", 1f, 1f),
    GREAT("Great", "Great Weapons", "Great Armor", 2f, 2f),
    BEATDOWN("Beatdown", "Beatdown Weapons", "Beatdown Armor", 10f, 10f)
}

@Serializable
enum class TimedChallenge(
    val displayName: String,
    val durationMinutes: Int?
) {
    NONE("No Time Limit", null),
    QUICK("Quick (10 min)", 10),
    FAST("Fast (30 min)", 30)
}
