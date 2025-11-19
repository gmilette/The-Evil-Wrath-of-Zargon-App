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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.Dialog
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.NpcType
import com.greenopal.zargon.data.models.StoryAction

/**
 * Dialog screen for NPC interactions
 * Based on QBASIC hut dialog system (ZARGON.BAS:1860+)
 */
@Composable
fun DialogScreen(
    npcType: NpcType,
    dialog: Dialog,
    gameState: GameState,
    onDialogEnd: (GameState, StoryAction?) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentAnswer by remember { mutableStateOf<String?>(null) }
    var selectedAction by remember { mutableStateOf<StoryAction?>(null) }

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
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // NPC name
                Text(
                    text = npcType.displayName,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Answer display (if question was asked)
                if (currentAnswer != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Text(
                            text = currentAnswer!!,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Continue button
                    Button(
                        onClick = { currentAnswer = null },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text("Continue...")
                    }
                } else {
                    // Question buttons
                    if (dialog.question1.isNotEmpty()) {
                        Button(
                            onClick = {
                                currentAnswer = dialog.answer1
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "1. ${dialog.question1}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (dialog.question2.isNotEmpty()) {
                        Button(
                            onClick = {
                                currentAnswer = dialog.answer2
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "2. ${dialog.question2}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    if (dialog.question3.isNotEmpty()) {
                        Button(
                            onClick = {
                                currentAnswer = dialog.answer3
                                selectedAction = dialog.storyAction
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dialog.storyAction != null) {
                                    MaterialTheme.colorScheme.tertiary
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            )
                        ) {
                            Text(
                                text = "3. ${dialog.question3}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Leave button
                    Button(
                        onClick = {
                            onDialogEnd(gameState, selectedAction)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray
                        )
                    ) {
                        Text("0. Leave")
                    }
                }
            }
        }
    }
}
