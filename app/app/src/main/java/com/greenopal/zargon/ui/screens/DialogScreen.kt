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
import androidx.compose.runtime.LaunchedEffect
import com.greenopal.zargon.data.models.Dialog
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.NpcType
import com.greenopal.zargon.data.models.StoryAction
import com.greenopal.zargon.domain.story.NpcDialogProvider

/**
 * Dialog screen for NPC interactions
 * Based on QBASIC hut dialog system (ZARGON.BAS:1860+)
 */
@Composable
fun DialogScreen(
    npcType: NpcType,
    dialogProvider: NpcDialogProvider,
    gameState: GameState,
    onDialogEnd: (GameState, StoryAction?) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentAnswer by remember { mutableStateOf<String?>(null) }
    var selectedAction by remember { mutableStateOf<StoryAction?>(null) }
    var dialog by remember { mutableStateOf(dialogProvider.getDialog(npcType, gameState)) }

    LaunchedEffect(gameState.storyStatus, gameState.inventory.size) {
        dialog = dialogProvider.getDialog(npcType, gameState)
        currentAnswer = null
        selectedAction = null
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

                // Description (if present)
                dialog.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

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
                        onClick = {
                            currentAnswer = null
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Continue...",
                            color = Color.White
                        )
                    }
                } else {
                    // Question buttons
                    if (dialog.question1.isNotEmpty()) {
                        Button(
                            onClick = {
                                currentAnswer = dialog.answer1
                                // Only update selectedAction if there's a new non-null action
                                dialog.action1?.let { selectedAction = it }
                            },
                            enabled = dialog.enabled1,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dialog.action1 != null) {
                                    Color(0xFF1565C0) // Dark blue for action buttons
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF424242),
                                disabledContentColor = Color(0xFF9E9E9E)
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
                                // Only update selectedAction if there's a new non-null action
                                dialog.action2?.let { selectedAction = it }
                            },
                            enabled = dialog.enabled2,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dialog.action2 != null) {
                                    Color(0xFF1565C0) // Dark blue for action buttons
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF424242),
                                disabledContentColor = Color(0xFF9E9E9E)
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
                                // Only update selectedAction if there's a new non-null action
                                dialog.storyAction?.let { selectedAction = it }
                            },
                            enabled = dialog.enabled3,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (dialog.storyAction != null) {
                                    Color(0xFF1565C0) // Dark blue for action buttons
                                } else {
                                    MaterialTheme.colorScheme.primary
                                },
                                contentColor = Color.White,
                                disabledContainerColor = Color(0xFF424242),
                                disabledContentColor = Color(0xFF9E9E9E)
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
                            containerColor = Color.DarkGray,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "0. Leave",
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
