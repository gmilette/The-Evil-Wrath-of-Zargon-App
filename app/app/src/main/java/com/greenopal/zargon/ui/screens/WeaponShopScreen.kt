package com.greenopal.zargon.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
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
                    color = mood.color,
                    fontWeight = FontWeight.Bold
                )

                // Gold display
                Text(
                    text = "You have ${updatedGameState.character.gold} gold",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFFFFD700)
                )

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
                        onPurchase = { weapon ->
                            val cost = (weapon.basePrice * mood.priceMultiplier).toInt()
                            if (updatedGameState.character.gold >= cost) {
                                updatedGameState = updatedGameState.updateCharacter(
                                    updatedGameState.character.copy(
                                        gold = updatedGameState.character.gold - cost,
                                        weaponBonus = weapon.attackBonus
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
                        onPurchase = { armor ->
                            val cost = (armor.basePrice * mood.priceMultiplier).toInt()
                            if (updatedGameState.character.gold >= cost) {
                                updatedGameState = updatedGameState.updateCharacter(
                                    updatedGameState.character.copy(
                                        gold = updatedGameState.character.gold - cost,
                                        armorBonus = armor.defenseBonus
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

@Composable
private fun MainMenu(
    onBuyWeapon: () -> Unit,
    onBuyArmor: () -> Unit,
    onLeave: () -> Unit
) {
    Button(
        onClick = onBuyWeapon,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("1. Buy a Weapon")
    }

    Button(
        onClick = onBuyArmor,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text("2. Buy Armor")
    }

    Button(
        onClick = onLeave,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
    ) {
        Text("0. Leave the store")
    }
}

@Composable
private fun WeaponList(
    mood: ShopMood,
    currentGold: Int,
    onPurchase: (Weapon) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Weapons for Sale:", style = MaterialTheme.typography.titleMedium)

        Weapon.values().forEach { weapon ->
            val adjustedPrice = (weapon.basePrice * mood.priceMultiplier).toInt()
            WeaponItem(weapon, adjustedPrice, currentGold >= adjustedPrice) {
                onPurchase(weapon)
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
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
    onPurchase: () -> Unit
) {
    Button(
        onClick = onPurchase,
        modifier = Modifier.fillMaxWidth(),
        enabled = canAfford,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (canAfford) MaterialTheme.colorScheme.secondary else Color.DarkGray
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${weapon.displayName} (+${weapon.attackBonus} AP)")
            Text("${price}g", color = Color(0xFFFFD700))
        }
    }
}

@Composable
private fun ArmorList(
    mood: ShopMood,
    currentGold: Int,
    onPurchase: (Armor) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Armor for Sale:", style = MaterialTheme.typography.titleMedium)

        Armor.values().forEach { armor ->
            val adjustedPrice = (armor.basePrice * mood.priceMultiplier).toInt()
            ArmorItem(armor, adjustedPrice, currentGold >= adjustedPrice) {
                onPurchase(armor)
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray)
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
    onPurchase: () -> Unit
) {
    Button(
        onClick = onPurchase,
        modifier = Modifier.fillMaxWidth(),
        enabled = canAfford,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (canAfford) MaterialTheme.colorScheme.secondary else Color.DarkGray
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("${armor.displayName} (+${armor.defenseBonus} DP)")
            Text("${price}g", color = Color(0xFFFFD700))
        }
    }
}

enum class ShopScreen {
    MAIN, WEAPONS, ARMOR
}

enum class ShopMood(val displayName: String, val priceMultiplier: Float, val color: Color) {
    GOOD("good", 0.88f, Color.Green),
    NORMAL("normal", 1.0f, Color.Yellow),
    ANGRY("angry", 1.22f, Color.Red)
}

enum class Weapon(val displayName: String, val basePrice: Int, val attackBonus: Int) {
    DAGGER("dagger", 20, 2),
    SHORT_SWORD("short sword", 30, 3),
    LONG_SWORD("long sword", 65, 5),
    SWORD_OF_THORNS("sword of thorns", 88, 7),
    BROAD_SWORD("broad sword", 103, 9),
    TWOHANDED_SWORD("twohanded sword", 250, 15),
    ATLANTEAN_SWORD("Atlantean Sword", 500, 25),
    GERMANIC_WARCLEAVER("Germanic WarCleaver", 834, 40)
}

enum class Armor(val displayName: String, val basePrice: Int, val defenseBonus: Int) {
    CLOTH("cloth", 20, 5),
    LEATHER("leather", 40, 10),
    PLATED_LEATHER("plated leather", 88, 18),
    SPIKED_LEATHER("spiked leather", 98, 20),
    CHAIN_MAIL("chain mail", 134, 28),
    PLATEMAIL("platemail", 279, 50),
    SPLINT_MAIL("Splint mail", 578, 80),
    RITE_OF_TOUGH_SKIN("Rite of tough skin", 1004, 120)
}
