package com.greenopal.zargon.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.data.models.MonsterStats
import com.greenopal.zargon.ui.theme.Ember
import com.greenopal.zargon.ui.theme.HpRedBright
import com.greenopal.zargon.ui.theme.Parchment

@Composable
fun MonsterStatsBox(
    monster: MonsterStats,
    modifier: Modifier = Modifier,
) {
    MedievalPanel(
        modifier       = modifier,
        contentPadding = PaddingValues(10.dp),
        showCornerGems = false,
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text  = monster.name,
                style = MaterialTheme.typography.titleSmall.copy(color = Ember),
            )

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("AP:", style = MaterialTheme.typography.bodySmall.copy(color = Parchment))
                Text(monster.attackPower.toString(), style = MaterialTheme.typography.bodySmall.copy(color = Parchment))
            }

            Spacer(Modifier.height(2.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("HP", style = MaterialTheme.typography.labelSmall.copy(color = HpRedBright))
                Text("${monster.currentHP} / ${monster.maxHP}", style = MaterialTheme.typography.labelSmall.copy(color = HpRedBright))
            }
            MedievalStatBar(monster.currentHP, monster.maxHP, StatBarType.HP, height = 6.dp)
        }
    }
}
