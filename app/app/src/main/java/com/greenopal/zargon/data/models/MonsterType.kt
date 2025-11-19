package com.greenopal.zargon.data.models

/**
 * Monster types from the original QBASIC game.
 * Based on SelectMonsta procedure (ZARGON.BAS:3083)
 */
enum class MonsterType(
    val displayName: String,
    val baseAP: Int,  // Base attack power
    val baseDP: Int,  // Base defense/HP
    val minLevel: Int, // Minimum player level to encounter
    val baseExperience: Int,
    val baseGold: Int
) {
    SLIME("Slime", baseAP = 1, baseDP = 5, minLevel = 1, baseExperience = 5, baseGold = 3),
    BAT("Bat", baseAP = 2, baseDP = 10, minLevel = 1, baseExperience = 8, baseGold = 5),
    BABBLE("Babble", baseAP = 4, baseDP = 12, minLevel = 1, baseExperience = 12, baseGold = 10),
    SPOOK("Spook", baseAP = 6, baseDP = 14, minLevel = 1, baseExperience = 15, baseGold = 15),
    BELETH("Beleth", baseAP = 8, baseDP = 16, minLevel = 2, baseExperience = 20, baseGold = 20),
    SKANDER_SNAKE("SkanderSnake", baseAP = 12, baseDP = 20, minLevel = 5, baseExperience = 30, baseGold = 30),
    NECRO("Necro", baseAP = 13, baseDP = 30, minLevel = 6, baseExperience = 50, baseGold = 45),
    KRAKEN("Kraken", baseAP = 25, baseDP = 60, minLevel = 1, baseExperience = 100, baseGold = 100),
    ZARGON("ZARGON", baseAP = 60, baseDP = 300, minLevel = 1, baseExperience = 1000, baseGold = 0);
}
