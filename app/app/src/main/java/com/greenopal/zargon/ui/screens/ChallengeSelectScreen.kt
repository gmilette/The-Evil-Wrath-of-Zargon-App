package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.greenopal.zargon.data.models.Challenge
import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalButtonVariant
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.OrnateSeparator
import com.greenopal.zargon.ui.components.ScreenHeading
import com.greenopal.zargon.ui.theme.EmberBright
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.GoldBright
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.ParchmentDim
import com.greenopal.zargon.ui.theme.TextDim
import com.greenopal.zargon.ui.viewmodels.ChallengeViewModel

@Composable
fun ChallengeSelectScreen(
    onStartChallenge: (ChallengeConfig) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChallengeViewModel = hiltViewModel()
) {
    val prestigeData by viewModel.prestigeData.collectAsState()

    DungeonBackground {
        LazyColumn(
            modifier            = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding      = PaddingValues(vertical = 24.dp),
        ) {
            item { ScreenHeading("Challenge Mode") }

            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Challenge.values().forEach { challenge ->
                            val config      = ChallengeConfig(challenges = setOf(challenge))
                            val isCompleted = viewModel.isChallengeCompleted(config.getChallengeId())
                            val reward      = viewModel.getRewardForChallenge(challenge)

                            MedievalButton(
                                onClick = { onStartChallenge(config) },
                                variant = if (isCompleted) MedievalButtonVariant.Equipped else MedievalButtonVariant.Gold,
                            ) {
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text  = challenge.displayName,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                color      = if (isCompleted) Parchment else Gold,
                                                fontWeight = FontWeight.Bold,
                                            ),
                                        )
                                        Text(
                                            text  = challenge.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (isCompleted) ParchmentDim else TextDim,
                                            ),
                                        )
                                        if (reward != null) {
                                            Text(
                                                text  = "Reward: ${reward.displayName}",
                                                style = MaterialTheme.typography.bodySmall.copy(color = Gold),
                                            )
                                        }
                                    }
                                    if (isCompleted) {
                                        Text(
                                            text  = "✓",
                                            style = MaterialTheme.typography.titleMedium.copy(color = GoldBright),
                                        )
                                    }
                                }
                            }
                        }

                        OrnateSeparator()

                        Text(
                            text  = "Completed: ${prestigeData.totalCompletions} challenges",
                            style = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim),
                        )

                        MedievalButton(
                            onClick = onBack,
                            variant = MedievalButtonVariant.Ember,
                        ) {
                            Text("Back", style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}
