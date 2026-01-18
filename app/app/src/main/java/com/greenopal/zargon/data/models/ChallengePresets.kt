package com.greenopal.zargon.data.models

object ChallengePresets {
    val BEGINNER = ChallengeConfig(
        difficulty = DifficultyLevel.NORMAL,
        weaponMode = EquipmentMode.NORMAL,
        armorMode = EquipmentMode.NORMAL,
        permanentDeath = false,
        timedChallenge = TimedChallenge.NONE,
        presetName = "Beginner"
    )

    val EASY_BEATDOWN = ChallengeConfig(
        difficulty = DifficultyLevel.NORMAL,
        weaponMode = EquipmentMode.BEATDOWN,
        armorMode = EquipmentMode.BEATDOWN,
        permanentDeath = false,
        timedChallenge = TimedChallenge.NONE,
        presetName = "Easy Beatdown"
    )

    val GLASS_CANNON = ChallengeConfig(
        difficulty = DifficultyLevel.HARD,
        weaponMode = EquipmentMode.BEATDOWN,
        armorMode = EquipmentMode.NONE,
        permanentDeath = false,
        timedChallenge = TimedChallenge.NONE,
        presetName = "Glass Cannon"
    )

    val TANK_MODE = ChallengeConfig(
        difficulty = DifficultyLevel.NORMAL,
        weaponMode = EquipmentMode.NONE,
        armorMode = EquipmentMode.BEATDOWN,
        permanentDeath = false,
        timedChallenge = TimedChallenge.NONE,
        presetName = "Tank Mode"
    )

    val SPEEDRUN = ChallengeConfig(
        difficulty = DifficultyLevel.NORMAL,
        weaponMode = EquipmentMode.GREAT,
        armorMode = EquipmentMode.GREAT,
        permanentDeath = false,
        timedChallenge = TimedChallenge.FAST,
        presetName = "Speedrun"
    )

    val NIGHTMARE = ChallengeConfig(
        difficulty = DifficultyLevel.INSANE,
        weaponMode = EquipmentMode.NORMAL,
        armorMode = EquipmentMode.NORMAL,
        permanentDeath = true,
        timedChallenge = TimedChallenge.NONE,
        presetName = "Nightmare"
    )

    val TRUE_MASTERY = ChallengeConfig(
        difficulty = DifficultyLevel.INSANE,
        weaponMode = EquipmentMode.NONE,
        armorMode = EquipmentMode.NONE,
        permanentDeath = true,
        timedChallenge = TimedChallenge.QUICK,
        presetName = "True Mastery"
    )

    val ALL_PRESETS = listOf(
        BEGINNER,
        EASY_BEATDOWN,
        GLASS_CANNON,
        TANK_MODE,
        SPEEDRUN,
        NIGHTMARE,
        TRUE_MASTERY
    )
}
