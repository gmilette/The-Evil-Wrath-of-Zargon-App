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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.domain.battle.BattleAction
import com.greenopal.zargon.domain.battle.BattleResult
import com.greenopal.zargon.domain.graphics.Sprite
import com.greenopal.zargon.ui.components.MonsterStatsBox
import com.greenopal.zargon.ui.components.SpriteView
import com.greenopal.zargon.ui.viewmodels.BattleViewModel

/**
 * Battle screen matching QBASIC battleset layout
 * Based on battleset procedure (ZARGON.BAS:530)
 */
@Composable
fun BattleScreen(
    gameState: GameState,
    playerSprite: Sprite?,
    monsterSprites: Map<String, Sprite?>,
    onBattleEnd: (BattleResult, GameState) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BattleViewModel = hiltViewModel()
) {
    // Start battle
    LaunchedEffect(Unit) {
        viewModel.startBattle(gameState)
    }

    val battleState by viewModel.battleState.collectAsState()

    // Handle battle end
    LaunchedEffect(battleState?.battleResult) {
        val state = battleState
        if (state != null && state.battleResult != BattleResult.InProgress) {
            // Update game state with new character stats
            val updatedGameState = gameState.updateCharacter(state.character)
            onBattleEnd(state.battleResult, updatedGameState)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (battleState != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Enemy display area (top)
                EnemyDisplayBox(
                    battleState = battleState!!,
                    monsterSprite = getMonsterSprite(battleState!!.monster.name, monsterSprites),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.4f)
                )

                // Bottom section with stats and controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.6f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Left column: Character stats + Player sprite
                    Column(
                        modifier = Modifier.weight(0.3f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Character stats
                        CharacterStatsMini(
                            character = battleState!!.character,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Player sprite
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            SpriteView(
                                sprite = playerSprite,
                                size = 80.dp
                            )
                        }
                    }

                    // Middle column: Monster stats + Messages
                    Column(
                        modifier = Modifier.weight(0.4f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Monster stats
                        MonsterStatsBox(
                            monster = battleState!!.monster,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Message box
                        MessageBox(
                            messages = battleState!!.messages,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
                    }

                    // Right column: Action menu
                    ActionMenu(
                        onAction = { action -> viewModel.onAction(action) },
                        isPlayerTurn = battleState!!.isPlayerTurn,
                        modifier = Modifier.weight(0.3f)
                    )
                }
            }
        } else {
            // Loading
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Entering Battle...",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun EnemyDisplayBox(
    battleState: com.greenopal.zargon.domain.battle.BattleState,
    monsterSprite: Sprite?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SpriteView(
                    sprite = monsterSprite,
                    size = 150.dp,
                    backgroundColor = Color.Transparent,
                    showBorder = false
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = battleState.monster.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun CharacterStatsMini(
    character: com.greenopal.zargon.data.models.CharacterStats,
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
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "JOE",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "AP: ${character.totalAP}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "HP: ${character.currentDP}/${character.maxDP}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "MP: ${character.currentMP}/${character.maxMP}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun MessageBox(
    messages: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.secondary)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            messages.forEach { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun ActionMenu(
    onAction: (BattleAction) -> Unit,
    isPlayerTurn: Boolean,
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
                .padding(8.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ACTIONS",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { onAction(BattleAction.Attack) },
                enabled = isPlayerTurn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("1. Attack!")
            }

            Button(
                onClick = { onAction(BattleAction.Magic) },
                enabled = isPlayerTurn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("2. Magick")
            }

            Button(
                onClick = { onAction(BattleAction.Run) },
                enabled = isPlayerTurn,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("3. Run!")
            }
        }
    }
}

/**
 * Helper to get monster sprite by name
 */
private fun getMonsterSprite(monsterName: String, sprites: Map<String, Sprite?>): Sprite? {
    // Remove "Great " prefix for sprite lookup
    val baseName = monsterName.replace("Great ", "", ignoreCase = true).lowercase()
    return when {
        baseName.contains("bat") -> sprites["bat"]
        baseName.contains("babble") -> sprites["babble"]
        baseName.contains("spook") -> sprites["spook"]
        baseName.contains("slime") -> sprites["slime"]
        baseName.contains("beleth") -> sprites["demon"] // "demon" sprite in bomb.sht
        baseName.contains("snake") -> sprites["snake"]
        baseName.contains("necro") -> sprites["necro"]
        baseName.contains("kraken") -> sprites["kraken"]
        baseName.contains("zargon") -> sprites["ZARGON"]
        else -> null
    }
}
