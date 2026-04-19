package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalButtonVariant
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.OrnateSeparator
import com.greenopal.zargon.ui.components.ScreenHeading
import com.greenopal.zargon.ui.theme.DebugGreen
import com.greenopal.zargon.ui.theme.Gold

@Composable
fun MenuScreen(
    onStartExploration: () -> Unit,
    onStartBattleTest: () -> Unit,
    onViewStats: () -> Unit,
    onViewQuestProgress: () -> Unit,
    onViewHints: () -> Unit,
    onViewChallengeProgress: () -> Unit,
    onBack: () -> Unit,
    onExitToTitle: () -> Unit,
    onDebugSetup: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    DungeonBackground {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(Modifier.height(32.dp))

            MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ScreenHeading("Menu")

                    Spacer(Modifier.height(4.dp))

                    MedievalButton(onClick = onViewStats) {
                        Text("View Character Stats", style = MaterialTheme.typography.titleMedium)
                    }

                    MedievalButton(onClick = onViewQuestProgress) {
                        Text("View Quest Progress", style = MaterialTheme.typography.titleMedium)
                    }

                    MedievalButton(onClick = onViewHints) {
                        Text("Map Guide & Hints", style = MaterialTheme.typography.titleMedium)
                    }

                    MedievalButton(onClick = onViewChallengeProgress) {
                        Text("Challenges", style = MaterialTheme.typography.titleMedium)
                    }

                    OrnateSeparator()

                    MedievalButton(variant = MedievalButtonVariant.Ember, onClick = onExitToTitle) {
                        Text("Exit to Main Menu", style = MaterialTheme.typography.titleMedium)
                    }

                    MedievalButton(onClick = onBack) {
                        Text("Close", style = MaterialTheme.typography.titleMedium)
                    }

                    if (onDebugSetup != null) {
                        MedievalButton(onClick = onDebugSetup) {
                            Text(
                                "[DEBUG] Max Joe Out",
                                style = MaterialTheme.typography.titleMedium.copy(color = DebugGreen),
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
