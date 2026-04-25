package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.Item
import com.greenopal.zargon.data.models.ItemType
import com.greenopal.zargon.data.models.PrestigeBonus
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.GameTitle
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalButtonVariant
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.OrnateSeparator
import com.greenopal.zargon.ui.theme.Ember
import com.greenopal.zargon.ui.theme.EmberBright
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.GoldBright
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.ParchmentDim
import com.greenopal.zargon.ui.theme.XpGreenBright
import kotlinx.coroutines.delay

@Composable
fun VictoryScreen(
    finalGameState: GameState,
    onReturnToTitle: () -> Unit,
    onReturnToGEF: (GameState) -> Unit,
    onChallengeComplete: ((com.greenopal.zargon.data.models.ChallengeResult) -> Unit)? = null,
    earnedBonus: PrestigeBonus? = null,
    modifier: Modifier = Modifier
) {
    var showContent by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        delay(500)
        showContent = true

        finalGameState.challengeConfig?.let { config ->
            val challengeStartTime = finalGameState.challengeStartTime ?: System.currentTimeMillis()
            val timeElapsed = System.currentTimeMillis() - challengeStartTime - finalGameState.totalPauseTime

            val result = com.greenopal.zargon.data.models.ChallengeResult(
                challengeId = config.getChallengeId(),
                completedAt = System.currentTimeMillis(),
                finalStats  = com.greenopal.zargon.data.models.ChallengeCompletionStats(
                    finalLevel       = finalGameState.character.level,
                    totalGoldEarned  = finalGameState.character.gold,
                    monstersDefeated = finalGameState.monstersDefeated,
                    deathCount       = finalGameState.deathCount,
                ),
                timeElapsedMs = timeElapsed,
            )
            onChallengeComplete?.invoke(result)
        }
    }

    DungeonBackground {
        if (showContent) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Spacer(Modifier.height(32.dp))

                GameTitle("VICTORY!", fontSize = 40.sp)

                Spacer(Modifier.height(16.dp))

                finalGameState.challengeConfig?.let { config ->
                    MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier            = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text  = "Challenge Completed!",
                                style = MaterialTheme.typography.headlineSmall.copy(color = GoldBright),
                            )
                            Text(
                                text  = config.getDisplayName(),
                                style = MaterialTheme.typography.bodyMedium.copy(color = Parchment),
                            )
                            if (finalGameState.challengeStartTime != null) {
                                val elapsed = (System.currentTimeMillis() - finalGameState.challengeStartTime - finalGameState.totalPauseTime) / 1000
                                Text(
                                    text  = "Time: ${elapsed / 60}m ${elapsed % 60}s",
                                    style = MaterialTheme.typography.bodyLarge.copy(color = Gold),
                                )
                            }
                            if (earnedBonus != null) {
                                OrnateSeparator()
                                Text(
                                    text  = "Bonus Unlocked: ${earnedBonus.displayName}",
                                    style = MaterialTheme.typography.headlineSmall.copy(color = EmberBright),
                                )
                                Text(
                                    text  = earnedBonus.description,
                                    style = MaterialTheme.typography.bodyMedium.copy(color = ParchmentDim),
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                }

                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text      = "You have defeated ZARGON\nand saved the land of GEF!",
                            style     = MaterialTheme.typography.headlineMedium.copy(color = GoldBright),
                            textAlign = TextAlign.Center,
                        )

                        OrnateSeparator()

                        Column(
                            modifier            = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text("Final Statistics", style = MaterialTheme.typography.titleSmall.copy(color = Gold))
                            Text("Level: ${finalGameState.character.level}", style = MaterialTheme.typography.bodyLarge.copy(color = Parchment))
                            Text("Experience: ${finalGameState.character.experience}", style = MaterialTheme.typography.bodyLarge.copy(color = Parchment))
                            Text("Gold: ${finalGameState.character.gold}g", style = MaterialTheme.typography.bodyLarge.copy(color = Parchment))
                            Text("Attack Power: ${finalGameState.character.baseAP + finalGameState.character.weaponBonus}", style = MaterialTheme.typography.bodyLarge.copy(color = Parchment))
                            Text("Defense: ${finalGameState.character.baseDP + finalGameState.character.armorBonus}", style = MaterialTheme.typography.bodyLarge.copy(color = Parchment))
                        }

                        OrnateSeparator()

                        Text(
                            text      = "The evil has been vanquished.\nPeace returns to the realm.",
                            style     = MaterialTheme.typography.bodyMedium.copy(color = ParchmentDim),
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text      = "Original QBASIC Game (1998-1999)\nSnappahed Software 98",
                            style     = MaterialTheme.typography.labelSmall.copy(color = ParchmentDim),
                            textAlign = TextAlign.Center,
                        )

                        MedievalButton(
                            onClick = {
                                val trophy = Item(
                                    name        = "Zargon",
                                    description = "Trophy of your victory over the evil overlord",
                                    type        = ItemType.KEY_ITEM,
                                )
                                val updatedState = finalGameState
                                    .copy(worldX = 2, worldY = 4, characterX = 10, characterY = 8)
                                    .addItem(trophy)
                                onReturnToGEF(updatedState)
                            },
                        ) {
                            Text("Return to GEF", style = MaterialTheme.typography.titleMedium.copy(color = XpGreenBright))
                        }

                        MedievalButton(
                            variant = MedievalButtonVariant.Ember,
                            onClick = onReturnToTitle,
                        ) {
                            Text("Return to Title", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

