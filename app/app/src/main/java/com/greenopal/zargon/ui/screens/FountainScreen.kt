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
            .background(Color.Black),
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
                        containerColor = Color(0xFF2A2A2A)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Current Stats:",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "HP: ${updatedGameState.character.currentDP}/${updatedGameState.character.maxDP}",
                            color = Color.White
                        )
                        Text(
                            text = "MP: ${updatedGameState.character.currentMP}/${updatedGameState.character.maxMP}",
                            color = Color.White
                        )
                    }
                }

                // Fountain action buttons
                Button(
                    onClick = {
                        val healedCharacter = updatedGameState.character.copy(
                            currentDP = updatedGameState.character.maxDP,
                            currentMP = updatedGameState.character.maxMP
                        )
                        updatedGameState = updatedGameState.updateCharacter(healedCharacter)
                        message = "You drink the cool refreshing water. You feel revitalized! HP and MP restored!"
                        showMessageDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = updatedGameState.character.currentDP < updatedGameState.character.maxDP ||
                            updatedGameState.character.currentMP < updatedGameState.character.maxMP,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2), // Blue
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF424242),
                        disabledContentColor = Color(0xFF888888)
                    )
                ) {
                    Text("1. drink from fountain")
                }

                Button(
                    onClick = {
                        val healedCharacter = updatedGameState.character.copy(
                            currentDP = updatedGameState.character.maxDP,
                            currentMP = updatedGameState.character.maxMP
                        )
                        updatedGameState = updatedGameState.updateCharacter(healedCharacter)
                        message = "You bathe in the fountain. The water cleanses your wounds! HP and MP restored!"
                        showMessageDialog = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = updatedGameState.character.currentDP < updatedGameState.character.maxDP ||
                            updatedGameState.character.currentMP < updatedGameState.character.maxMP,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2), // Blue
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFF424242),
                        disabledContentColor = Color(0xFF888888)
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
                        containerColor = Color(0xFF4CAF50), // Bright green
                        contentColor = Color.White
                    )
                ) {
                    Text("3. save your journey")
                }

                Button(
                    onClick = { onFountainExit(updatedGameState) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF666666), // Medium gray
                        contentColor = Color.White
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
