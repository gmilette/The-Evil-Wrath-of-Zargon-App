package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.greenopal.zargon.data.models.ChallengePresets
import com.greenopal.zargon.ui.theme.DarkStone
import com.greenopal.zargon.ui.theme.EmberOrange
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.MidStone
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.viewmodels.ChallengeViewModel

@Composable
fun ChallengeProgressScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChallengeViewModel = hiltViewModel()
) {
    val prestigeData by viewModel.prestigeData.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkStone),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MidStone
            ),
            border = BorderStroke(3.dp, Gold)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "CHALLENGE PROGRESS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        letterSpacing = 2.sp
                    ),
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkStone
                    ),
                    border = BorderStroke(2.dp, Gold)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Overall Progress",
                            style = MaterialTheme.typography.titleLarge,
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )

                        val completionPercentage = prestigeData.getCompletionPercentage()
                        LinearProgressIndicator(
                            progress = completionPercentage / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(24.dp),
                            color = Gold,
                            trackColor = MidStone
                        )

                        Text(
                            text = "${String.format("%.1f", completionPercentage)}% Complete",
                            style = MaterialTheme.typography.titleMedium,
                            color = Parchment,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "${prestigeData.completedChallenges.size} / 288 Challenges Completed",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Parchment,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkStone
                    ),
                    border = BorderStroke(2.dp, Parchment)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Earned Prestige Bonuses",
                            style = MaterialTheme.typography.titleLarge,
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )

                        PrestigeBonusRow(
                            label = "Starting Gold",
                            value = "+${prestigeData.startingGoldBonus}",
                            maxValue = 100
                        )

                        PrestigeBonusRow(
                            label = "Permanent AP Bonus",
                            value = if (prestigeData.hasAPBonus) "+5" else "Not Earned",
                            maxValue = 5
                        )

                        PrestigeBonusRow(
                            label = "Permanent DP Bonus",
                            value = if (prestigeData.hasDPBonus) "+5" else "Not Earned",
                            maxValue = 5
                        )

                        PrestigeBonusRow(
                            label = "XP Multiplier",
                            value = "${String.format("%.0f", (1.0f + prestigeData.xpMultiplierBonus) * 100)}%",
                            maxValue = 150
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkStone
                    ),
                    border = BorderStroke(2.dp, EmberOrange)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Preset Challenges",
                            style = MaterialTheme.typography.titleLarge,
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )

                        ChallengePresets.ALL_PRESETS.forEach { preset ->
                            val isCompleted = viewModel.isChallengeCompleted(preset.getChallengeId())
                            PresetStatusRow(
                                presetName = preset.getDisplayName(),
                                isCompleted = isCompleted
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onBack,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmberOrange,
                        contentColor = DarkStone
                    )
                ) {
                    Text(
                        text = "Back",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun PrestigeBonusRow(
    label: String,
    value: String,
    maxValue: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = Parchment
        )
        Text(
            text = "$value / $maxValue",
            style = MaterialTheme.typography.bodyLarge,
            color = Gold,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PresetStatusRow(
    presetName: String,
    isCompleted: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = presetName,
            style = MaterialTheme.typography.bodyLarge,
            color = Parchment
        )
        Text(
            text = if (isCompleted) "COMPLETED" else "LOCKED",
            style = MaterialTheme.typography.bodyMedium,
            color = if (isCompleted) Parchment else MidStone,
            fontWeight = FontWeight.Bold
        )
    }
}
