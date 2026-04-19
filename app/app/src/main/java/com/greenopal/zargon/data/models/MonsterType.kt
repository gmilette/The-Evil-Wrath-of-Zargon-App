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
    SLIME("Slime", baseAP = 1, baseDP = 5, minLevel = 1),
    BAT("Bat", baseAP = 2, baseDP = 10, minLevel = 1),
    BABBLE("Babble", baseAP = 5, baseDP = 12, minLevel = 1),
    SPOOK("Spook", baseAP = 7, baseDP = 14, minLevel = 1),
    BELETH("Beleth", baseAP = 8, baseDP = 16, minLevel = 2),
    SKANDER_SNAKE("SkanderSnake", baseAP = 12, baseDP = 20, minLevel = 5),
    NECRO("Necro", baseAP = 13, baseDP = 30, minLevel = 6),
    KRAKEN("Kraken", baseAP = 40, baseDP = 200, minLevel = 1),
    ZARGON("ZARGON", baseAP = 100, baseDP = 400, minLevel = 1);
}
