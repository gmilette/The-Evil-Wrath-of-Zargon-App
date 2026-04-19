package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.sp
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.domain.challenges.ChallengeModifiers
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalButtonVariant
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.OrnateSeparator
import com.greenopal.zargon.ui.components.PanelHeading
import com.greenopal.zargon.ui.components.FlavorText
import com.greenopal.zargon.ui.components.ScrollBanner
import com.greenopal.zargon.ui.theme.EmberBright
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.GoldBright
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.TextDim

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
    var message       by remember { mutableStateOf<String?>(null) }
    var updatedState  by remember { mutableStateOf(gameState) }

    BackHandler {
        if (currentScreen == ShopScreen.MAIN) onShopExit(updatedState)
        else currentScreen = ShopScreen.MAIN
    }

    DungeonBackground {
        LazyColumn(
            modifier            = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding      = PaddingValues(vertical = 24.dp),
        ) {
            item {
                MedievalPanel(
                    modifier       = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp, start = 24.dp, end = 24.dp),
                        ) {
                            Text(
                                text     = "✕",
                                style    = MaterialTheme.typography.titleSmall.copy(color = Gold.copy(alpha = 0.7f)),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication        = null,
                                    ) { onShopExit(updatedState) },
                            )
                            Column(
                                modifier            = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                PanelHeading("Gothox Slothair's\nWeapon Shop")
                                FlavorText("A large, dirt-covered blacksmith stands behind the counter")
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        OrnateSeparator(Modifier.padding(horizontal = 24.dp))
                        Spacer(Modifier.height(10.dp))

                        challengeConfig?.getDisplayName()?.takeIf { it != "Normal" }?.let { badge ->
                            Text(
                                text  = "⚔ CHALLENGE: $badge ⚔",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    color    = EmberBright,
                                    fontSize = 11.sp,
                                ),
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }

                        Text(
                            text  = "You have  ${updatedState.character.gold} gold",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color      = GoldBright,
                                fontWeight = FontWeight.Bold,
                            ),
                            modifier = Modifier.padding(bottom = 4.dp),
                        )

                        message?.let { msg ->
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text      = msg,
                                style     = MaterialTheme.typography.bodyMedium.copy(color = EmberBright),
                                modifier  = Modifier.padding(horizontal = 24.dp),
                                textAlign = TextAlign.Center,
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        OrnateSeparator(Modifier.padding(horizontal = 24.dp))
                        Spacer(Modifier.height(12.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp)
                                .padding(bottom = 20.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            when (currentScreen) {
                                ShopScreen.MAIN -> ShopMainMenu(
                                    onBuyWeapon = { currentScreen = ShopScreen.WEAPONS },
                                    onBuyArmor  = { currentScreen = ShopScreen.ARMOR },
                                    onLeave     = { onShopExit(updatedState) },
                                )

                                ShopScreen.WEAPONS -> {
                                    ScrollBanner("Weapons for Sale")
                                    Spacer(Modifier.height(4.dp))
                                    Weapon.values().forEach { weapon ->
                                        val name = challengeModifiers.getWeaponDisplayName(
                                            weapon.displayName, challengeConfig, prestige,
                                        )
                                        val bonus = challengeModifiers.getEffectiveWeaponBonus(
                                            weapon.attackBonus, challengeConfig, prestige,
                                        )
                                        val equipped  = weapon.ordinal == updatedState.character.weaponStatus
                                        val canAfford = updatedState.character.gold >= weapon.basePrice

                                        ShopItemButton(
                                            name      = if (equipped) "$name (+${bonus} AP) [EQUIPPED]" else "$name (+${bonus} AP)",
                                            priceText = if (equipped) null else "${weapon.basePrice}g",
                                            equipped  = equipped,
                                            canAfford = canAfford,
                                            onClick   = {
                                                if (!equipped && canAfford) {
                                                    updatedState = updatedState.updateCharacter(
                                                        updatedState.character.copy(
                                                            gold         = updatedState.character.gold - weapon.basePrice,
                                                            weaponBonus  = bonus,
                                                            weaponStatus = weapon.ordinal,
                                                        )
                                                    )
                                                    message = "Purchased $name!"
                                                    currentScreen = ShopScreen.MAIN
                                                } else if (!canAfford) {
                                                    message = "Not enough gold"
                                                }
                                            },
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    MedievalButton(
                                        onClick = { currentScreen = ShopScreen.MAIN },
                                        variant = MedievalButtonVariant.Ember,
                                    ) {
                                        Text("Back", style = MaterialTheme.typography.titleMedium)
                                    }
                                }

                                ShopScreen.ARMOR -> {
                                    ScrollBanner("Armor for Sale")
                                    Spacer(Modifier.height(4.dp))
                                    Armor.values().forEach { armor ->
                                        val name = challengeModifiers.getArmorDisplayName(
                                            armor.displayName, challengeConfig, prestige,
                                        )
                                        val bonus = challengeModifiers.getEffectiveArmorBonus(
                                            armor.defenseBonus, challengeConfig, prestige,
                                        )
                                        val equipped  = armor.ordinal == updatedState.character.armorStatus
                                        val canAfford = updatedState.character.gold >= armor.basePrice

                                        ShopItemButton(
                                            name      = if (equipped) "$name (+${bonus} DP) [EQUIPPED]" else "$name (+${bonus} DP)",
                                            priceText = if (equipped) null else "${armor.basePrice}g",
                                            equipped  = equipped,
                                            canAfford = canAfford,
                                            onClick   = {
                                                if (!equipped && canAfford) {
                                                    updatedState = updatedState.updateCharacter(
                                                        updatedState.character.copy(
                                                            gold        = updatedState.character.gold - armor.basePrice,
                                                            armorBonus  = bonus,
                                                            armorStatus = armor.ordinal,
                                                        )
                                                    )
                                                    message = "Purchased $name!"
                                                    currentScreen = ShopScreen.MAIN
                                                } else if (!canAfford) {
                                                    message = "Not enough gold"
                                                }
                                            },
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    MedievalButton(
                                        onClick = { currentScreen = ShopScreen.MAIN },
                                        variant = MedievalButtonVariant.Ember,
                                    ) {
                                        Text("Back", style = MaterialTheme.typography.titleMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShopMainMenu(
    onBuyWeapon: () -> Unit,
    onBuyArmor: () -> Unit,
    onLeave: () -> Unit,
) {
    MedievalButton(onClick = onBuyWeapon) {
        Text("1. Buy a Weapon", style = MaterialTheme.typography.titleMedium)
    }
    MedievalButton(onClick = onBuyArmor) {
        Text("2. Buy Armor", style = MaterialTheme.typography.titleMedium)
    }
    OrnateSeparator()
    MedievalButton(onClick = onLeave, variant = MedievalButtonVariant.Ember) {
        Text("0. Leave the store", style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun ShopItemButton(
    name: String,
    priceText: String?,
    equipped: Boolean,
    canAfford: Boolean,
    onClick: () -> Unit,
) {
    val variant = when {
        equipped   -> MedievalButtonVariant.Equipped
        !canAfford -> MedievalButtonVariant.Disabled
        else       -> MedievalButtonVariant.Gold
    }

    MedievalButton(onClick = onClick, variant = variant) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically,
        ) {
            Text(
                text     = name,
                style    = MaterialTheme.typography.titleMedium.copy(
                    color      = when {
                        equipped   -> Parchment
                        canAfford  -> Gold
                        else       -> TextDim
                    },
                    fontWeight = if (equipped) FontWeight.Bold else FontWeight.Normal,
                ),
                modifier = Modifier.weight(1f),
            )
            priceText?.let {
                Text(
                    text  = it,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color      = if (canAfford) GoldBright else TextDim,
                        fontWeight = FontWeight.Bold,
                    ),
                    modifier = Modifier.padding(start = 12.dp),
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
