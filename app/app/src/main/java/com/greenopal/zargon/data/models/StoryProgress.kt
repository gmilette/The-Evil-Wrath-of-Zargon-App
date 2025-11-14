package com.greenopal.zargon.data.models

/**
 * Story progression tracking
 * Based on QBASIC storystatus variable (ZARGON.BAS:102)
 *
 * Story stages:
 * 1.0 = Start, need to rescue boatman
 * 1.5 = Know about dynamite
 * 2.0 = Rescued boatman
 * 2.5 = Gave boatman wood, received boat plans
 * 3.0 = Boatman died (need to collect items)
 * 3.2 = Have boat plans, can read them
 * 4.0 = Need to resurrect boatman
 * 4.3 = Have soul, ready to give to necromancer
 * 5.0 = Boatman resurrected
 * 5.5 = Boat built, can travel river
 * 6.0+ = On island, approaching castle
 */
data class StoryProgress(
    val status: Float = 1.0f,
    val hasBoatPlans: Boolean = false,
    val hasSoul: Boolean = false,
    val hasWood: Boolean = false,
    val hasCloth: Boolean = false,
    val hasRutter: Boolean = false,
    val boatBuilt: Boolean = false
) {
    fun advanceTo(newStatus: Float): StoryProgress {
        return copy(status = newStatus)
    }

    fun giveItem(item: String): StoryProgress {
        return when (item.lowercase()) {
            "boat plans", "boat list" -> copy(hasBoatPlans = true)
            "soul" -> copy(hasSoul = true)
            "wood" -> copy(hasWood = true)
            "cloth" -> copy(hasCloth = true)
            "rutter" -> copy(hasRutter = true)
            else -> this
        }
    }

    fun buildBoat(): StoryProgress {
        return copy(boatBuilt = true, status = 5.5f)
    }
}

/**
 * NPC dialog data
 * Based on QBASIC hut system (ZARGON.BAS:1860+)
 */
data class Dialog(
    val question1: String,
    val answer1: String,
    val question2: String,
    val answer2: String,
    val question3: String,
    val answer3: String,
    val storyAction: StoryAction? = null
)

sealed class StoryAction {
    data class AdvanceStory(val newStatus: Float) : StoryAction()
    data class GiveItem(val itemName: String) : StoryAction()
    data class TakeItem(val itemName: String) : StoryAction()
    object BuildBoat : StoryAction()
    object ResurrectBoatman : StoryAction()
}

/**
 * NPC types
 */
enum class NpcType(val npcId: String, val displayName: String) {
    BOATMAN("11", "Boatman"),
    SANDMAN("14", "Sandman"),
    NECROMANCER("41", "Necromancer"),
    GOTHOX("W", "Gothox (Weapon Shop)"),
    HEALER("H", "Healer")
}
