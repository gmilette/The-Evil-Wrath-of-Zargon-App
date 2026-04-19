package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.Dialog
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.NpcType
import com.greenopal.zargon.data.models.StoryAction
import com.greenopal.zargon.domain.story.NpcDialogProvider
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.FlavorText
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalButtonVariant
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.OrnateSeparator
import com.greenopal.zargon.ui.components.PanelHeading
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.ParchmentDim

@Composable
fun DialogScreen(
    npcType: NpcType,
    dialogProvider: NpcDialogProvider,
    gameState: GameState,
    onActionTaken: (StoryAction) -> Unit = {},
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

    DungeonBackground {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            MedievalPanel(
                modifier       = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                contentPadding = PaddingValues(24.dp),
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text     = "✕",
                            style    = MaterialTheme.typography.titleSmall.copy(color = Gold.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(bottom = 8.dp),
                        )
                        PanelHeading(npcType.displayName)
                    }

                    dialog.description?.let { description ->
                        FlavorText(description)
                        OrnateSeparator()
                    }

                    if (currentAnswer != null) {
                        MedievalPanel(
                            modifier       = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            showCornerGems = false,
                        ) {
                            Text(
                                text      = currentAnswer!!,
                                style     = MaterialTheme.typography.bodyMedium.copy(color = Parchment),
                                textAlign = TextAlign.Start,
                                modifier  = Modifier.fillMaxWidth(),
                            )
                        }

                        Spacer(Modifier.height(4.dp))

                        MedievalButton(
                            onClick = {
                                selectedAction?.let { action ->
                                    onActionTaken(action)
                                    selectedAction = null
                                }
                                currentAnswer = null
                            }
                        ) {
                            Text("Continue...", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        if (dialog.question1.isNotEmpty()) {
                            val isDisabled = !dialog.enabled1
                            MedievalButton(
                                onClick = {
                                    currentAnswer = dialog.answer1
                                    dialog.action1?.let { selectedAction = it }
                                },
                                variant = if (isDisabled) MedievalButtonVariant.Disabled else MedievalButtonVariant.Gold,
                            ) {
                                Text("1. ${dialog.question1}", style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        if (dialog.question2.isNotEmpty()) {
                            val isDisabled = !dialog.enabled2
                            MedievalButton(
                                onClick = {
                                    currentAnswer = dialog.answer2
                                    dialog.action2?.let { selectedAction = it }
                                },
                                variant = if (isDisabled) MedievalButtonVariant.Disabled else MedievalButtonVariant.Gold,
                            ) {
                                Text("2. ${dialog.question2}", style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        if (dialog.question3.isNotEmpty()) {
                            val isDisabled = !dialog.enabled3
                            MedievalButton(
                                onClick = {
                                    currentAnswer = dialog.answer3
                                    dialog.storyAction?.let { selectedAction = it }
                                },
                                variant = if (isDisabled) MedievalButtonVariant.Disabled else MedievalButtonVariant.Gold,
                            ) {
                                Text("3. ${dialog.question3}", style = MaterialTheme.typography.titleMedium)
                            }
                        }

                        OrnateSeparator()

                        MedievalButton(
                            variant = MedievalButtonVariant.Ember,
                            onClick = { onDialogEnd(gameState, selectedAction) },
                        ) {
                            Text("0. Leave", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
