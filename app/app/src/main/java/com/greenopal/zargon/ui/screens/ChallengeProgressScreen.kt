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
import com.greenopal.zargon.data.models.PrestigeBonus
import com.greenopal.zargon.ui.theme.DarkStone
import com.greenopal.zargon.ui.theme.EmberOrange
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.MidStone
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.viewmodels.ChallengeViewModel

@Composable
fun ChallengeProgressScreen(
    onBack: () -> Unit,
    activeChallengeName: String? = null,
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
                    text = "CHALLENGES",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 28.sp,
                        letterSpacing = 2.sp
                    ),
                    color = Gold,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (activeChallengeName != null) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = DarkStone
                        ),
                        border = BorderStroke(2.dp, EmberOrange)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Current Challenge",
                                style = MaterialTheme.typography.titleMedium,
                                color = EmberOrange,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = activeChallengeName,
                                style = MaterialTheme.typography.titleLarge,
                                color = Gold,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
                            text = "Prizes",
                            style = MaterialTheme.typography.titleLarge,
                            color = Gold,
                            fontWeight = FontWeight.Bold
                        )

                        PrestigeBonus.values().forEach { bonus ->
                            val isUnlocked = bonus in prestigeData.unlockedBonuses
                            val isActive = bonus in prestigeData.activeBonuses
                            PrizeRow(
                                label = bonus.displayName,
                                description = bonus.description,
                                isUnlocked = isUnlocked,
                                isActive = isActive
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
private fun PrizeRow(
    label: String,
    description: String,
    isUnlocked: Boolean,
    isActive: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isUnlocked) Parchment else MidStone
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = if (isUnlocked) Parchment.copy(alpha = 0.7f) else MidStone
            )
        }
        Text(
            text = when {
                isActive -> "ACTIVE"
                isUnlocked -> "UNLOCKED"
                else -> "LOCKED"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = when {
                isActive -> Gold
                isUnlocked -> Parchment
                else -> MidStone
            },
            fontWeight = FontWeight.Bold
        )
    }
}
