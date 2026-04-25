package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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

@Composable
fun FountainScreen(
    gameState: GameState,
    onSaveGame: (GameState) -> Unit,
    onFountainExit: (GameState) -> Unit,
    modifier: Modifier = Modifier
) {
    var message          by remember { mutableStateOf<String?>(null) }
    var updatedGameState by remember { mutableStateOf(gameState) }
    var showDialog       by remember { mutableStateOf(false) }

    BackHandler { onFountainExit(updatedGameState) }

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
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        PanelHeading("The Fountain")
                        FlavorText("A beautiful crystal-clear fountain flows before you")

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
                            }
                        }

                        OrnateSeparator()

                        val needsHeal = updatedGameState.character.currentHP < updatedGameState.character.maxHP ||
                            updatedGameState.character.currentMP < updatedGameState.character.maxMP

                        MedievalButton(
                            onClick = {
                                val healed = updatedGameState.character.copy(
                                    currentHP = updatedGameState.character.maxHP,
                                    currentMP = updatedGameState.character.maxMP,
                                )
                                updatedGameState = updatedGameState.updateCharacter(healed)
                                message    = "You drink the cool refreshing water. You feel revitalized! HP and MP restored!"
                                showDialog = true
                            },
                            variant = if (needsHeal) MedievalButtonVariant.Gold else MedievalButtonVariant.Disabled,
                        ) {
                            Text("1. drink from fountain", style = MaterialTheme.typography.titleMedium)
                        }

                        MedievalButton(
                            onClick = {
                                val healed = updatedGameState.character.copy(
                                    currentHP = updatedGameState.character.maxHP,
                                    currentMP = updatedGameState.character.maxMP,
                                )
                                updatedGameState = updatedGameState.updateCharacter(healed)
                                message    = "You bathe in the fountain. The water cleanses your wounds! HP and MP restored!"
                                showDialog = true
                            },
                            variant = if (needsHeal) MedievalButtonVariant.Gold else MedievalButtonVariant.Disabled,
                        ) {
                            Text("2. bathe in fountain", style = MaterialTheme.typography.titleMedium)
                        }

                        MedievalButton(onClick = {
                            onSaveGame(updatedGameState)
                            message    = "Game saved!"
                            showDialog = true
                        }) {
                            Text("3. save your journey", style = MaterialTheme.typography.titleMedium)
                        }

                        MedievalButton(
                            variant = MedievalButtonVariant.Ember,
                            onClick = { onFountainExit(updatedGameState) },
                        ) {
                            Text("0. leave", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }

        if (showDialog && message != null) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Fountain", style = MaterialTheme.typography.headlineMedium.copy(color = GoldBright)) },
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
