package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.greenopal.zargon.data.models.GameState

/**
 * Fountain screen for restoring HP/MP and saving
 * Based on QBASIC fountain procedure (ZARGON.BAS:1487-1540)
 */
@Composable
fun FountainScreen(
    gameState: GameState,
    onSaveGame: (GameState) -> Unit,
    onFountainExit: (GameState) -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf<String?>(null) }
    var updatedGameState by remember { mutableStateOf(gameState) }
    var showMessageDialog by remember { mutableStateOf(false) }

    // Handle Android back button
    BackHandler {
        onFountainExit(updatedGameState)
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
                    onClick = { onFountainExit(updatedGameState) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit fountain",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                // Header
                Text(
                    text = "The Fountain",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "A beautiful crystal-clear fountain flows before you",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                // Stats display
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Current Stats:",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = "HP: ${updatedGameState.character.currentHP}/${updatedGameState.character.maxHP}",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "MP: ${updatedGameState.character.currentMP}/${updatedGameState.character.maxMP}",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Fountain action buttons
                Button(
                    onClick = {
                        val healedCharacter = updatedGameState.character.copy(
                            currentHP = updatedGameState.character.maxHP,
                            currentMP = updatedGameState.character.maxMP
                        )
                        updatedGameState = updatedGameState.updateCharacter(healedCharacter)
                        message = "You drink the cool refreshing water. You feel revitalized! HP and MP restored!"
                        showMessageDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = updatedGameState.character.currentHP < updatedGameState.character.maxHP ||
                            updatedGameState.character.currentMP < updatedGameState.character.maxMP,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Text("1. drink from fountain")
                }

                Button(
                    onClick = {
                        val healedCharacter = updatedGameState.character.copy(
                            currentHP = updatedGameState.character.maxHP,
                            currentMP = updatedGameState.character.maxMP
                        )
                        updatedGameState = updatedGameState.updateCharacter(healedCharacter)
                        message = "You bathe in the fountain. The water cleanses your wounds! HP and MP restored!"
                        showMessageDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = updatedGameState.character.currentHP < updatedGameState.character.maxHP ||
                            updatedGameState.character.currentMP < updatedGameState.character.maxMP,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )
                ) {
                    Text("2. bathe in fountain")
                }

                Button(
                    onClick = {
                        onSaveGame(updatedGameState)
                        message = "Game saved!"
                        showMessageDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text("3. save your journey")
                }

                Button(
                    onClick = { onFountainExit(updatedGameState) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text("0. leave")
                }
            }
        }
        }

        // Message dialog popup
        if (showMessageDialog && message != null) {
            AlertDialog(
                onDismissRequest = { showMessageDialog = false },
                title = {
                    Text(
                        text = "Fountain",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    Text(
                        text = message!!,
                        style = MaterialTheme.typography.bodyLarge
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showMessageDialog = false }) {
                        Text("OK")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface
            )
        }
    }
}
