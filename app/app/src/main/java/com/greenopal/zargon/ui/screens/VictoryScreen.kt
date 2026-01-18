package com.greenopal.zargon.ui.screens

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.greenopal.zargon.data.models.Item
import com.greenopal.zargon.data.models.ItemType
import com.greenopal.zargon.ui.theme.DarkStone
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.Parchment
import kotlinx.coroutines.delay

/**
 * Victory screen shown after defeating Zargon
 */
@Composable
fun VictoryScreen(
    finalGameState: GameState,
    onReturnToTitle: () -> Unit,
    onReturnToGEF: (GameState) -> Unit,
    onChallengeComplete: ((com.greenopal.zargon.data.models.ChallengeResult) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showText by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        showText = true

        finalGameState.challengeConfig?.let { config ->
            val challengeStartTime = finalGameState.challengeStartTime ?: System.currentTimeMillis()
            val timeElapsed = System.currentTimeMillis() - challengeStartTime - finalGameState.totalPauseTime

            val result = com.greenopal.zargon.data.models.ChallengeResult(
                challengeId = config.getChallengeId(),
                completedAt = System.currentTimeMillis(),
                finalStats = com.greenopal.zargon.data.models.ChallengeCompletionStats(
                    finalLevel = finalGameState.character.level,
                    totalGoldEarned = finalGameState.character.gold,
                    monstersDefeated = finalGameState.monstersDefeated,
                    deathCount = finalGameState.deathCount
                ),
                timeElapsedMs = timeElapsed
            )

            onChallengeComplete?.invoke(result)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (showText) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(3.dp, Color(0xFFFFD700)) // Gold border
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "VICTORY!",
                        style = MaterialTheme.typography.displayMedium,
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    finalGameState.challengeConfig?.let { config ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Parchment
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "CHALLENGE COMPLETED!",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Gold,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = config.getDisplayName(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DarkStone
                                )
                                if (config.timedChallenge != com.greenopal.zargon.data.models.TimedChallenge.NONE) {
                                    val challengeStartTime = finalGameState.challengeStartTime ?: System.currentTimeMillis()
                                    val timeElapsed = (System.currentTimeMillis() - challengeStartTime - finalGameState.totalPauseTime) / 1000
                                    val minutes = timeElapsed / 60
                                    val seconds = timeElapsed % 60
                                    Text(
                                        text = "Time: ${minutes}m ${seconds}s",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DarkStone,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Victory message
                    Text(
                        text = "You have defeated ZARGON\nand saved the land of GEF!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Final stats
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Final Statistics",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )

                            StatLine("Level", "${finalGameState.character.level}")
                            StatLine("Experience", "${finalGameState.character.experience}")
                            StatLine("Gold", "${finalGameState.character.gold}g")
                            StatLine("Attack Power", "${finalGameState.character.totalAP}")
                            StatLine("Defense", "${finalGameState.character.totalDefense}")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Ending message
                    Text(
                        text = "The evil has been vanquished.\nPeace returns to the realm.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Credits
                    Text(
                        text = "Original QBASIC Game (1998-1999)\nSnappahed Software 98",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                    Text(
                        text = "Android Port (2025)",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Return to GEF button
                    Button(
                        onClick = {
                            // Warp to healer location (Map 2,4 at coordinates 10,8)
                            // Add "Zargon" trophy item to inventory
                            val zargonTrophy = Item(
                                name = "Zargon",
                                description = "Trophy of your victory over the evil overlord",
                                type = ItemType.KEY_ITEM
                            )

                            val updatedState = finalGameState
                                .copy(
                                    worldX = 2,
                                    worldY = 4,
                                    characterX = 10,
                                    characterY = 8
                                )
                                .addItem(zargonTrophy)

                            onReturnToGEF(updatedState)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50) // Green for continue
                        )
                    ) {
                        Text(
                            text = "Return to GEF",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Return to title button
                    Button(
                        onClick = onReturnToTitle,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = "Return to Title",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSecondaryContainer
    )
}
