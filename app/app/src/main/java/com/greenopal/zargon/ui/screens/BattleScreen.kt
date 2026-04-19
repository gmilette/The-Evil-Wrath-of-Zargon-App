package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.greenopal.zargon.R
import com.greenopal.zargon.data.models.CharacterStats
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.domain.battle.BattleAction
import com.greenopal.zargon.domain.battle.BattleResult
import com.greenopal.zargon.domain.battle.BattleState
import com.greenopal.zargon.domain.graphics.Sprite
import com.greenopal.zargon.ui.components.BattleRewardsDialog
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.MagicMenu
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalButtonVariant
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.MedievalStatBar
import com.greenopal.zargon.ui.components.MonsterStatsBox
import com.greenopal.zargon.ui.components.OrnateSeparator
import com.greenopal.zargon.ui.components.SpriteView
import com.greenopal.zargon.ui.components.StatBarType
import com.greenopal.zargon.ui.theme.Ember
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.GoldBright
import com.greenopal.zargon.ui.theme.HpRed
import com.greenopal.zargon.ui.theme.HpRedBright
import com.greenopal.zargon.ui.theme.MpBlueBright
import com.greenopal.zargon.ui.theme.PanelBg
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.theme.ParchmentDim
import com.greenopal.zargon.ui.viewmodels.BattleViewModel

@Composable
fun BattleScreen(
    gameState: GameState,
    playerSprite: Sprite?,
    monsterSprites: Map<String, Sprite?>,
    onBattleEnd: (BattleResult, GameState) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BattleViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.startBattle(gameState)
    }

    val battleState by viewModel.battleState.collectAsState()
    val battleRewards by viewModel.battleRewards.collectAsState()

    var showRewardsDialog by remember { mutableStateOf(false) }
    var showDefeatDialog by remember { mutableStateOf(false) }

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

    fun handleBattleEnd() {
        android.util.Log.d("BattleScreen", "handleBattleEnd called")
        val state = battleState
        if (state != null && state.battleResult != BattleResult.InProgress) {
            val updatedGameState = viewModel.getUpdatedGameState() ?: gameState
            android.util.Log.d("BattleScreen", "Calling onBattleEnd with gold: ${updatedGameState.character.gold}")
            onBattleEnd(state.battleResult, updatedGameState)
        }
    }

    DungeonBackground {
        Box(modifier = modifier.fillMaxSize()) {
            if (battleState != null) {
                Column(
                    modifier            = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EnemyDisplayBox(
                        battleState   = battleState!!,
                        monsterSprite = getMonsterSprite(battleState!!.monster.name, monsterSprites),
                        modifier      = Modifier
                            .fillMaxWidth()
                            .weight(0.4f),
                    )

                    Row(
                        modifier              = Modifier
                            .fillMaxWidth()
                            .weight(0.6f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Column(
                            modifier            = Modifier.weight(0.3f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CharacterStatsMini(
                                character = battleState!!.character,
                                modifier  = Modifier.fillMaxWidth(),
                            )
                            Box(
                                modifier         = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center,
                            ) {
                                SpriteView(sprite = playerSprite, size = 80.dp)
                            }
                        }

                        Column(
                            modifier            = Modifier.weight(0.4f),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            MonsterStatsBox(
                                monster  = battleState!!.monster,
                                modifier = Modifier.fillMaxWidth(),
                            )
                            MessageBox(
                                messages = battleState!!.messages,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                            )
                        }

                        ActionMenu(
                            onAction     = { action -> viewModel.onAction(action) },
                            isPlayerTurn = battleState!!.isPlayerTurn,
                            modifier     = Modifier.weight(0.3f),
                        )
                    }
                }

                if (battleState!!.showMagicMenu) {
                    MagicMenu(
                        playerLevel  = battleState!!.character.level,
                        currentMP    = battleState!!.character.currentMP,
                        onSpellSelected = { spellAction -> viewModel.onAction(spellAction) },
                        onCancel        = { viewModel.closeMagicMenu() },
                        prestigeData = gameState.prestigeData,
                    )
                }
            } else {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text  = "Entering Battle...",
                        style = MaterialTheme.typography.titleLarge.copy(color = GoldBright),
                    )
                }
            }

            if (showRewardsDialog && battleRewards != null) {
                BattleRewardsDialog(
                    rewards   = battleRewards!!,
                    onDismiss = {
                        showRewardsDialog = false
                        handleBattleEnd()
                    },
                )
            }

            if (showDefeatDialog) {
                DefeatDialog(
                    onDismiss = {
                        showDefeatDialog = false
                        handleBattleEnd()
                    },
                )
            }
        }
    }
}

