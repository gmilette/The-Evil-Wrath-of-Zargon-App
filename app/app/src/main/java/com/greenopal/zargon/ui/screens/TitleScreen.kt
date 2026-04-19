package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.greenopal.zargon.data.repository.SaveSlotInfo
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.GameTitle
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalButtonVariant
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.ScrollBanner
import com.greenopal.zargon.ui.theme.Ember
import com.greenopal.zargon.ui.theme.EmberBright
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.GoldBright
import com.greenopal.zargon.ui.theme.PanelBg
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.ParchmentDim

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TitleScreen(
    saveSlots: List<SaveSlotInfo>,
    onNewGame: (Int) -> Unit,
    onContinue: (Int) -> Unit,
    onDeleteSave: (Int) -> Unit,
    onRestartChallenge: (Int) -> Unit,
    onViewProgress: () -> Unit,
    modifier: Modifier = Modifier
) {
    var slotToDelete by remember { mutableStateOf<Int?>(null) }

    DungeonBackground {
        LazyColumn(
            modifier            = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(vertical = 32.dp),
        ) {
            item {
                GameTitle("THE EVIL WRATH\nOF ZARGON")
            }

            item {
                ScrollBanner("Select Save Slot")
            }

            items(saveSlots) { slot ->
                SaveSlotRow(
                    slot               = slot,
                    onClick            = { if (slot.exists) onContinue(slot.slot) else onNewGame(slot.slot) },
                    onLongClick        = { if (slot.exists) slotToDelete = slot.slot },
                    onRestartChallenge = { onRestartChallenge(slot.slot) },
                )
            }

            item {
                MedievalButton(
                    onClick  = onViewProgress,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("View Challenge Progress", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    slotToDelete?.let { slot ->
        AlertDialog(
            onDismissRequest = { slotToDelete = null },
            title = {
                Text(
                    "Delete Save Slot?",
                    style = MaterialTheme.typography.headlineSmall.copy(color = GoldBright),
                )
            },
            text = {
                Text(
                    "Are you sure you want to delete save slot $slot? This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Parchment),
                )
            },
            confirmButton = {
                TextButton(onClick = { onDeleteSave(slot); slotToDelete = null }) {
                    Text("Delete", style = MaterialTheme.typography.titleMedium.copy(color = Ember))
                }
            },
            dismissButton = {
                TextButton(onClick = { slotToDelete = null }) {
                    Text("Cancel", style = MaterialTheme.typography.titleMedium.copy(color = Gold))
                }
            },
            containerColor = PanelBg,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SaveSlotRow(
    slot: SaveSlotInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRestartChallenge: () -> Unit,
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        MedievalPanel(
            modifier = Modifier
                .weight(1f)
                .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        ) {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (slot.exists) {
                    Text(
                        text  = "Slot ${slot.slot} — Continue",
                        style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                    )
                    slot.gameState?.let { state ->
                        Text(
                            text  = "Lv${state.character.level} · ${state.character.gold}g",
                            style = MaterialTheme.typography.bodySmall.copy(color = Parchment),
                        )
                        state.challengeConfig?.let { config ->
                            Text(
                                text  = config.getDisplayName(),
                                style = MaterialTheme.typography.labelSmall.copy(color = EmberBright),
                            )
                        }
                    }
                } else {
                    Text(
                        text  = "Slot ${slot.slot} — New Game",
                        style = MaterialTheme.typography.titleMedium.copy(color = Gold),
                    )
                    Text(
                        text  = "Empty",
                        style = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim),
                    )
                }
            }
        }

        MedievalButton(
            onClick  = onRestartChallenge,
            variant  = MedievalButtonVariant.Ember,
            modifier = Modifier,
        ) {
            Text("Restart", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
