package com.greenopal.zargon.domain.progression

import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.Item
import com.greenopal.zargon.data.models.Items
import com.greenopal.zargon.data.models.MonsterType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Battle rewards and experience system
 * Based on WinBattle procedure (ZARGON.BAS:3658)
 */
@Singleton
class RewardSystem @Inject constructor() {

    /**
     * Calculate XP reward for defeating a monster
     * Based on ZARGON.BAS:3671-3678
     */
    fun calculateXP(monsterType: MonsterType, scalingFactor: Int): Int {
        val baseXP = when (monsterType) {
            MonsterType.SLIME -> 2
            MonsterType.BAT -> 4
            MonsterType.BABBLE -> 6
            MonsterType.SPOOK -> 12
            MonsterType.BELETH -> 18
            MonsterType.NECRO -> 20
            MonsterType.SKANDER_SNAKE -> 25
            MonsterType.KRAKEN -> 45 // No scaling for Kraken
            MonsterType.ZARGON -> 100 // Special boss
        }

        return if (monsterType == MonsterType.KRAKEN || monsterType == MonsterType.ZARGON) {
            baseXP
        } else {
            baseXP * scalingFactor
        }
    }

    /**
     * Calculate gold reward for defeating a monster
     * Based on ZARGON.BAS:3671-3678
     */
    fun calculateGold(monsterType: MonsterType, scalingFactor: Int): Int {
        // gLoss = (-1 * howmuchbigger * 3) - this is actually a bonus
        val bonus = scalingFactor * 3

        val baseGold = when (monsterType) {
            MonsterType.SLIME -> 3
            MonsterType.BAT -> 5
            MonsterType.BABBLE -> 10
            MonsterType.SPOOK -> 14
            MonsterType.BELETH -> 23 - Random.nextInt(1, 7) // gnLOSS
            MonsterType.NECRO -> 20
            MonsterType.SKANDER_SNAKE -> 25
            MonsterType.KRAKEN -> 30
            MonsterType.ZARGON -> 100
        }

        return baseGold + bonus
    }

    /**
     * Check for special item drops
     * Based on ZARGON.BAS:3685-3688
     */
    fun getSpecialDrop(
        monsterType: MonsterType,
        worldX: Int,
        worldY: Int
    ): Item? {
        // Necro at map(4,2) drops trapped soul
        if (monsterType == MonsterType.NECRO && worldX == 4 && worldY == 2) {
            return Items.TRAPPED_SOUL
        }
        return null
    }

    /**
     * Calculate XP needed for next level
     * Based on ZARGON.BAS:3694-3696
     * Formula: nextlev = nextlev + (nextlev + lev * 30)
     */
    fun calculateXPForNextLevel(currentLevel: Int, currentNextLevelXP: Int): Int {
        val addTo = currentNextLevelXP + (currentLevel * 30)
        return currentNextLevelXP + addTo
    }

    /**
     * Get initial XP requirement for level 2
     */
    fun getInitialNextLevelXP(): Int {
        return 30 // Starting requirement
    }
}

/**
 * Leveling system
 * Based on CheckLevel procedure (ZARGON.BAS:836)
 */
@Singleton
class LevelingSystem @Inject constructor() {

    /**
     * Level up the character with random stat gains
     * Based on ZARGON.BAS:837-850
     */
    fun levelUp(character: CharacterStats): CharacterStats {
        val currentLevel = character.level

        // Random stat gains (QBASIC: INT(RND * lev) + bonus)
        val apGain = Random.nextInt(0, currentLevel + 1) + 2
        val dpGain = 4 + Random.nextInt(0, currentLevel + 1) + 1
        val mpGain = 3 + Random.nextInt(0, currentLevel + 1) + 1

        return character.levelUp(apGain, dpGain, mpGain)
    }

    /**
     * Check if character should level up and apply level gains
     * Returns pair of (updated character, did level up)
     */
    fun checkAndApplyLevelUp(
        character: CharacterStats,
        nextLevelXP: Int
    ): Pair<CharacterStats, Boolean> {
        return if (character.experience >= nextLevelXP) {
            val leveledCharacter = levelUp(character)
            Pair(leveledCharacter, true)
        } else {
            Pair(character, false)
        }
    }
}

/**
 * Complete battle rewards including XP, gold, items, and leveling
 */
data class BattleRewards(
    val xpGained: Int,
    val goldGained: Int,
    val itemDropped: Item?,
    val leveledUp: Boolean = false,
    val newLevel: Int? = null,
    val apGain: Int? = null,
    val dpGain: Int? = null,
    val mpGain: Int? = null
)
