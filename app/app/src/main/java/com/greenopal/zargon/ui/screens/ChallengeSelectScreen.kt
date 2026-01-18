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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.greenopal.zargon.data.models.ChallengeConfig
import com.greenopal.zargon.data.models.ChallengePresets
import com.greenopal.zargon.data.models.DifficultyLevel
import com.greenopal.zargon.data.models.EquipmentMode
import com.greenopal.zargon.data.models.TimedChallenge
import com.greenopal.zargon.ui.theme.DarkStone
import com.greenopal.zargon.ui.theme.EmberOrange
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.MidStone
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.viewmodels.ChallengeViewModel

@Composable
fun ChallengeSelectScreen(
    onStartChallenge: (ChallengeConfig) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChallengeViewModel = hiltViewModel()
) {
    val challengeConfig by viewModel.challengeConfig.collectAsState()
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
                    text = "CHALLENGE MODE",
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
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Prestige Bonuses",
                            style = MaterialTheme.typography.titleMedium,
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Permanent AP Bonus: ${if (prestigeData.hasAPBonus) "+5" else "Not Yet Earned"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Parchment
                        )
                        Text(
                            text = "Permanent DP Bonus: ${if (prestigeData.hasDPBonus) "+5" else "Not Yet Earned"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Parchment
                        )
                        Text(
                            text = "Starting Gold: +${prestigeData.startingGoldBonus}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Parchment
                        )
                        Text(
                            text = "XP Multiplier: ${String.format("%.0f", (1.0f + prestigeData.xpMultiplierBonus) * 100)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Parchment
                        )
                        Text(
                            text = "Completed: ${prestigeData.totalCompletions} challenges",
                            style = MaterialTheme.typography.bodySmall,
                            color = Parchment
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Preset Challenges",
                    style = MaterialTheme.typography.titleLarge,
                    color = Parchment,
                    fontWeight = FontWeight.Bold
                )

                ChallengePresets.ALL_PRESETS.forEach { preset ->
                    val isCompleted = viewModel.isChallengeCompleted(preset.getChallengeId())
                    PresetChallengeButton(
                        preset = preset,
                        isCompleted = isCompleted,
                        onClick = {
                            viewModel.applyPreset(preset)
                            onStartChallenge(preset)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Custom Challenge",
                    style = MaterialTheme.typography.titleLarge,
                    color = Parchment,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = challengeConfig.getDisplayName(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Gold,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = DarkStone
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Difficulty",
                            style = MaterialTheme.typography.titleSmall,
                            color = Gold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DifficultyLevel.values().forEach { diff ->
                                Button(
                                    onClick = { viewModel.setDifficulty(diff) },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (challengeConfig.difficulty == diff) Gold else MidStone,
                                        contentColor = if (challengeConfig.difficulty == diff) DarkStone else Parchment
                                    )
                                ) {
                                    Text(
                                        text = diff.displayName,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Weapon Mode",
                            style = MaterialTheme.typography.titleSmall,
                            color = Gold
                        )
                        EquipmentMode.values().forEach { mode ->
                            Button(
                                onClick = { viewModel.setWeaponMode(mode) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (challengeConfig.weaponMode == mode) Gold else MidStone,
                                    contentColor = if (challengeConfig.weaponMode == mode) DarkStone else Parchment
                                )
                            ) {
                                Text(
                                    text = "${mode.weaponDisplayName} (${mode.powerMultiplier}x power, ${mode.costMultiplier}x cost)",
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Armor Mode",
                            style = MaterialTheme.typography.titleSmall,
                            color = Gold
                        )
                        EquipmentMode.values().forEach { mode ->
                            Button(
                                onClick = { viewModel.setArmorMode(mode) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (challengeConfig.armorMode == mode) Gold else MidStone,
                                    contentColor = if (challengeConfig.armorMode == mode) DarkStone else Parchment
                                )
                            ) {
                                Text(
                                    text = "${mode.armorDisplayName} (${mode.powerMultiplier}x power, ${mode.costMultiplier}x cost)",
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Permanent Death",
                                style = MaterialTheme.typography.titleSmall,
                                color = EmberOrange
                            )
                            Switch(
                                checked = challengeConfig.permanentDeath,
                                onCheckedChange = { viewModel.setPermanentDeath(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = EmberOrange,
                                    checkedTrackColor = EmberOrange.copy(alpha = 0.5f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Timed Challenge",
                            style = MaterialTheme.typography.titleSmall,
                            color = Gold
                        )
                        TimedChallenge.values().forEach { timed ->
                            Button(
                                onClick = { viewModel.setTimedChallenge(timed) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (challengeConfig.timedChallenge == timed) Gold else MidStone,
                                    contentColor = if (challengeConfig.timedChallenge == timed) DarkStone else Parchment
                                )
                            ) {
                                Text(
                                    text = timed.displayName,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onStartChallenge(challengeConfig) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Parchment,
                        contentColor = DarkStone
                    )
                ) {
                    Text(
                        text = "START CHALLENGE",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

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
private fun PresetChallengeButton(
    preset: ChallengeConfig,
    isCompleted: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isCompleted) Parchment else Gold,
            contentColor = DarkStone
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = preset.getDisplayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (isCompleted) {
                    Text(
                        text = "✓",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DarkStone
                    )
                }
            }

            Text(
                text = buildString {
                    append(preset.difficulty.displayName)
                    append(" • ")
                    append(preset.weaponMode.weaponDisplayName)
                    append(" • ")
                    append(preset.armorMode.armorDisplayName)
                    if (preset.permanentDeath) {
                        append(" • Permadeath")
                    }
                    if (preset.timedChallenge != TimedChallenge.NONE) {
                        append(" • ${preset.timedChallenge.displayName}")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = DarkStone.copy(alpha = 0.8f)
            )
        }
    }
}
