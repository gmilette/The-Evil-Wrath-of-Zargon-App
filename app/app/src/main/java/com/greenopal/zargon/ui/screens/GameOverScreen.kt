package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalButtonVariant
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.OrnateSeparator
import com.greenopal.zargon.ui.components.ScreenHeading
import com.greenopal.zargon.ui.theme.Ember
import com.greenopal.zargon.ui.theme.EmberBright
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.HpRed
import com.greenopal.zargon.ui.theme.HpRedBright
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.ParchmentDim

@Composable
fun GameOverScreen(
    finalGameState: GameState,
    onReturnToTitle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPermanentDeath = finalGameState.challengeConfig?.isPermadeath == true
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    DungeonBackground {
        Column(
            modifier            = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            MedievalPanel(
                modifier       = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(32.dp),
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        text  = "GAME OVER",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = HpRedBright,
                        ),
                        textAlign = TextAlign.Center,
                    )

                    if (isPermanentDeath) {
                        Text(
                            text  = "PERMANENT DEATH",
                            style = MaterialTheme.typography.headlineMedium.copy(color = EmberBright),
                            textAlign = TextAlign.Center,
                        )
                    }

                    OrnateSeparator()

                    Text(
                        text  = if (isPermanentDeath) {
                            "You have been defeated...\nNo saves in Permanent Death mode."
                        } else {
                            "You have been defeated..."
                        },
                        style     = MaterialTheme.typography.bodyMedium.copy(color = Parchment),
                        textAlign = TextAlign.Center,
                    )

                    Text(
                        text      = "The forces of Zargon have proven too powerful.",
                        style     = MaterialTheme.typography.bodyMedium.copy(color = ParchmentDim),
                        textAlign = TextAlign.Center,
                    )

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
                            Text(
                                text  = "Final Stats",
                                style = MaterialTheme.typography.titleSmall.copy(color = Gold),
                            )
                            Text("Level: ${finalGameState.character.level}", style = MaterialTheme.typography.bodyLarge.copy(color = Parchment))
                            Text("Gold Earned: ${finalGameState.character.gold}", style = MaterialTheme.typography.bodyLarge.copy(color = Parchment))
                            Text("Experience: ${finalGameState.character.experience}", style = MaterialTheme.typography.bodyLarge.copy(color = Parchment))
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    MedievalButton(onClick = onReturnToTitle) {
                        Text("Return to Title", style = MaterialTheme.typography.titleMedium)
                    }

                    Text(
                        text      = "Better luck next time, adventurer.",
                        style     = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
