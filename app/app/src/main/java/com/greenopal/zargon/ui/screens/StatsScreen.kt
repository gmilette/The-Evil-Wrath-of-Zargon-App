package com.greenopal.zargon.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.Item
import com.greenopal.zargon.data.repository.PrestigeRepository

/**
 * Character stats and inventory screen
 * Based on QBASIC charstatz and inventory display
 */
@Composable
fun StatsScreen(
    gameState: GameState,
    onBack: () -> Unit,
    prestigeRepository: PrestigeRepository,
    modifier: Modifier = Modifier
) {
    // Load prestige data
    val prestige by prestigeRepository.loadPrestigeFlow().collectAsState(initial = com.greenopal.zargon.data.models.PrestigeData())

    // Handle back button
    BackHandler {
        onBack()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            item {
                Text(
                    text = "CHARACTER STATUS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Character Stats Card
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "JOE - Level ${gameState.character.level}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Stats
                        StatRow("Hit Points", "${gameState.character.currentHP} / ${gameState.character.maxDP}")
                        StatRow("Magic Points", "${gameState.character.currentMP} / ${gameState.character.maxMP}")

                        StatRow("Attack Power", gameState.character.totalAP.toString())
                        StatRow("Defense Power", gameState.character.totalDefense.toString())

                        if (prestige.activeBonuses.isNotEmpty()) {
                            StatRow(
                                "Prestige",
                                "${prestige.activeBonuses.size} active",
                                MaterialTheme.colorScheme.tertiary
                            )
                        }

                        StatRow("Gold", "${gameState.character.gold}g", MaterialTheme.colorScheme.primary)
                        StatRow("Experience", "${gameState.character.experience} / ${gameState.nextLevelXP}")

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Equipment
                        StatRow("Weapon", com.greenopal.zargon.ui.screens.getWeaponName(gameState.character.weaponStatus))
                        StatRow("Armor", com.greenopal.zargon.ui.screens.getArmorName(gameState.character.armorStatus))

                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                        // Story progress
                        StatRow("Story Progress", String.format("%.1f", gameState.storyStatus), MaterialTheme.colorScheme.secondary)
                        StatRow("Map Location", "World ${gameState.worldX}-${gameState.worldY}")
                    }
                }
            }

            // Inventory Section
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.tertiary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "INVENTORY (${gameState.inventory.size}/10)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        if (gameState.inventory.isEmpty()) {
                            Text(
                                text = "No items",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                gameState.inventory.forEach { item ->
                                    ItemRow(item)
                                }
                            }
                        }
                    }
                }
            }

            // Back button
            item {
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
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}

@Composable
private fun ItemRow(item: Item) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.name.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = when (item.type) {
                    com.greenopal.zargon.data.models.ItemType.KEY_ITEM -> MaterialTheme.colorScheme.tertiary
                    com.greenopal.zargon.data.models.ItemType.WEAPON -> MaterialTheme.colorScheme.error
                    com.greenopal.zargon.data.models.ItemType.ARMOR -> MaterialTheme.colorScheme.secondary
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            if (item.description.isNotEmpty()) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}
