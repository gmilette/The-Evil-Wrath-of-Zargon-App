package com.greenopal.zargon.domain.world

import com.greenopal.zargon.data.models.GameState
import kotlin.random.Random

/**
 * World spell system - spells cast while exploring
 * Based on QBASIC castspell procedure (ZARGON.BAS:781)
 */
sealed class WorldSpell(
    val id: Int,
    val name: String,
    val mpCost: Int,
    val requiredSpellLevel: Int,
    val description: String
) {
    /**
     * Check if player can cast this spell
     */
    fun canCast(currentMP: Int, spellLevel: Int): Boolean {
        return currentMP >= mpCost && spellLevel >= requiredSpellLevel
    }

    /**
     * Apply spell effect to game state
     * @return Updated game state and result message
     */
    abstract fun cast(gameState: GameState): Pair<GameState, String>

    /**
     * Cure spell (ZARGON.BAS:793-798)
     * MP cost: 3
     * Heal: INT(RND * 6) + 6 * level
     */
    object Cure : WorldSpell(
        id = 1,
        name = "Cure",
        mpCost = 3,
        requiredSpellLevel = 3,
        description = "Restore hit points"
    ) {
        override fun cast(gameState: GameState): Pair<GameState, String> {
            if (gameState.character.currentHP >= gameState.character.maxHP) {
                return gameState to "Already at full health!"
            }
            // QBASIC: damage = INT(RND * 6) + 6 * lev
            val healAmount = Random.nextInt(1, 7) + 6 * gameState.character.level

            val newHP = minOf(
                gameState.character.currentHP + healAmount,
                gameState.character.maxHP
            )

            val actualHealed = newHP - gameState.character.currentHP

            val updatedState = gameState.copy(
                character = gameState.character.copy(
                    currentHP = newHP,
                    currentMP = gameState.character.currentMP - mpCost
                )
            )

            return updatedState to "Cured $actualHealed HP!"
        }
    }

    /**
     * Restore spell - Enhanced healing (user requested)
     * MP cost: 8
     * Heal: INT(RND * 10) + 10 * level
     */
    object Restore : WorldSpell(
        id = 2,
        name = "Restore",
        mpCost = 8,
        requiredSpellLevel = 5,
        description = "Restore more hit points"
    ) {
        override fun cast(gameState: GameState): Pair<GameState, String> {
            if (gameState.character.currentHP >= gameState.character.maxHP) {
                return gameState to "Already at full health!"
            }
            val healAmount = Random.nextInt(1, 11) + 10 * gameState.character.level

            val newHP = minOf(
                gameState.character.currentHP + healAmount,
                gameState.character.maxHP
            )

            val actualHealed = newHP - gameState.character.currentHP

            val updatedState = gameState.copy(
                character = gameState.character.copy(
                    currentHP = newHP,
                    currentMP = gameState.character.currentMP - mpCost
                )
            )

            return updatedState to "Restored $actualHealed HP!"
        }
    }

    /**
     * Warp spell (ZARGON.BAS:805-812)
     * MP cost: 7
     * Effect: Teleport to healer location (Map 2,4 at coordinates 10,8)
     */
    object Warp : WorldSpell(
        id = 3,
        name = "Warp",
        mpCost = 7,
        requiredSpellLevel = 8,
        description = "Teleport to the healer"
    ) {
        override fun cast(gameState: GameState): Pair<GameState, String> {
            // QBASIC: mapx = 2: mapy = 4: cx = 10: cy = 8
            val updatedState = gameState.copy(
                worldX = 2,
                worldY = 4,
                characterX = 10,
                characterY = 8,
                character = gameState.character.copy(
                    currentMP = gameState.character.currentMP - mpCost
                )
            )

            return updatedState to "Warped to the healer!"
        }
    }

    object GreaterCure : WorldSpell(
        id = 1,
        name = "Greater Cure",
        mpCost = 5,
        requiredSpellLevel = 3,
        description = "Restore hit points (1.5x enhanced)"
    ) {
        override fun cast(gameState: GameState): Pair<GameState, String> {
            if (gameState.character.currentHP >= gameState.character.maxHP) {
                return gameState to "Already at full health!"
            }
            val healAmount = (Random.nextInt(1, 7) + 6 * gameState.character.level) * 3 / 2
            val newHP = minOf(gameState.character.currentHP + healAmount, gameState.character.maxHP)
            val actualHealed = newHP - gameState.character.currentHP
            val updatedState = gameState.copy(
                character = gameState.character.copy(
                    currentHP = newHP,
                    currentMP = gameState.character.currentMP - mpCost
                )
            )
            return updatedState to "Greater Cure restored $actualHealed HP!"
        }
    }

    object GreaterRestore : WorldSpell(
        id = 2,
        name = "Greater Restore",
        mpCost = 12,
        requiredSpellLevel = 5,
        description = "Restore hit points (1.5x enhanced)"
    ) {
        override fun cast(gameState: GameState): Pair<GameState, String> {
            if (gameState.character.currentHP >= gameState.character.maxHP) {
                return gameState to "Already at full health!"
            }
            val healAmount = (Random.nextInt(1, 11) + 10 * gameState.character.level) * 3 / 2
            val newHP = minOf(gameState.character.currentHP + healAmount, gameState.character.maxHP)
            val actualHealed = newHP - gameState.character.currentHP
            val updatedState = gameState.copy(
                character = gameState.character.copy(
                    currentHP = newHP,
                    currentMP = gameState.character.currentMP - mpCost
                )
            )
            return updatedState to "Greater Restore healed $actualHealed HP!"
        }
    }

    companion object {
        /**
         * All available world spells
         */
        val ALL = listOf(Cure, Restore, Warp)

        /**
         * Get spells available at a given spell level, optionally with MASTER_SPELLBOOK enhancements
         */
        fun getAvailableSpells(spellLevel: Int, hasMasterSpellbook: Boolean = false): List<WorldSpell> {
            return ALL
                .filter { it.requiredSpellLevel <= spellLevel }
                .map { spell ->
                    if (!hasMasterSpellbook) spell
                    else when (spell) {
                        Cure -> GreaterCure
                        Restore -> GreaterRestore
                        else -> spell
                    }
                }
        }

        /**
         * Get spell by ID
         */
        fun getById(id: Int): WorldSpell? {
            return ALL.find { it.id == id }
        }
    }
}
