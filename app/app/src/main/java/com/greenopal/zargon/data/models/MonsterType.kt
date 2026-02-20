package com.greenopal.zargon.data.models

/**
 * Monster types from the original QBASIC game.
 * Based on SelectMonsta procedure (ZARGON.BAS:3083)
 * Note: XP and gold rewards are calculated in RewardSystem based on monster type and scaling factor
 */
enum class MonsterType(
    val displayName: String,
    val baseAP: Int,  // Base attack power
    val baseDP: Int,  // Base defense/HP
    val minLevel: Int // Minimum player level to encounter
) {
    SLIME("Slime", baseAP = 1, baseDP = 6, minLevel = 1),
    BAT("Bat", baseAP = 3, baseDP = 12, minLevel = 1),
    BABBLE("Babble", baseAP = 5, baseDP = 15, minLevel = 1),
    SPOOK("Spook", baseAP = 7, baseDP = 18, minLevel = 1),
    BELETH("Beleth", baseAP = 10, baseDP = 20, minLevel = 2),
    SKANDER_SNAKE("SkanderSnake", baseAP = 15, baseDP = 25, minLevel = 5),
    NECRO("Necro", baseAP = 16, baseDP = 38, minLevel = 6),
    KRAKEN("Kraken", baseAP = 31, baseDP = 75, minLevel = 1),
    ZARGON("ZARGON", baseAP = 75, baseDP = 375, minLevel = 1);
}
