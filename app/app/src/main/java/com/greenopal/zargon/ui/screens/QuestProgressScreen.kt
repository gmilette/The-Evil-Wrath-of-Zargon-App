package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.MedievalStatBar
import com.greenopal.zargon.ui.components.OrnateSeparator
import com.greenopal.zargon.ui.components.ScreenHeading
import com.greenopal.zargon.ui.components.ScrollBanner
import com.greenopal.zargon.ui.components.StatBarType
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.GoldBright
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.ParchmentDim
import com.greenopal.zargon.ui.theme.TextDim
import com.greenopal.zargon.ui.theme.XpGreen
import com.greenopal.zargon.ui.theme.XpGreenBright

@Composable
fun QuestProgressScreen(
    gameState: GameState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val questItems = listOf(
        QuestItem("dynamite", "Dynamite", "Blast through rocks"),
        QuestItem("dead wood", "Dead Wood", "Material for ship"),
        QuestItem("rutter", "Rutter", "Navigation tool"),
        QuestItem("cloth", "Cloth", "Sail material"),
        QuestItem("wood", "Wood", "Ship hull material"),
        QuestItem("boat plans", "Boat Plans", "Ship blueprints"),
        QuestItem("trapped soul", "Trapped Soul", "Currency for necromancer"),
        QuestItem("ship", "Ship", "River travel vehicle"),
        QuestItem("Zargon", "Zargon Trophy", "Victory over evil overlord")
    )

    val foundItems       = questItems.count { gameState.hasDiscovered(it.itemName) }
    val totalItems       = questItems.size
    val completionPercent = (foundItems.toFloat() / totalItems * 100).toInt()

    DungeonBackground {
        LazyColumn(
            modifier            = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding      = PaddingValues(vertical = 24.dp),
        ) {
            item { ScreenHeading("Quest Progress") }

            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text  = "Game Completion",
                            style = MaterialTheme.typography.headlineMedium.copy(color = GoldBright),
                        )

                        Text(
                            text  = "$completionPercent%",
                            style = MaterialTheme.typography.displayLarge.copy(color = XpGreenBright),
                        )

                        MedievalStatBar(
                            current = foundItems,
                            max     = totalItems,
                            type    = StatBarType.XP,
                            height  = 14.dp,
                        )

                        Text(
                            text  = "$foundItems / $totalItems Quest Items",
                            style = MaterialTheme.typography.bodyLarge.copy(color = Parchment),
                        )
                    }
                }
            }

            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        ScrollBanner("Quest Items")

                        Spacer(Modifier.height(8.dp))

                        questItems.forEachIndexed { index, item ->
                            val found = gameState.hasDiscovered(item.itemName)
                            QuestItemRow(item = item, found = found)
                            if (index < questItems.lastIndex) {
                                OrnateSeparator()
                            }
                        }
                    }
                }
            }

            item {
                MedievalPanel(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text  = "Story Progress: ${String.format("%.1f", gameState.storyStatus)}",
                            style = MaterialTheme.typography.titleSmall.copy(color = Gold),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = getStoryStageDescription(gameState.storyStatus),
                            style = MaterialTheme.typography.bodyMedium.copy(color = ParchmentDim),
                        )
                    }
                }
            }

            item {
                MedievalButton(onClick = onBack) {
                    Text("Back to Menu", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun QuestItemRow(item: QuestItem, found: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment     = Alignment.CenterVertically,
            modifier              = Modifier.weight(1f),
        ) {
            Icon(
                imageVector    = if (found) Icons.Default.Check else Icons.Default.Clear,
                contentDescription = if (found) "Found" else "Not found",
                tint           = if (found) XpGreenBright else TextDim,
                modifier       = Modifier.size(24.dp),
            )
            Column {
                Text(
                    text  = item.displayName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color      = if (found) Parchment else TextDim,
                        fontWeight = if (found) FontWeight.Bold else FontWeight.Normal,
                    ),
                )
                Text(
                    text  = item.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim),
                )
            }
        }
    }
}

private fun getStoryStageDescription(status: Float): String {
    return when {
        status < 1.5f -> "Beginning: Need to rescue boatman"
        status < 2.0f -> "Know about dynamite location"
        status < 2.5f -> "Rescued boatman from rocks"
        status < 3.0f -> "Gave boatman wood"
        status < 3.8f -> "Boatman working on ship"
        status < 4.0f -> "Need to collect remaining materials"
        status < 4.3f -> "Boatman died, need soul"
        status < 5.0f -> "Have soul, ready for necromancer"
        status < 5.5f -> "Boatman resurrected"
        status >= 5.5f -> "Ship built, can travel river!"
        else           -> "Unknown stage"
    }
}

private data class QuestItem(
    val itemName: String,
    val displayName: String,
    val description: String
)
