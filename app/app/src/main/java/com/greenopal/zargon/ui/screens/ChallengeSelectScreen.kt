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
import com.greenopal.zargon.data.models.Challenge
import com.greenopal.zargon.data.models.ChallengeConfig
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

                Challenge.values().forEach { challenge ->
                    val config = ChallengeConfig(challenges = setOf(challenge))
                    val isCompleted = viewModel.isChallengeCompleted(config.getChallengeId())
                    val reward = viewModel.getRewardForChallenge(challenge)
                    Button(
                        onClick = { onStartChallenge(config) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) Parchment else Gold,
                            contentColor = DarkStone
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = challenge.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = challenge.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DarkStone.copy(alpha = 0.7f)
                                )
                                if (reward != null) {
                                    Text(
                                        text = "Reward: ${reward.displayName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DarkStone.copy(alpha = 0.7f)
                                    )
                                }
                            }
                            if (isCompleted) {
                                Text(
                                    text = "DONE",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = DarkStone
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Completed: ${prestigeData.totalCompletions} challenges",
                    style = MaterialTheme.typography.bodySmall,
                    color = Parchment
                )

                Spacer(modifier = Modifier.height(8.dp))

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
