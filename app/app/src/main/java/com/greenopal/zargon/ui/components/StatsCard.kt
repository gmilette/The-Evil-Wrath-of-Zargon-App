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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.CharacterStats

/**
 * Displays character stats in a retro-styled card.
 * Based on charstatz procedure (ZARGON.BAS:820)
 */
@Composable
fun StatsCard(
    stats: CharacterStats,
    modifier: Modifier = Modifier
) {
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

            // Combat Stats
            StatRow(label = "AP", value = stats.totalAP.toString())
            StatRow(label = "Defense", value = stats.totalDefense.toString())

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
    value: String
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
            fontWeight = FontWeight.Bold
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
