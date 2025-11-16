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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.GameState

/**
 * Quest progress screen showing story completion
 * Tracks all 8 quest items and calculates game completion percentage
 */
@Composable
fun QuestProgressScreen(
    gameState: GameState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Define all quest items
    val questItems = listOf(
        QuestItem("dynomite", "Dynamite", "Blast through rocks"),
        QuestItem("dead wood", "Dead Wood", "Material for ship"),
        QuestItem("rutter", "Rutter", "Navigation tool"),
        QuestItem("cloth", "Cloth", "Sail material"),
        QuestItem("wood", "Wood", "Ship hull material"),
        QuestItem("boat plans", "Boat Plans", "Ship blueprints"),
        QuestItem("trapped soul", "Trapped Soul", "Currency for necromancer"),
        QuestItem("ship", "Ship", "River travel vehicle")
    )

    // Calculate progress
    val foundItems = questItems.count { item ->
        gameState.hasItem(item.itemName)
    }
    val totalItems = questItems.size
    val completionPercent = (foundItems.toFloat() / totalItems * 100).toInt()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "QUEST PROGRESS",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            // Progress Summary Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Game Completion",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Text(
                        text = "$completionPercent%",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    LinearProgressIndicator(
                        progress = completionPercent / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Text(
                        text = "$foundItems / $totalItems Quest Items",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Quest Items List
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "QUEST ITEMS",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    questItems.forEach { item ->
                        val found = gameState.hasItem(item.itemName)
                        QuestItemRow(
                            item = item,
                            found = found
                        )
                        if (item != questItems.last()) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }

            // Story Status
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Story Progress: ${String.format("%.1f", gameState.storyStatus)}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = getStoryStageDescription(gameState.storyStatus),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                }
            }

            // Back button
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(0.6f)
            ) {
                Text("BACK TO MENU")
            }
        }
    }
}

@Composable
private fun QuestItemRow(
    item: QuestItem,
    found: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (found) Icons.Default.Check else Icons.Default.Clear,
                contentDescription = if (found) "Found" else "Not found",
                tint = if (found) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                modifier = Modifier.size(24.dp)
            )

            Column {
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (found) FontWeight.Bold else FontWeight.Normal,
                    color = if (found) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (found) Color.LightGray else Color.DarkGray
                )
            }
        }

        if (found) {
            Text(
                text = "✓",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Get human-readable description of story stage
 */
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
        else -> "Unknown stage"
    }
}

/**
 * Data class for quest items
 */
private data class QuestItem(
    val itemName: String,       // Name as stored in inventory
    val displayName: String,    // Display name
    val description: String     // Short description
)
