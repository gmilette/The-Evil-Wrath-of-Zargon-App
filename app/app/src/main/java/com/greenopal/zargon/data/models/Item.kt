package com.greenopal.zargon.data.models

import kotlinx.serialization.Serializable

/**
 * Game items for inventory.
 * Maps to QBASIC items() AS STRING array
 */
@Serializable
data class Item(
    val name: String,
    val description: String = "",
    val type: ItemType = ItemType.MISC
)

enum class ItemType {
    WEAPON,
    ARMOR,
    CONSUMABLE,
    KEY_ITEM,  // Story items like "trapped soul"
    MISC
}

/**
 * Common items from the game
 */
object Items {
    val TRAPPED_SOUL = Item(
        name = "trapped soul",
        description = "A soul trapped by the Necromancer",
        type = ItemType.KEY_ITEM
    )
}
