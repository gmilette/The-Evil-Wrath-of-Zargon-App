package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.greenopal.zargon.R
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.domain.battle.BattleAction
import com.greenopal.zargon.domain.battle.BattleResult
import com.greenopal.zargon.domain.graphics.Sprite
import com.greenopal.zargon.ui.components.BattleRewardsDialog
import com.greenopal.zargon.ui.components.MagicMenu
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
    val battleRewards by viewModel.battleRewards.collectAsState()

    var showRewardsDialog by remember { mutableStateOf(false) }
    var showDefeatDialog by remember { mutableStateOf(false) }

    // Show rewards dialog when battle is won and rewards are available
    LaunchedEffect(battleState?.battleResult, battleRewards) {
        android.util.Log.d("BattleScreen", "LaunchedEffect triggered - Result: ${battleState?.battleResult}, Rewards: ${battleRewards}, ShowDialog: $showRewardsDialog")
        if (battleState?.battleResult == BattleResult.Victory && battleRewards != null) {
            android.util.Log.d("BattleScreen", "Showing rewards dialog NOW!")
            showRewardsDialog = true
        } else if (battleState?.battleResult == BattleResult.Defeat) {
            android.util.Log.d("BattleScreen", "Player defeated - showing death dialog")
            showDefeatDialog = true
        } else if (battleState?.battleResult == BattleResult.Fled) {
            android.util.Log.d("BattleScreen", "Player fled - ending battle")
            val state = battleState
            if (state != null) {
                val updatedGameState = viewModel.getUpdatedGameState() ?: gameState
                onBattleEnd(state.battleResult, updatedGameState)
            }
        }
    }

    // Handle battle end after rewards dialog is dismissed
    fun handleBattleEnd() {
        android.util.Log.d("BattleScreen", "handleBattleEnd called")
        val state = battleState
        if (state != null && state.battleResult != BattleResult.InProgress) {
            // Get updated game state with all rewards applied
            val updatedGameState = viewModel.getUpdatedGameState() ?: gameState
            android.util.Log.d("BattleScreen", "Calling onBattleEnd with gold: ${updatedGameState.character.gold}")
            onBattleEnd(state.battleResult, updatedGameState)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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

            // Magic menu overlay
            if (battleState!!.showMagicMenu) {
                MagicMenu(
                    playerLevel = battleState!!.character.level,
                    currentMP = battleState!!.character.currentMP,
                    onSpellSelected = { spellAction ->
                        viewModel.onAction(spellAction)
                    },
                    onCancel = {
                        viewModel.closeMagicMenu()
                    }
                )
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
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Rewards dialog
        if (showRewardsDialog && battleRewards != null) {
            BattleRewardsDialog(
                rewards = battleRewards!!,
                onDismiss = {
                    showRewardsDialog = false
                    handleBattleEnd()
                }
            )
        }

        // Defeat dialog
        if (showDefeatDialog) {
            DefeatDialog(
                onDismiss = {
                    showDefeatDialog = false
                    handleBattleEnd()
                }
            )
        }
    }
}

@Composable
private fun DefeatDialog(
    onDismiss: () -> Unit
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = { /* Prevent dismissing by tapping outside */ }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            border = BorderStroke(3.dp, MaterialTheme.colorScheme.error)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "YOU DIED",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Your quest has ended...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Return to Title")
                }
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
            containerColor = MaterialTheme.colorScheme.surface
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
                text = "HP: ${character.currentHP}/${character.maxHP}",
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
            // Display messages in reverse order (latest at top)
            messages.asReversed().forEach { message ->
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

            Image(
                painter = painterResource(R.drawable.attack_icon),
                contentDescription = "Attack",
                modifier = Modifier
                    .size(72.dp)
                    .clickable(enabled = isPlayerTurn) { onAction(BattleAction.Attack) }
            )

            Image(
                painter = painterResource(R.drawable.spell),
                contentDescription = "Magic",
                modifier = Modifier
                    .size(72.dp)
                    .clickable(enabled = isPlayerTurn) { onAction(BattleAction.Magic) }
            )

            Image(
                painter = painterResource(R.drawable.run_icon),
                contentDescription = "Run",
                modifier = Modifier
                    .size(72.dp)
                    .clickable(enabled = isPlayerTurn) { onAction(BattleAction.Run) }
            )
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
