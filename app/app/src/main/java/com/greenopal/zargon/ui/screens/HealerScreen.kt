package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.FlavorText
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalButtonVariant
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.MedievalStatBar
import com.greenopal.zargon.ui.components.OrnateSeparator
import com.greenopal.zargon.ui.components.PanelHeading
import com.greenopal.zargon.ui.components.StatBarType
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.GoldBright
import com.greenopal.zargon.ui.theme.HpRedBright
import com.greenopal.zargon.ui.theme.MpBlueBright
import com.greenopal.zargon.ui.theme.PanelBg
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.ParchmentDim
import com.greenopal.zargon.ui.theme.TextDim

@Composable
fun HealerScreen(
    gameState: GameState,
    onSaveGame: (GameState) -> Unit,
    onHealerExit: (GameState) -> Unit,
    modifier: Modifier = Modifier
) {
    var message          by remember { mutableStateOf<String?>(null) }
    var updatedGameState by remember { mutableStateOf(gameState) }
    var showDialog       by remember { mutableStateOf(false) }

    BackHandler { onHealerExit(updatedGameState) }

    DungeonBackground {
        LazyColumn(
            modifier            = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(vertical = 24.dp),
        ) {
            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier            = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                        PanelHeading("The Healer")
                        FlavorText("hi mr. healer!")

                        OrnateSeparator()

                        MedievalPanel(
                            modifier       = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            showCornerGems = false,
                        ) {
                            Column(
                                modifier            = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("HP", style = MaterialTheme.typography.titleSmall.copy(color = HpRedBright))
                                    Text(
                                        "${updatedGameState.character.currentHP}/${updatedGameState.character.maxHP}",
                                        style = MaterialTheme.typography.bodyLarge.copy(color = HpRedBright),
                                    )
                                }
                                MedievalStatBar(updatedGameState.character.currentHP, updatedGameState.character.maxHP, StatBarType.HP)

                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("MP", style = MaterialTheme.typography.titleSmall.copy(color = MpBlueBright))
                                    Text(
                                        "${updatedGameState.character.currentMP}/${updatedGameState.character.maxMP}",
                                        style = MaterialTheme.typography.bodyLarge.copy(color = MpBlueBright),
                                    )
                                }
                                MedievalStatBar(updatedGameState.character.currentMP, updatedGameState.character.maxMP, StatBarType.MP)

                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text("Gold", style = MaterialTheme.typography.titleSmall.copy(color = Gold))
                                    Text(
                                        "${updatedGameState.character.gold}g",
                                        style = MaterialTheme.typography.bodyLarge.copy(color = GoldBright),
                                    )
                                }
                            }
                        }

                        OrnateSeparator()

                        FlavorText("what do you want?")

                        HealingOption(
                            label   = "1) healing  3gp",
                            enabled = updatedGameState.character.gold >= 3,
                            onClick = {
                                val newChar = updatedGameState.character.copy(
                                    currentHP = updatedGameState.character.maxHP,
                                    gold      = updatedGameState.character.gold - 3,
                                )
                                updatedGameState = updatedGameState.updateCharacter(newChar)
                                message    = "sure my son"
                                showDialog = true
                            }
                        )

                        HealingOption(
                            label   = "2) magic healing  3gp",
                            enabled = updatedGameState.character.gold >= 3,
                            onClick = {
                                val newChar = updatedGameState.character.copy(
                                    currentMP = updatedGameState.character.maxMP,
                                    gold      = updatedGameState.character.gold - 3,
                                )
                                updatedGameState = updatedGameState.updateCharacter(newChar)
                                message    = "sure my son"
                                showDialog = true
                            }
                        )

                        HealingOption(
                            label   = "3) complete rest  5gp",
                            enabled = updatedGameState.character.gold >= 5,
                            onClick = {
                                val newChar = updatedGameState.character.copy(
                                    currentHP = updatedGameState.character.maxHP,
                                    currentMP = updatedGameState.character.maxMP,
                                    gold      = updatedGameState.character.gold - 5,
                                )
                                updatedGameState = updatedGameState.updateCharacter(newChar)
                                message    = "sure my son"
                                showDialog = true
                            }
                        )

                        MedievalButton(onClick = {
                            onSaveGame(updatedGameState)
                            message    = "Game saved!"
                            showDialog = true
                        }) {
                            Text("4. shall i save your game?", style = MaterialTheme.typography.titleMedium)
                        }

                        MedievalButton(
                            variant = MedievalButtonVariant.Ember,
                            onClick = { onHealerExit(updatedGameState) },
                        ) {
                            Text("5. i've had enough of this guy", style = MaterialTheme.typography.titleMedium)
                        }
                        }
                        Text(
                            text     = "✕",
                            style    = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .clickable(
                                    indication        = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick           = { onHealerExit(updatedGameState) },
                                ),
                        )
                    }
                }
            }
        }

        if (showDialog && message != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Healer", style = MaterialTheme.typography.headlineMedium.copy(color = GoldBright)) },
                text  = { Text(message!!, style = MaterialTheme.typography.bodyMedium.copy(color = Parchment)) },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK", style = MaterialTheme.typography.titleMedium.copy(color = Gold))
                    }
                },
                containerColor = PanelBg,
            )
        }
    }
}

@Composable
private fun HealingOption(label: String, enabled: Boolean, onClick: () -> Unit) {
    MedievalButton(
        onClick = onClick,
        variant = if (enabled) MedievalButtonVariant.Gold else MedievalButtonVariant.Disabled,
    ) {
        Text(label, style = MaterialTheme.typography.titleMedium)
    }
}
