package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.Item
import com.greenopal.zargon.data.models.ItemType
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.MedievalStatBar
import com.greenopal.zargon.ui.components.OrnateSeparator
import com.greenopal.zargon.ui.components.ScreenHeading
import com.greenopal.zargon.ui.components.ScrollBanner
import com.greenopal.zargon.ui.components.StatBarType
import com.greenopal.zargon.ui.theme.Ember
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.GoldBright
import com.greenopal.zargon.ui.theme.HpRedBright
import com.greenopal.zargon.ui.theme.MpBlueBright
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.ParchmentDim
import com.greenopal.zargon.ui.theme.XpGreenBright

@Composable
fun StatsScreen(
    gameState: GameState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    DungeonBackground {
        LazyColumn(
            modifier            = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding      = PaddingValues(vertical = 24.dp),
        ) {
            item {
                ScreenHeading("Character Status")
            }

            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text  = "JOE — Level ${gameState.character.level}",
                            style = MaterialTheme.typography.headlineMedium.copy(color = GoldBright),
                        )

                        OrnateSeparator()

                        StatRow("HP") {
                            Text(
                                "${gameState.character.currentHP} / ${gameState.character.maxHP}",
                                style = MaterialTheme.typography.bodyLarge.copy(color = HpRedBright),
                            )
                        }
                        MedievalStatBar(
                            current = gameState.character.currentHP,
                            max     = gameState.character.maxHP,
                            type    = StatBarType.HP,
                        )

                        StatRow("MP") {
                            Text(
                                "${gameState.character.currentMP} / ${gameState.character.maxMP}",
                                style = MaterialTheme.typography.bodyLarge.copy(color = MpBlueBright),
                            )
                        }
                        MedievalStatBar(
                            current = gameState.character.currentMP,
                            max     = gameState.character.maxMP,
                            type    = StatBarType.MP,
                        )

                        StatRow("XP") {
                            Text(
                                "${gameState.character.experience} / ${gameState.nextLevelXP}",
                                style = MaterialTheme.typography.bodyLarge.copy(color = XpGreenBright),
                            )
                        }
                        MedievalStatBar(
                            current = gameState.character.experience,
                            max     = gameState.nextLevelXP,
                            type    = StatBarType.XP,
                        )

                        OrnateSeparator()

                        StatRow("Attack Power") {
                            Text(
                                "${gameState.character.baseAP + gameState.character.weaponBonus}",
                                style = MaterialTheme.typography.bodyLarge.copy(color = Parchment),
                            )
                        }
                        StatRow("Defense Power") {
                            Text(
                                "${gameState.character.baseDP + gameState.character.armorBonus}",
                                style = MaterialTheme.typography.bodyLarge.copy(color = Parchment),
                            )
                        }
                        StatRow("Gold") {
                            Text(
                                "${gameState.character.gold}g",
                                style = MaterialTheme.typography.bodyLarge.copy(color = Gold, fontWeight = FontWeight.Bold),
                            )
                        }

                        OrnateSeparator()

                        StatRow("Weapon") {
                            Text(
                                getWeaponName(gameState.character.weaponStatus),
                                style = MaterialTheme.typography.bodyLarge.copy(color = ParchmentDim),
                            )
                        }
                        StatRow("Armor") {
                            Text(
                                getArmorName(gameState.character.armorStatus),
                                style = MaterialTheme.typography.bodyLarge.copy(color = ParchmentDim),
                            )
                        }

                        OrnateSeparator()

                        StatRow("Story") {
                            Text(
                                String.format("%.1f", gameState.storyStatus),
                                style = MaterialTheme.typography.bodyLarge.copy(color = ParchmentDim),
                            )
                        }
                        StatRow("Location") {
                            Text(
                                "World ${gameState.worldX}-${gameState.worldY}",
                                style = MaterialTheme.typography.bodyLarge.copy(color = ParchmentDim),
                            )
                        }
                    }
                }
            }

            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ScrollBanner("Inventory (${gameState.inventory.size}/10)")

                        Spacer(Modifier.height(4.dp))

                        if (gameState.inventory.isEmpty()) {
                            Text(
                                text      = "No items",
                                style     = MaterialTheme.typography.bodyMedium.copy(color = ParchmentDim),
                                modifier  = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        } else {
                            gameState.inventory.forEach { item ->
                                ItemEntry(item)
                            }
                        }
                    }
                }
            }

            item {
                MedievalButton(onClick = onBack) {
                    Text("Back to Menu", style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: @Composable () -> Unit) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Text(
            text  = label,
            style = MaterialTheme.typography.titleSmall.copy(color = ParchmentDim),
        )
        value()
    }
}

@Composable
private fun ItemEntry(item: Item) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text  = item.name.uppercase(),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = when (item.type) {
                    ItemType.KEY_ITEM -> Ember
                    ItemType.WEAPON   -> HpRedBright
                    ItemType.ARMOR    -> Gold
                    else              -> Parchment
                },
                fontWeight = FontWeight.Bold,
            ),
        )
        if (item.description.isNotEmpty()) {
            Text(
                text  = item.description,
                style = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim),
            )
        }
    }
}
