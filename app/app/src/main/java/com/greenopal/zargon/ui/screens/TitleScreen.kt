package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenopal.zargon.R
import com.greenopal.zargon.data.repository.SaveSlotInfo
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.DarkStone
import com.greenopal.zargon.ui.theme.MidStone
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.EmberOrange

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

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "THE EVIL WRATH\nOF ZARGON",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    letterSpacing = 2.sp
                ),
                color = Gold,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Select Save Slot:",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 20.sp
                ),
                color = Parchment,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            saveSlots.forEach { slot ->
                SaveSlotButton(
                    slot = slot,
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
                    },
                    onRestartChallenge = { onRestartChallenge(slot.slot) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onViewProgress,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = DarkStone
                )
            ) {
                Text(
                    text = "View Challenge Progress",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp
                    ),
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    slotToDelete?.let { slot ->
        AlertDialog(
            onDismissRequest = { slotToDelete = null },
            title = {
                Text(
                    text = "Delete Save Slot?",
                    color = Gold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete save slot $slot? This action cannot be undone.",
                    color = Parchment
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteSave(slot)
                        slotToDelete = null
                    }
                ) {
                    Text("Delete", color = EmberOrange)
                }
            },
            dismissButton = {
                TextButton(onClick = { slotToDelete = null }) {
                    Text("Cancel", color = Parchment)
                }
            },
            containerColor = DarkStone
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SaveSlotButton(
    slot: SaveSlotInfo,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onRestartChallenge: () -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(95.dp)
                .background(
                    color = MidStone,
                    shape = RoundedCornerShape(8.dp)
                )
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (slot.exists) {
                    Text(
                        text = "Slot ${slot.slot} - Continue",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp
                        ),
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    slot.gameState?.let { state ->
                        Text(
                            text = "Level ${state.character.level} | ${state.character.gold} gold",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 11.sp
                            ),
                            color = Parchment
                        )
                        state.challengeConfig?.let { config ->
                            Text(
                                text = config.getDisplayName(),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 10.sp
                                ),
                                color = EmberOrange,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Slot ${slot.slot} - New Game",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 16.sp
                        ),
                        color = Gold,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Empty",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 12.sp
                        ),
                        color = MidStone
                    )
                }
            }
        }

        Button(
            onClick = onRestartChallenge,
            modifier = Modifier
                .height(95.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Parchment,
                contentColor = DarkStone
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "Restart",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp
                ),
                fontWeight = FontWeight.Bold
            )
        }
    }
}
