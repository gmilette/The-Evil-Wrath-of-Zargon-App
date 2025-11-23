package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.repository.SaveSlotInfo

/**
 * Title screen with New Game and Continue options
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TitleScreen(
    saveSlots: List<SaveSlotInfo>,
    onNewGame: (Int) -> Unit,
    onContinue: (Int) -> Unit,
    onDeleteSave: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var slotToDelete by remember { mutableStateOf<Int?>(null) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
        ) {
            Column(
                modifier = Modifier
                    .padding(32.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "THE EVIL WRATH\nOF ZARGON",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Select Save Slot:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.secondary
                )

                saveSlots.forEach { slot ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .combinedClickable(
                                onClick = {
                                    if (slot.exists) {
                                        onContinue(slot.slot)
                                    } else {
                                        onNewGame(slot.slot)
                                    }
                                },
                                onLongClick = {
                                    if (slot.exists) {
                                        slotToDelete = slot.slot
                                    }
                                }
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (slot.exists) {
                                MaterialTheme.colorScheme.secondary
                            } else {
                                MaterialTheme.colorScheme.primary
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (slot.exists) {
                                Text(
                                    text = "Slot ${slot.slot} - Continue",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                slot.gameState?.let { state ->
                                    Text(
                                        text = "Level ${state.character.level} | ${state.character.gold} gold | Map ${state.worldX}${state.worldY}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            } else {
                                Text(
                                    text = "Slot ${slot.slot} - New Game",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "Empty",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    slotToDelete?.let { slot ->
        AlertDialog(
            onDismissRequest = { slotToDelete = null },
            title = {
                Text(text = "Delete Save Slot?")
            },
            text = {
                Text(text = "Are you sure you want to delete save slot $slot? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSave(slot)
                        slotToDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { slotToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
