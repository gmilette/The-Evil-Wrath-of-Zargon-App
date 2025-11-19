package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.greenopal.zargon.data.models.GameState

/**
 * Healer screen for restoring HP/MP
 * Based on QBASIC healr procedure (ZARGON.BAS:1610-1665)
 */
@Composable
fun HealerScreen(
    gameState: GameState,
    onSaveGame: (GameState) -> Unit,
    onHealerExit: (GameState) -> Unit,
    modifier: Modifier = Modifier
) {
    var message by remember { mutableStateOf<String?>(null) }
    var updatedGameState by remember { mutableStateOf(gameState) }
    var showMessageDialog by remember { mutableStateOf(false) }

    // Handle Android back button
    BackHandler {
        onHealerExit(updatedGameState)
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
                    text = "The Healer",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "hi mr. healer!",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

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
                        Text(
                            text = "Gold: ${updatedGameState.character.gold}",
                            color = Color(0xFFFFD700),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "what do you want?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                // Service buttons
                HealingOption(
                    number = 1,
                    service = "healing",
                    cost = 3,
                    enabled = updatedGameState.character.gold >= 3 &&
                            updatedGameState.character.currentDP < updatedGameState.character.maxDP,
                    onClick = {
                        val newChar = updatedGameState.character.copy(
                            currentDP = updatedGameState.character.maxDP,
                            gold = updatedGameState.character.gold - 3
                        )
                        updatedGameState = updatedGameState.updateCharacter(newChar)
                        message = "sure my son"
                        showMessageDialog = true
                    }
                )

                HealingOption(
                    number = 2,
                    service = "magic healing",
                    cost = 3,
                    enabled = updatedGameState.character.gold >= 3 &&
                            updatedGameState.character.currentMP < updatedGameState.character.maxMP,
                    onClick = {
                        val newChar = updatedGameState.character.copy(
                            currentMP = updatedGameState.character.maxMP,
                            gold = updatedGameState.character.gold - 3
                        )
                        updatedGameState = updatedGameState.updateCharacter(newChar)
                        message = "sure my son"
                        showMessageDialog = true
                    }
                )

                HealingOption(
                    number = 3,
                    service = "complete rest",
                    cost = 5,
                    enabled = updatedGameState.character.gold >= 5 &&
                            (updatedGameState.character.currentDP < updatedGameState.character.maxDP ||
                                    updatedGameState.character.currentMP < updatedGameState.character.maxMP),
                    onClick = {
                        val newChar = updatedGameState.character.copy(
                            currentDP = updatedGameState.character.maxDP,
                            currentMP = updatedGameState.character.maxMP,
                            gold = updatedGameState.character.gold - 5
                        )
                        updatedGameState = updatedGameState.updateCharacter(newChar)
                        message = "sure my son"
                        showMessageDialog = true
                    }
                )

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
                    Text("4. shall i save your game?")
                }

                Button(
                    onClick = { onHealerExit(updatedGameState) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF666666), // Medium gray with white text
                        contentColor = Color.White
                    )
                ) {
                    Text("5. i've had enough of this guy")
                }
            }
        }

        // Message dialog popup
        if (showMessageDialog && message != null) {
            AlertDialog(
                onDismissRequest = { showMessageDialog = false },
                title = {
                    Text(
                        text = "Healer",
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

@Composable
private fun HealingOption(
    number: Int,
    service: String,
    cost: Int,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) Color(0xFF1976D2) else Color(0xFF424242),
            contentColor = Color.White,
            disabledContainerColor = Color(0xFF424242),
            disabledContentColor = Color(0xFF888888)
        )
    ) {
        Text(
            text = "$number) $service  ${cost}gp",
            color = if (enabled) Color.White else Color(0xFF888888)
        )
    }
}
