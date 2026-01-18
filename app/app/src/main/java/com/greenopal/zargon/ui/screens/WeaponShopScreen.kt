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
import androidx.hilt.navigation.compose.hiltViewModel
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.EquipmentMode
import com.greenopal.zargon.domain.challenges.ChallengeModifiers
import com.greenopal.zargon.ui.theme.EmberOrange

/**
 * Weapon shop screen (Gothox Slothair)
 * Based on QBASIC wepshop procedure (ZARGON.BAS:3356-3439)
 */
@Composable
fun WeaponShopScreen(
    gameState: GameState,
    challengeModifiers: ChallengeModifiers,
    onShopExit: (GameState) -> Unit,
    modifier: Modifier = Modifier
) {
    val priceMultiplier = 1.0f
    val challengeConfig = gameState.challengeConfig

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
                        .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 56.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
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

                if (challengeConfig != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = EmberOrange.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (challengeConfig.weaponMode != EquipmentMode.NORMAL || challengeConfig.armorMode != EquipmentMode.NORMAL) {
                                Text(
                                    text = "CHALLENGE MODE ACTIVE",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = EmberOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            if (challengeConfig.weaponMode != EquipmentMode.NORMAL) {
                                Text(
                                    text = "${challengeConfig.weaponMode.weaponDisplayName}: ${challengeConfig.weaponMode.powerMultiplier}x Power, ${challengeConfig.weaponMode.costMultiplier}x Cost",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            if (challengeConfig.armorMode != EquipmentMode.NORMAL) {
                                Text(
                                    text = "${challengeConfig.armorMode.armorDisplayName}: ${challengeConfig.armorMode.powerMultiplier}x Power, ${challengeConfig.armorMode.costMultiplier}x Cost",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

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
                        priceMultiplier = priceMultiplier,
                        currentGold = updatedGameState.character.gold,
                        currentWeaponStatus = updatedGameState.character.weaponStatus,
                        challengeConfig = challengeConfig,
                        challengeModifiers = challengeModifiers,
                        onPurchase = { weapon, bonusMultiplier, costMultiplier, displayName ->
                            if (!challengeModifiers.canPurchaseWeapons(challengeConfig)) {
                                message = "Weapons disabled in this challenge mode!"
                                return@WeaponList
                            }
                            val baseCost = (weapon.basePrice * priceMultiplier).toInt()
                            val cost = (baseCost * costMultiplier).toInt()
                            val effectiveBonus = (weapon.attackBonus * bonusMultiplier).toInt()

                            if (updatedGameState.character.gold >= cost) {
                                updatedGameState = updatedGameState.updateCharacter(
                                    updatedGameState.character.copy(
                                        gold = updatedGameState.character.gold - cost,
                                        weaponBonus = effectiveBonus,
                                        weaponStatus = weapon.ordinal
                                    )
                                )
                                message = "Purchased $displayName!"
                                currentScreen = ShopScreen.MAIN
                            } else {
                                message = "You can't afford it"
                            }
                        },
                        onBack = { currentScreen = ShopScreen.MAIN }
                    )

                    ShopScreen.ARMOR -> ArmorList(
                        priceMultiplier = priceMultiplier,
                        currentGold = updatedGameState.character.gold,
                        currentArmorStatus = updatedGameState.character.armorStatus,
                        challengeConfig = challengeConfig,
                        challengeModifiers = challengeModifiers,
                        onPurchase = { armor, bonusMultiplier, costMultiplier, displayName ->
                            if (!challengeModifiers.canPurchaseArmor(challengeConfig)) {
                                message = "Armor disabled in this challenge mode!"
                                return@ArmorList
                            }
                            val baseCost = (armor.basePrice * priceMultiplier).toInt()
                            val cost = (baseCost * costMultiplier).toInt()
                            val effectiveBonus = (armor.defenseBonus * bonusMultiplier).toInt()

                            if (updatedGameState.character.gold >= cost) {
                                updatedGameState = updatedGameState.updateCharacter(
                                    updatedGameState.character.copy(
                                        gold = updatedGameState.character.gold - cost,
                                        armorBonus = effectiveBonus,
                                        armorStatus = armor.ordinal
                                    )
                                )
                                message = "Purchased $displayName!"
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
    priceMultiplier: Float,
    currentGold: Int,
    currentWeaponStatus: Int,
    challengeConfig: com.greenopal.zargon.data.models.ChallengeConfig?,
    challengeModifiers: ChallengeModifiers,
    onPurchase: (Weapon, Float, Float, String) -> Unit,
    onBack: () -> Unit
) {
    val canPurchase = challengeModifiers.canPurchaseWeapons(challengeConfig)
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Weapons for Sale:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (!canPurchase) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = EmberOrange.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Weapons are disabled in this challenge mode!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EmberOrange,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Weapon.values().forEach { weapon ->
                val baseCost = (weapon.basePrice * priceMultiplier).toInt()
                val isEquipped = weapon.ordinal == currentWeaponStatus

                item {
                    WeaponItem(
                        name = weapon.displayName,
                        price = baseCost,
                        effectiveBonus = weapon.attackBonus,
                        canAfford = currentGold >= baseCost && canPurchase,
                        isEquipped = isEquipped,
                        isDisabled = !canPurchase
                    ) {
                        onPurchase(weapon, 1f, 1f, weapon.displayName)
                    }
                }

                if (challengeConfig?.weaponMode == EquipmentMode.GREAT) {
                    item {
                        WeaponItem(
                            name = "great ${weapon.displayName}",
                            price = (baseCost * 2),
                            effectiveBonus = weapon.attackBonus * 2,
                            canAfford = currentGold >= (baseCost * 2) && canPurchase,
                            isEquipped = false,
                            isDisabled = !canPurchase
                        ) {
                            onPurchase(weapon, 2f, 2f, "great ${weapon.displayName}")
                        }
                    }
                }

                if (challengeConfig?.weaponMode == EquipmentMode.BEATDOWN) {
                    item {
                        WeaponItem(
                            name = "beatdown ${weapon.displayName}",
                            price = (baseCost * 10),
                            effectiveBonus = weapon.attackBonus * 10,
                            canAfford = currentGold >= (baseCost * 10) && canPurchase,
                            isEquipped = false,
                            isDisabled = !canPurchase
                        ) {
                            onPurchase(weapon, 10f, 10f, "beatdown ${weapon.displayName}")
                        }
                    }
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
    name: String,
    price: Int,
    effectiveBonus: Int,
    canAfford: Boolean,
    isEquipped: Boolean,
    isDisabled: Boolean = false,
    onPurchase: () -> Unit
) {
    Button(
        onClick = onPurchase,
        modifier = Modifier.fillMaxWidth(),
        enabled = canAfford && !isEquipped && !isDisabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isEquipped -> MaterialTheme.colorScheme.secondary
                canAfford && !isDisabled -> MaterialTheme.colorScheme.primary
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
                    "$name (+${effectiveBonus} AP) [EQUIPPED]"
                } else {
                    "$name (+${effectiveBonus} AP)"
                }
            )
            if (!isEquipped) {
                Text(
                    text = "${price}g",
                    color = if (canAfford) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ArmorList(
    priceMultiplier: Float,
    currentGold: Int,
    currentArmorStatus: Int,
    challengeConfig: com.greenopal.zargon.data.models.ChallengeConfig?,
    challengeModifiers: ChallengeModifiers,
    onPurchase: (Armor, Float, Float, String) -> Unit,
    onBack: () -> Unit
) {
    val canPurchase = challengeModifiers.canPurchaseArmor(challengeConfig)
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Armor for Sale:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (!canPurchase) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = EmberOrange.copy(alpha = 0.3f)
                )
            ) {
                Text(
                    text = "Armor is disabled in this challenge mode!",
                    style = MaterialTheme.typography.bodyLarge,
                    color = EmberOrange,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Armor.values().forEach { armor ->
                val baseCost = (armor.basePrice * priceMultiplier).toInt()
                val isEquipped = armor.ordinal == currentArmorStatus

                item {
                    ArmorItem(
                        name = armor.displayName,
                        price = baseCost,
                        effectiveBonus = armor.defenseBonus,
                        canAfford = currentGold >= baseCost && canPurchase,
                        isEquipped = isEquipped,
                        isDisabled = !canPurchase
                    ) {
                        onPurchase(armor, 1f, 1f, armor.displayName)
                    }
                }

                if (challengeConfig?.armorMode == EquipmentMode.GREAT) {
                    item {
                        ArmorItem(
                            name = "great ${armor.displayName}",
                            price = (baseCost * 2),
                            effectiveBonus = armor.defenseBonus * 2,
                            canAfford = currentGold >= (baseCost * 2) && canPurchase,
                            isEquipped = false,
                            isDisabled = !canPurchase
                        ) {
                            onPurchase(armor, 2f, 2f, "great ${armor.displayName}")
                        }
                    }
                }

                if (challengeConfig?.armorMode == EquipmentMode.BEATDOWN) {
                    item {
                        ArmorItem(
                            name = "beatdown ${armor.displayName}",
                            price = (baseCost * 10),
                            effectiveBonus = armor.defenseBonus * 10,
                            canAfford = currentGold >= (baseCost * 10) && canPurchase,
                            isEquipped = false,
                            isDisabled = !canPurchase
                        ) {
                            onPurchase(armor, 10f, 10f, "beatdown ${armor.displayName}")
                        }
                    }
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
    name: String,
    price: Int,
    effectiveBonus: Int,
    canAfford: Boolean,
    isEquipped: Boolean,
    isDisabled: Boolean = false,
    onPurchase: () -> Unit
) {
    Button(
        onClick = onPurchase,
        modifier = Modifier.fillMaxWidth(),
        enabled = canAfford && !isEquipped && !isDisabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isEquipped -> MaterialTheme.colorScheme.secondary
                canAfford && !isDisabled -> MaterialTheme.colorScheme.primary
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
                    "$name (+${effectiveBonus} DP) [EQUIPPED]"
                } else {
                    "$name (+${effectiveBonus} DP)"
                }
            )
            if (!isEquipped) {
                Text(
                    text = "${price}g",
                    color = if (canAfford) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
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
