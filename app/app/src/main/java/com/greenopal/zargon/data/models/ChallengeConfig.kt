package com.greenopal.zargon.data.models

import kotlinx.serialization.Serializable

@Serializable
enum class Challenge(
    val displayName: String,
    val description: String
) {
    WEAK_ARMOR("Weak Armor", "Armor is less effective"),
    WEAK_WEAPONS("Weak Weapons", "Weapons are less effective"),
    STRONG_ENEMIES("Strong Enemies", "Enemies are much stronger"),
    STRONGER_ENEMIES("Stronger Enemies", "Enemies are far stronger"),
    ONE_DEATH("Permadeath", "Game over if Joe dies"),
    NO_MAGIC("No Magic", "Cannot use any spells"),
    IMPOSSIBLE_MISSION("Impossible Mission", "Permadeath with strong enemies"),
    MAGE_QUEST("Mage Quest", "Weak equipment — rely on spells"),
    WARRIOR_MODE("Warrior Mode", "No magic with stronger enemies")
}

@Serializable
data class ChallengeConfig(
    val challenges: Set<Challenge> = emptySet()
) {
    fun getChallengeId(): String {
        val parts = mutableListOf<String>()
        challenges.sorted().forEach { parts.add(it.name) }
        return parts.joinToString("_").ifEmpty { "NORMAL" }
    }

    fun getDisplayName(): String {
        if (challenges.isEmpty()) return "Normal"
        return challenges.joinToString(" + ") { it.displayName }
    }

    val isPermadeath: Boolean get() =
        Challenge.ONE_DEATH in challenges || Challenge.IMPOSSIBLE_MISSION in challenges

    val isNoMagic: Boolean get() =
        Challenge.NO_MAGIC in challenges || Challenge.WARRIOR_MODE in challenges
}
