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
import com.greenopal.zargon.ui.theme.EmberOrange

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
                            text = "HP: ${updatedGameState.character.currentDP}/${updatedGameState.character.maxDP}",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "MP: ${updatedGameState.character.currentMP}/${updatedGameState.character.maxMP}",
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Gold: ${updatedGameState.character.gold}",
                            color = MaterialTheme.colorScheme.primary,
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

                if (updatedGameState.challengeConfig?.isPermadeath != true) {
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
                        Text("4. shall i save your game?")
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = "PERMANENT DEATH MODE - No saves available",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmberOrange,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                Button(
                    onClick = { onHealerExit(updatedGameState) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text(if (updatedGameState.challengeConfig?.isPermadeath == true) "4. i've had enough of this guy" else "5. i've had enough of this guy")
                }
            }

                IconButton(
                    onClick = { onHealerExit(updatedGameState) },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Exit healer",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
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
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
        ),
        border = if (!enabled) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
    ) {
        Text(
            text = "$number) $service  ${cost}gp"
        )
    }
}
