package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.GameState
import kotlin.random.Random

/**
 * Weapon shop screen (Gothox Slothair)
 * Based on QBASIC wepshop procedure (ZARGON.BAS:3356-3439)
 */
@Composable
fun WeaponShopScreen(
    gameState: GameState,
    onShopExit: (GameState) -> Unit,
    modifier: Modifier = Modifier
) {
    // Gothox's mood affects prices
    val mood by remember {
        mutableStateOf(
            when (Random.nextInt(100)) {
                in 0..24 -> ShopMood.GOOD
                in 25..74 -> ShopMood.NORMAL
                else -> ShopMood.ANGRY
            }
        )
    }

    var currentScreen by remember { mutableStateOf(ShopScreen.MAIN) }
    var message by remember { mutableStateOf<String?>(null) }
    var updatedGameState by remember { mutableStateOf(gameState) }

    // Handle Android back button
    BackHandler {
        when (currentScreen) {
            ShopScreen.MAIN -> onShopExit(updatedGameState)
            else -> currentScreen = ShopScreen.MAIN
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
        ) {
            Box {
                // Exit button in top-left corner
                IconButton(
                    onClick = { onShopExit(updatedGameState) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit shop",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                // Shop header
                Text(
                    text = "Gothox Slothair's Weapon Shop",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "A large, dirt-covered blacksmith stands behind the counter",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Gothox appears to be in a(n) ${mood.displayName} mood",
                    style = MaterialTheme.typography.bodyMedium,
                    color = mood.getColor(),
                    fontWeight = FontWeight.Bold
                )

                // Gold display
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Text(
                        text = "You have ${updatedGameState.character.gold} gold",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Message display
                if (message != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = message!!,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Screen content
                when (currentScreen) {
                    ShopScreen.MAIN -> MainMenu(
                        onBuyWeapon = { currentScreen = ShopScreen.WEAPONS },
                        onBuyArmor = { currentScreen = ShopScreen.ARMOR },
                        onLeave = { onShopExit(updatedGameState) }
                    )

                    ShopScreen.WEAPONS -> WeaponList(
                        mood = mood,
                        currentGold = updatedGameState.character.gold,
                        currentWeaponStatus = updatedGameState.character.weaponStatus,
                        onPurchase = { weapon ->
                            val cost = (weapon.basePrice * mood.priceMultiplier).toInt()
                            if (updatedGameState.character.gold >= cost) {
                                updatedGameState = updatedGameState.updateCharacter(
                                    updatedGameState.character.copy(
                                        gold = updatedGameState.character.gold - cost,
                                        weaponBonus = weapon.attackBonus,
                                        weaponStatus = weapon.ordinal
                                    )
                                )
                                message = "Purchased ${weapon.displayName}!"
                                currentScreen = ShopScreen.MAIN
                            } else {
                                message = "You can't afford it"
                            }
                        },
                        onBack = { currentScreen = ShopScreen.MAIN }
                    )

                    ShopScreen.ARMOR -> ArmorList(
                        mood = mood,
                        currentGold = updatedGameState.character.gold,
                        currentArmorStatus = updatedGameState.character.armorStatus,
                        onPurchase = { armor ->
                            val cost = (armor.basePrice * mood.priceMultiplier).toInt()
                            if (updatedGameState.character.gold >= cost) {
                                updatedGameState = updatedGameState.updateCharacter(
                                    updatedGameState.character.copy(
                                        gold = updatedGameState.character.gold - cost,
                                        armorBonus = armor.defenseBonus,
                                        armorStatus = armor.ordinal
                                    )
                                )
                                message = "Purchased ${armor.displayName}!"
                                currentScreen = ShopScreen.MAIN
                            } else {
                                message = "You can't afford it"
                            }
                        },
                        onBack = { currentScreen = ShopScreen.MAIN }
                    )
                }
            }
        }
    }
    }
}

@Composable
private fun MainMenu(
    onBuyWeapon: () -> Unit,
    onBuyArmor: () -> Unit,
    onLeave: () -> Unit
) {
    Button(
        onClick = onBuyWeapon,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text("1. Buy a Weapon")
    }

    Button(
        onClick = onBuyArmor,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text("2. Buy Armor")
    }

    Button(
        onClick = onLeave,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
    ) {
        Text("0. Leave the store")
    }
}

@Composable
private fun WeaponList(
    mood: ShopMood,
    currentGold: Int,
    currentWeaponStatus: Int,
    onPurchase: (Weapon) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Weapons for Sale:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Weapon.values()) { weapon ->
                val adjustedPrice = (weapon.basePrice * mood.priceMultiplier).toInt()
                val isEquipped = weapon.ordinal == currentWeaponStatus
                WeaponItem(
                    weapon = weapon,
                    price = adjustedPrice,
                    canAfford = currentGold >= adjustedPrice,
                    isEquipped = isEquipped
                ) {
                    onPurchase(weapon)
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun WeaponItem(
    weapon: Weapon,
    price: Int,
    canAfford: Boolean,
    isEquipped: Boolean,
    onPurchase: () -> Unit
) {
    Button(
        onClick = onPurchase,
        modifier = Modifier.fillMaxWidth(),
        enabled = canAfford && !isEquipped,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isEquipped -> MaterialTheme.colorScheme.secondary
                canAfford -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            },
            contentColor = when {
                isEquipped -> MaterialTheme.colorScheme.onSecondary
                canAfford -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurface
            },
            disabledContainerColor = when {
                isEquipped -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            },
            disabledContentColor = when {
                isEquipped -> MaterialTheme.colorScheme.onSecondary
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }
        ),
        border = if (!canAfford && !isEquipped) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isEquipped) {
                    "${weapon.displayName} (+${weapon.attackBonus} AP) [EQUIPPED]"
                } else {
                    "${weapon.displayName} (+${weapon.attackBonus} AP)"
                }
            )
            if (!isEquipped) {
                Text(
                    text = "${price}g",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ArmorList(
    mood: ShopMood,
    currentGold: Int,
    currentArmorStatus: Int,
    onPurchase: (Armor) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Armor for Sale:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Armor.values()) { armor ->
                val adjustedPrice = (armor.basePrice * mood.priceMultiplier).toInt()
                val isEquipped = armor.ordinal == currentArmorStatus
                ArmorItem(
                    armor = armor,
                    price = adjustedPrice,
                    canAfford = currentGold >= adjustedPrice,
                    isEquipped = isEquipped
                ) {
                    onPurchase(armor)
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text("Back")
        }
    }
}

@Composable
private fun ArmorItem(
    armor: Armor,
    price: Int,
    canAfford: Boolean,
    isEquipped: Boolean,
    onPurchase: () -> Unit
) {
    Button(
        onClick = onPurchase,
        modifier = Modifier.fillMaxWidth(),
        enabled = canAfford && !isEquipped,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isEquipped -> MaterialTheme.colorScheme.secondary
                canAfford -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            },
            contentColor = when {
                isEquipped -> MaterialTheme.colorScheme.onSecondary
                canAfford -> MaterialTheme.colorScheme.onPrimary
                else -> MaterialTheme.colorScheme.onSurface
            },
            disabledContainerColor = when {
                isEquipped -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            },
            disabledContentColor = when {
                isEquipped -> MaterialTheme.colorScheme.onSecondary
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            }
        ),
        border = if (!canAfford && !isEquipped) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (isEquipped) {
                    "${armor.displayName} (+${armor.defenseBonus} DP) [EQUIPPED]"
                } else {
                    "${armor.displayName} (+${armor.defenseBonus} DP)"
                }
            )
            if (!isEquipped) {
                Text(
                    text = "${price}g",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

enum class ShopScreen {
    MAIN, WEAPONS, ARMOR
}

enum class ShopMood(val displayName: String, val priceMultiplier: Float) {
    GOOD("good", 0.88f),
    NORMAL("normal", 1.0f),
    ANGRY("angry", 1.22f)
}

@Composable
fun ShopMood.getColor(): Color {
    return when (this) {
        ShopMood.GOOD -> MaterialTheme.colorScheme.secondary
        ShopMood.NORMAL -> MaterialTheme.colorScheme.primary
        ShopMood.ANGRY -> MaterialTheme.colorScheme.tertiary
    }
}

enum class Weapon(val displayName: String, val basePrice: Int, val attackBonus: Int) {
    DAGGER("dagger", 20, 2),
    SHORT_SWORD("short sword", 30, 3),
    LONG_SWORD("long sword", 65, 5),
    SWORD_OF_THORNS("sword of thorns", 88, 7),
    BROAD_SWORD("broad sword", 103, 9),
    TWOHANDED_SWORD("twohanded sword", 250, 15),
    ATLANTEAN_SWORD("Atlantean Sword", 500, 25),
    // too powerful
//    GERMANIC_WARCLEAVER("Germanic WarCleaver", 834, 40),
//    SIR_BEATDOWN("Sir Beatdown", 2500, 60)
}

enum class Armor(val displayName: String, val basePrice: Int, val defenseBonus: Int) {
    CLOTH("cloth", 20, 5),
    LEATHER("leather", 40, 10),
    PLATED_LEATHER("plated leather", 88, 18),
    SPIKED_LEATHER("spiked leather", 98, 20),
    CHAIN_MAIL("chain mail", 134, 28),
    PLATEMAIL("platemail", 279, 50),
//    SPLINT_MAIL("Splintmail", 578, 80),
//    RITE_OF_TOUGH_SKIN("Rite of tough skin", 1004, 120)
}

/**
 * Get weapon name from weaponStatus ordinal
 */
fun getWeaponName(weaponStatus: Int): String {
    return Weapon.values().getOrNull(weaponStatus)?.displayName ?: "None"
}

/**
 * Get armor name from armorStatus ordinal
 */
fun getArmorName(armorStatus: Int): String {
    return Armor.values().getOrNull(armorStatus)?.displayName ?: "None"
}
