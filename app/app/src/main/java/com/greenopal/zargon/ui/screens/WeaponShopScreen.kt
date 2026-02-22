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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.domain.challenges.ChallengeModifiers
import com.greenopal.zargon.ui.theme.EmberOrange

@Composable
fun WeaponShopScreen(
    gameState: GameState,
    challengeModifiers: ChallengeModifiers,
    onShopExit: (GameState) -> Unit,
    prestige: PrestigeData? = null,
    modifier: Modifier = Modifier
) {
    val challengeConfig = gameState.challengeConfig

    var currentScreen by remember { mutableStateOf(ShopScreen.MAIN) }
    var message by remember { mutableStateOf<String?>(null) }
    var updatedGameState by remember { mutableStateOf(gameState) }

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
                    val activeDesc = challengeConfig.getDisplayName()
                    if (activeDesc != "Normal") {
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
                                Text(
                                    text = "CHALLENGE: $activeDesc",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = EmberOrange,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

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

                when (currentScreen) {
                    ShopScreen.MAIN -> MainMenu(
                        onBuyWeapon = { currentScreen = ShopScreen.WEAPONS },
                        onBuyArmor = { currentScreen = ShopScreen.ARMOR },
                        onLeave = { onShopExit(updatedGameState) }
                    )

                    ShopScreen.WEAPONS -> WeaponList(
                        currentGold = updatedGameState.character.gold,
                        currentWeaponStatus = updatedGameState.character.weaponStatus,
                        challengeConfig = challengeConfig,
                        challengeModifiers = challengeModifiers,
                        prestige = prestige,
                        onPurchase = { weapon, effectiveBonus, displayName ->
                            val cost = weapon.basePrice
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
                        currentGold = updatedGameState.character.gold,
                        currentArmorStatus = updatedGameState.character.armorStatus,
                        challengeConfig = challengeConfig,
                        challengeModifiers = challengeModifiers,
                        prestige = prestige,
                        onPurchase = { armor, effectiveBonus, displayName ->
                            val cost = armor.basePrice
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
    currentGold: Int,
    currentWeaponStatus: Int,
    challengeConfig: com.greenopal.zargon.data.models.ChallengeConfig?,
    challengeModifiers: ChallengeModifiers,
    prestige: PrestigeData?,
    onPurchase: (Weapon, Int, String) -> Unit,
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
            Weapon.values().forEach { weapon ->
                val displayName = challengeModifiers.getWeaponDisplayName(
                    weapon.displayName, challengeConfig, prestige
                )
                val effectiveBonus = challengeModifiers.getEffectiveWeaponBonus(
                    weapon.attackBonus, challengeConfig, prestige
                )
                val isEquipped = weapon.ordinal == currentWeaponStatus

                item {
                    WeaponItem(
                        name = displayName,
                        price = weapon.basePrice,
                        effectiveBonus = effectiveBonus,
                        canAfford = currentGold >= weapon.basePrice,
                        isEquipped = isEquipped
                    ) {
                        onPurchase(weapon, effectiveBonus, displayName)
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
    currentGold: Int,
    currentArmorStatus: Int,
    challengeConfig: com.greenopal.zargon.data.models.ChallengeConfig?,
    challengeModifiers: ChallengeModifiers,
    prestige: PrestigeData?,
    onPurchase: (Armor, Int, String) -> Unit,
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
            Armor.values().forEach { armor ->
                val displayName = challengeModifiers.getArmorDisplayName(
                    armor.displayName, challengeConfig, prestige
                )
                val effectiveBonus = challengeModifiers.getEffectiveArmorBonus(
                    armor.defenseBonus, challengeConfig, prestige
                )
                val isEquipped = armor.ordinal == currentArmorStatus

                item {
                    ArmorItem(
                        name = displayName,
                        price = armor.basePrice,
                        effectiveBonus = effectiveBonus,
                        canAfford = currentGold >= armor.basePrice,
                        isEquipped = isEquipped
                    ) {
                        onPurchase(armor, effectiveBonus, displayName)
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
    DAGGER("dagger", 20, 5),
    SHORT_SWORD("short sword", 45, 8),
    LONG_SWORD("long sword", 100, 13),
    SWORD_OF_THORNS("sword of thorns", 175, 18),
    BROAD_SWORD("broad sword", 280, 23),
    TWOHANDED_SWORD("twohanded sword", 400, 28),
    ATLANTEAN_SWORD("Atlantean Sword", 600, 35),
}

enum class Armor(val displayName: String, val basePrice: Int, val defenseBonus: Int) {
    CLOTH("cloth", 15, 5),
    LEATHER("leather", 35, 8),
    PLATED_LEATHER("plated leather", 80, 15),
    SPIKED_LEATHER("spiked leather", 160, 20),
    CHAIN_MAIL("chain mail", 300, 30),
    PLATEMAIL("platemail", 550, 42),
}

fun getWeaponName(weaponStatus: Int): String {
    return Weapon.values().getOrNull(weaponStatus)?.displayName ?: "None"
}

fun getArmorName(armorStatus: Int): String {
    return Armor.values().getOrNull(armorStatus)?.displayName ?: "None"
}