@Composable
private fun DefeatDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = { }) {
        MedievalPanel(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier            = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text      = "YOU DIED",
                    style     = MaterialTheme.typography.headlineLarge.copy(color = HpRedBright),
                    textAlign = TextAlign.Center,
                )
                OrnateSeparator()
                Text(
                    text      = "Your quest has ended...",
                    style     = MaterialTheme.typography.bodyMedium.copy(color = ParchmentDim),
                    textAlign = TextAlign.Center,
                )
                MedievalButton(onClick = onDismiss, variant = MedievalButtonVariant.Ember) {
                    Text("Return to Title", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

@Composable
private fun EnemyDisplayBox(
    battleState: BattleState,
    monsterSprite: Sprite?,
    modifier: Modifier = Modifier,
) {
    MedievalPanel(modifier = modifier) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SpriteView(
                    sprite          = monsterSprite,
                    size            = 150.dp,
                    backgroundColor = androidx.compose.ui.graphics.Color.Transparent,
                    showBorder      = false,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = battleState.monster.name,
                    style = MaterialTheme.typography.titleMedium.copy(color = Ember),
                )
            }
        }
    }
}

@Composable
private fun CharacterStatsMini(
    character: CharacterStats,
    modifier: Modifier = Modifier,
) {
    MedievalPanel(
        modifier       = modifier,
        contentPadding = PaddingValues(10.dp),
        showCornerGems = false,
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("JOE", style = MaterialTheme.typography.titleSmall.copy(color = GoldBright))
            Text("AP: ${character.baseAP + character.weaponBonus}", style = MaterialTheme.typography.bodySmall.copy(color = Parchment))
            Spacer(Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("HP", style = MaterialTheme.typography.labelSmall.copy(color = HpRedBright))
                Text("${character.currentHP}/${character.maxHP}", style = MaterialTheme.typography.labelSmall.copy(color = HpRedBright))
            }
            MedievalStatBar(character.currentHP, character.maxHP, StatBarType.HP, height = 6.dp)
            Spacer(Modifier.height(2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("MP", style = MaterialTheme.typography.labelSmall.copy(color = MpBlueBright))
                Text("${character.currentMP}/${character.maxMP}", style = MaterialTheme.typography.labelSmall.copy(color = MpBlueBright))
            }
            MedievalStatBar(character.currentMP, character.maxMP, StatBarType.MP, height = 6.dp)
        }
    }
}

@Composable
private fun MessageBox(
    messages: List<String>,
    modifier: Modifier = Modifier,
) {
    MedievalPanel(
        modifier       = modifier,
        contentPadding = PaddingValues(8.dp),
        showCornerGems = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            messages.asReversed().forEach { message ->
                Text(
                    text  = message,
                    style = MaterialTheme.typography.bodySmall.copy(color = ParchmentDim),
                )
            }
        }
    }
}

@Composable
private fun ActionMenu(
    onAction: (BattleAction) -> Unit,
    isPlayerTurn: Boolean,
    modifier: Modifier = Modifier,
) {
    MedievalPanel(
        modifier       = modifier,
        contentPadding = PaddingValues(8.dp),
        showCornerGems = false,
    ) {
        Column(
            modifier            = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text      = "ACTIONS",
                style     = MaterialTheme.typography.labelSmall.copy(color = Gold),
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth(),
            )

            val btnVariant = if (isPlayerTurn) MedievalButtonVariant.Gold else MedievalButtonVariant.Disabled

            MedievalButton(
                onClick = { onAction(BattleAction.Attack) },
                variant = btnVariant,
            ) {
                Image(
                    painter            = painterResource(R.drawable.attack_icon),
                    contentDescription = "Attack",
                    modifier           = Modifier.size(40.dp),
                )
                Text("ATTACK", style = MaterialTheme.typography.labelSmall.copy(color = GoldBright))
            }

            MedievalButton(
                onClick = { onAction(BattleAction.Magic) },
                variant = btnVariant,
            ) {
                Image(
                    painter            = painterResource(R.drawable.spell),
                    contentDescription = "Magic",
                    modifier           = Modifier.size(40.dp),
                )
                Text("MAGIC", style = MaterialTheme.typography.labelSmall.copy(color = GoldBright))
            }

            MedievalButton(
                onClick = { onAction(BattleAction.Run) },
                variant = btnVariant,
            ) {
                Image(
                    painter            = painterResource(R.drawable.run_icon),
                    contentDescription = "Flee",
                    modifier           = Modifier.size(40.dp),
                )
                Text("FLEE", style = MaterialTheme.typography.labelSmall.copy(color = GoldBright))
            }
        }
    }
}

private fun getMonsterSprite(monsterName: String, sprites: Map<String, Sprite?>): Sprite? {
    val baseName = monsterName.replace("Great ", "", ignoreCase = true).lowercase()
    return when {
        baseName.contains("bat")    -> sprites["bat"]
        baseName.contains("babble") -> sprites["babble"]
        baseName.contains("spook")  -> sprites["spook"]
        baseName.contains("slime")  -> sprites["slime"]
        baseName.contains("beleth") -> sprites["demon"]
        baseName.contains("snake")  -> sprites["snake"]
        baseName.contains("necro")  -> sprites["necro"]
        baseName.contains("kraken") -> sprites["kraken"]
        baseName.contains("zargon") -> sprites["ZARGON"]
        else                        -> null
    }
}
