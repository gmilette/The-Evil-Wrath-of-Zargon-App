package com.greenopal.zargon.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.greenopal.zargon.data.models.PrestigeBonus
import com.greenopal.zargon.data.models.PrestigeData
import com.greenopal.zargon.domain.battle.BattleAction
import com.greenopal.zargon.domain.battle.Spell
import com.greenopal.zargon.domain.battle.Spells

/**
 * Magic menu dialog showing available spells
 * Based on Magix procedure (ZARGON.BAS:2515)
 */
@Composable
fun MagicMenu(
    playerLevel: Int,
    currentMP: Int,
    onSpellSelected: (BattleAction.CastSpell) -> Unit,
    onCancel: () -> Unit,
    prestigeData: PrestigeData = PrestigeData(),
    modifier: Modifier = Modifier
) {
    val spellBonusActive = prestigeData.isBonusActive(PrestigeBonus.MASTER_SPELLBOOK)
    Dialog(onDismissRequest = onCancel) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "MAGICKS",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Get available spells
                val availableSpells = Spells.getAvailableSpells(playerLevel)

                // Display spells
                availableSpells.forEach { spell ->
                    SpellButton(
                        spell = spell,
                        currentMP = currentMP,
                        spellBonusActive = spellBonusActive,
                        onClick = {
                            onSpellSelected(BattleAction.CastSpell(spell.id))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Cancel button
                Button(
                    onClick = onCancel,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("${availableSpells.size + 1}. Forget it")
                }
            }
        }
    }
}

@Composable
private fun SpellButton(
    spell: Spell,
    currentMP: Int,
    spellBonusActive: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val canCast = spell.canCast(currentMP)
    val backgroundColor = if (canCast) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (canCast) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Button(
        onClick = onClick,
        enabled = canCast,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${spell.id}. ${if (spellBonusActive) "Great ${spell.name}" else spell.name}",
                color = textColor,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "MP: ${spell.mpCost}",
                color = textColor,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
