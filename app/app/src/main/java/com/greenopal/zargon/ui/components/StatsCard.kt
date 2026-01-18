package com.greenopal.zargon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.data.repository.PrestigeRepository

/**
 * Displays character stats in a retro-styled card.
 * Based on charstatz procedure (ZARGON.BAS:820)
 */
@Composable
fun StatsCard(
    stats: CharacterStats,
    prestigeRepository: PrestigeRepository,
    modifier: Modifier = Modifier
) {
    val prestige by prestigeRepository.loadPrestigeFlow().collectAsState(initial = PrestigeData())
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Title
            Text(
                text = "JOE'S STATS",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Level and Gold
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Level: ${stats.level}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Gold: ${stats.gold}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFFFFD700) // Gold color
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Experience
            Text(
                text = "EXP: ${stats.experience}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Combat Stats with prestige bonuses
            val apWithPrestige = stats.totalAP + prestige.permanentAPBonus
            val apDisplay = if (prestige.permanentAPBonus > 0) {
                "${stats.baseAP} + ${stats.weaponBonus} + ${prestige.permanentAPBonus} = $apWithPrestige"
            } else {
                apWithPrestige.toString()
            }
            StatRow(
                label = "AP",
                value = apDisplay,
                color = if (prestige.permanentAPBonus > 0) Color(0xFFFF9A3C) else Color.Unspecified
            )

            val defenseWithPrestige = stats.totalDefense + prestige.permanentDPBonus
            val defenseDisplay = if (prestige.permanentDPBonus > 0) {
                "${stats.baseDP} + ${stats.armorBonus} + ${prestige.permanentDPBonus} = $defenseWithPrestige"
            } else {
                defenseWithPrestige.toString()
            }
            StatRow(
                label = "Defense",
                value = defenseDisplay,
                color = if (prestige.permanentDPBonus > 0) Color(0xFFFF9A3C) else Color.Unspecified
            )

            if (prestige.xpMultiplierBonus > 0f) {
                val multiplier = String.format("%.0f%%", prestige.xpMultiplierBonus * 100)
                StatRow(
                    label = "XP Bonus",
                    value = "+$multiplier",
                    color = Color(0xFFFF9A3C)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // HP Bar
            StatBar(
                label = "HP",
                current = stats.currentDP,
                max = stats.maxDP,
                percentage = stats.hpPercentage,
                color = Color(0xFFFF5555) // Red
            )

            Spacer(modifier = Modifier.height(8.dp))

            // MP Bar
            StatBar(
                label = "MP",
                current = stats.currentMP,
                max = stats.maxMP,
                percentage = stats.mpPercentage,
                color = Color(0xFF55FFFF) // Cyan
            )
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    color: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = if (color != Color.Unspecified) color else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatBar(
    label: String,
    current: Int,
    max: Int,
    percentage: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "$current / $max",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
