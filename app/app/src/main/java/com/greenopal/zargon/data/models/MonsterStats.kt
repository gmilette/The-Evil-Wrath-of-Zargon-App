package com.greenopal.zargon.data.models

/**
 * Monster statistics for battle.
 * Maps to QBASIC variables: M$, mAP, mDP, howmuchbigger
 */
data class MonsterStats(
    val type: MonsterType,
    val attackPower: Int,       // mAP in QBASIC
    val currentHP: Int,         // mDP in QBASIC
    val maxHP: Int,             // Original mDP
    val scalingFactor: Int = 1, // howmuchbigger in QBASIC (must be >= 1)
    val displayName: String? = null, // Custom display name (e.g., "Great Bat")
) {
    init {
        // Validate scalingFactor to prevent division by zero or negative values
        require(scalingFactor >= 1) { "scalingFactor must be at least 1, got $scalingFactor" }
        require(maxHP > 0) { "maxHP must be positive, got $maxHP" }
    }

    val name: String get() = displayName ?: type.displayName
    val isAlive: Boolean get() = currentHP > 0
    val hpPercentage: Float get() = currentHP.toFloat() / maxHP.toFloat()

    /**
     * Take damage and return updated stats
     */
    fun takeDamage(damage: Int): MonsterStats {
        return copy(currentHP = maxOf(0, currentHP - damage))
    }

    companion object {
        /**
         * Create a monster with level-based scaling
         * Based on SelectMonsta procedure (ZARGON.BAS:3083)
         */
        fun create(type: MonsterType, playerLevel: Int): MonsterStats {
            // Calculate scaling factor (howmuchbigger)
            // In QBASIC: howmuchbigger = INT(RND * (lev / 2 + 1)) + 1
            val scalingFactor = (playerLevel / 2 + 1).coerceAtLeast(1)

            val scaledAP = type.baseAP * scalingFactor
            val scaledDP = type.baseDP * scalingFactor

            return MonsterStats(
                type = type,
                attackPower = scaledAP,
                currentHP = scaledDP,
                maxHP = scaledDP,
                scalingFactor = scalingFactor
            )
        }
    }
}
