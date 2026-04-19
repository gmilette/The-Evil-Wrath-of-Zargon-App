package com.greenopal.zargon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.greenopal.zargon.domain.progression.BattleRewards
import com.greenopal.zargon.ui.theme.Ember
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.GoldBright
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.ParchmentDim
import com.greenopal.zargon.ui.theme.XpGreenBright

@Composable
fun BattleRewardsDialog(
    rewards: BattleRewards,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Dialog(onDismissRequest = onDismiss) {
        MedievalPanel(modifier = modifier.fillMaxWidth()) {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text      = "Victory!",
                    style     = MaterialTheme.typography.headlineMedium.copy(color = GoldBright),
                    textAlign = TextAlign.Center,
                )

                OrnateSeparator()

                Text("You Gained:", style = MaterialTheme.typography.bodyMedium.copy(color = Parchment))

                Text(
                    text  = "${rewards.xpGained} Experience!",
                    style = MaterialTheme.typography.titleMedium.copy(color = XpGreenBright),
                )

                Text(
                    text  = "${rewards.goldGained} Gold!",
                    style = MaterialTheme.typography.titleMedium.copy(color = Gold),
                )

                rewards.itemDropped?.let { item ->
                    OrnateSeparator()
                    Text("You picked up:", style = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim))
                    Text(item.name, style = MaterialTheme.typography.bodyLarge.copy(color = Ember))
                }

                if (rewards.leveledUp && rewards.newLevel != null) {
                    OrnateSeparator()
                    MedievalPanel(showCornerGems = false) {
                        Column(
                            modifier            = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(
                                text  = "LEVEL UP!",
                                style = MaterialTheme.typography.headlineSmall.copy(color = GoldBright),
                            )
                            Text(
                                text      = "JOE has raised to level ${rewards.newLevel}!",
                                style     = MaterialTheme.typography.bodyMedium.copy(color = Parchment),
                                textAlign = TextAlign.Center,
                            )
                            if (rewards.apGain != null && rewards.dpGain != null && rewards.mpGain != null) {
                                Spacer(Modifier.height(4.dp))
                                Text("AP +${rewards.apGain}", style = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim))
                                Text("DP +${rewards.dpGain}", style = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim))
                                Text("MP +${rewards.mpGain}", style = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim))
                            }
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                MedievalButton(onClick = {
                    android.util.Log.d("BattleRewardsDialog", "Continue button clicked - Gold gained: ${rewards.goldGained}")
                    onDismiss()
                }) {
                    Text("Continue", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
