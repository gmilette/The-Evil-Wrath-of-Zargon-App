package com.greenopal.zargon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.greenopal.zargon.data.models.NpcType
import com.greenopal.zargon.data.models.StoryAction
import com.greenopal.zargon.data.repository.SaveGameRepository
import com.greenopal.zargon.domain.battle.BattleResult
import com.greenopal.zargon.domain.graphics.Sprite
import com.greenopal.zargon.domain.graphics.SpriteParser
import com.greenopal.zargon.domain.graphics.TileParser
import com.greenopal.zargon.domain.story.NpcDialogProvider
import com.greenopal.zargon.ui.components.SpriteView
import com.greenopal.zargon.ui.components.StatsCard
import com.greenopal.zargon.ui.screens.BattleScreen
import com.greenopal.zargon.ui.screens.DialogScreen
import com.greenopal.zargon.ui.screens.GameOverScreen
import com.greenopal.zargon.ui.screens.HealerScreen
import com.greenopal.zargon.ui.screens.MapScreen
import com.greenopal.zargon.ui.screens.MenuScreen
import com.greenopal.zargon.ui.screens.QuestProgressScreen
import com.greenopal.zargon.ui.screens.StatsScreen
import com.greenopal.zargon.ui.screens.TitleScreen
import com.greenopal.zargon.ui.screens.VictoryScreen
import com.greenopal.zargon.ui.screens.WeaponShopScreen
import com.greenopal.zargon.ui.theme.ZargonTheme
import com.greenopal.zargon.ui.viewmodels.GameViewModel
import com.greenopal.zargon.ui.viewmodels.TileInteraction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

enum class ScreenState {
    TITLE, MENU, MAP, BATTLE, STATS, QUEST_PROGRESS, DIALOG, WEAPON_SHOP, HEALER, GAME_OVER, VICTORY
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var spriteParser: SpriteParser

    @Inject
    lateinit var tileParser: TileParser

    @Inject
    lateinit var dialogProvider: NpcDialogProvider

    @Inject
    lateinit var saveRepository: SaveGameRepository

    @Inject
    lateinit var tileBitmapCache: com.greenopal.zargon.domain.graphics.TileBitmapCache

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZargonTheme {
                val viewModel: GameViewModel = hiltViewModel()
                val gameState by viewModel.gameState.collectAsState()

                // Load sprites
                var playerSprite by remember { mutableStateOf<Sprite?>(null) }
                var monsterSprites by remember { mutableStateOf<Map<String, Sprite?>>(emptyMap()) }
                var spriteCount by remember { mutableStateOf(0) }
                var screenState by remember { mutableStateOf(ScreenState.TITLE) }
                var currentNpcType by remember { mutableStateOf<NpcType?>(null) }
                var isExplorationMode by remember { mutableStateOf(false) }

                LaunchedEffect(Unit) {
                    val sprites = spriteParser.parseAllSprites()
                    spriteCount = sprites.size

                    // Load player sprite (use oldman or placeholder)
                    playerSprite = sprites["oldman"] ?: spriteParser.createPlaceholderSprite("player")

                    // Load monster sprites
                    monsterSprites = mapOf(
                        "bat" to spriteParser.createBatSprite(),
                        "babble" to spriteParser.createBabbleSprite(),
                        "spook" to spriteParser.createSpookSprite(),
                        "slime" to spriteParser.createSlimeSprite(),
                        "demon" to sprites["demon"],
                        "snake" to sprites["snake"],
                        "necro" to sprites["necro"],
                        "kraken" to sprites["kraken"],
                        "ZARGON" to sprites["ZARGON"]
                    )
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (screenState) {
                        ScreenState.TITLE -> {
                            val saveSlots = remember { saveRepository.getAllSaves() }
                            TitleScreen(
                                saveSlots = saveSlots,
                                onNewGame = {
                                    // Reset to new game state and start exploration
                                    viewModel.newGame()
                                    isExplorationMode = true
                                    screenState = ScreenState.MAP
                                },
                                onContinue = { slot ->
                                    // Load saved game and start exploration
                                    saveRepository.loadGame(slot)?.let { savedState ->
                                        viewModel.updateGameState(savedState)
                                        isExplorationMode = true
                                        screenState = ScreenState.MAP
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        ScreenState.MENU -> {
                            MenuScreen(
                                onStartExploration = {
                                    isExplorationMode = true
                                    screenState = ScreenState.MAP
                                },
                                onStartBattleTest = {
                                    isExplorationMode = false
                                    screenState = ScreenState.BATTLE
                                },
                                onViewStats = {
                                    screenState = ScreenState.STATS
                                },
                                onViewQuestProgress = {
                                    screenState = ScreenState.QUEST_PROGRESS
                                },
                                onBack = {
                                    // Return to map when closing menu
                                    screenState = ScreenState.MAP
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        ScreenState.MAP -> {
                            MapScreen(
                                gameState = gameState,
                                playerSprite = playerSprite,
                                tileParser = tileParser,
                                tileBitmapCache = tileBitmapCache,
                                onEnterBattle = { encounterState ->
                                    // Update game state with encounter
                                    viewModel.updateGameState(encounterState)
                                    screenState = ScreenState.BATTLE
                                },
                                onInteract = { interaction ->
                                    when (interaction) {
                                        is TileInteraction.NpcDialog -> {
                                            currentNpcType = interaction.npcType
                                            screenState = ScreenState.DIALOG
                                        }
                                        is TileInteraction.WeaponShop -> {
                                            android.util.Log.d("MainActivity", "Entering shop - Position: World (${gameState.worldX}, ${gameState.worldY}), Char (${gameState.characterX}, ${gameState.characterY})")
                                            screenState = ScreenState.WEAPON_SHOP
                                        }
                                        is TileInteraction.Healer -> {
                                            android.util.Log.d("MainActivity", "Entering healer - Position: World (${gameState.worldX}, ${gameState.worldY}), Char (${gameState.characterX}, ${gameState.characterY})")
                                            screenState = ScreenState.HEALER
                                        }
                                        is TileInteraction.Castle -> {
                                            // Trigger final boss battle with ZARGON
                                            val bossState = gameState.copy(
                                                character = gameState.character.copy()
                                            )
                                            viewModel.updateGameState(bossState)
                                            isExplorationMode = false // Boss battle mode
                                            screenState = ScreenState.BATTLE
                                        }
                                    }
                                },
                                onOpenMenu = {
                                    screenState = ScreenState.MENU
                                },
                                onPositionChanged = { updatedState ->
                                    // Sync position changes from MapViewModel back to GameViewModel
                                    android.util.Log.d("MainActivity", "Position updated from MapViewModel: World (${updatedState.worldX}, ${updatedState.worldY}), Char (${updatedState.characterX}, ${updatedState.characterY})")
                                    viewModel.updateGameState(updatedState)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        ScreenState.BATTLE -> {
                            BattleScreen(
                                gameState = gameState,
                                playerSprite = playerSprite,
                                monsterSprites = monsterSprites,
                                onBattleEnd = { result, updatedGameState ->
                                    // Update game state
                                    android.util.Log.d("MainActivity", "Battle ended - Updated state gold: ${updatedGameState.character.gold}, XP: ${updatedGameState.character.experience}")
                                    android.util.Log.d("MainActivity", "Battle ended - Position: World (${updatedGameState.worldX}, ${updatedGameState.worldY}), Char (${updatedGameState.characterX}, ${updatedGameState.characterY})")
                                    viewModel.updateGameState(updatedGameState)
                                    android.util.Log.d("MainActivity", "After updateGameState - ViewModel state gold: ${viewModel.gameState.value.character.gold}")
                                    android.util.Log.d("MainActivity", "After updateGameState - Position: World (${viewModel.gameState.value.worldX}, ${viewModel.gameState.value.worldY}), Char (${viewModel.gameState.value.characterX}, ${viewModel.gameState.value.characterY})")

                                    // Handle battle result based on outcome
                                    when (result) {
                                        is BattleResult.Victory -> {
                                            // Check if ZARGON was defeated (player is at castle)
                                            val defeatedZargon = updatedGameState.worldX == 3 &&
                                                    updatedGameState.worldY == 2 &&
                                                    updatedGameState.characterX in 13..16 &&
                                                    updatedGameState.characterY in 4..6

                                            screenState = when {
                                                defeatedZargon -> ScreenState.VICTORY
                                                isExplorationMode -> ScreenState.MAP
                                                else -> ScreenState.MENU
                                            }
                                        }
                                        is BattleResult.Defeat -> {
                                            // Player died - game over
                                            screenState = ScreenState.GAME_OVER
                                        }
                                        is BattleResult.Fled -> {
                                            // Fled - return to map if exploring
                                            screenState = if (isExplorationMode) {
                                                ScreenState.MAP
                                            } else {
                                                ScreenState.MENU
                                            }
                                        }
                                        else -> {}
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        ScreenState.STATS -> {
                            StatsScreen(
                                gameState = gameState,
                                onBack = { screenState = ScreenState.MENU },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        ScreenState.QUEST_PROGRESS -> {
                            QuestProgressScreen(
                                gameState = gameState,
                                onBack = { screenState = ScreenState.MENU },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        ScreenState.DIALOG -> {
                            currentNpcType?.let { npcType ->
                                val dialog = dialogProvider.getDialog(npcType, gameState)
                                DialogScreen(
                                    npcType = npcType,
                                    dialog = dialog,
                                    gameState = gameState,
                                    onDialogEnd = { updatedState, storyAction ->
                                        // Handle story actions
                                        val finalState = when (storyAction) {
                                            is StoryAction.AdvanceStory -> {
                                                updatedState.updateStory(storyAction.newStatus)
                                            }
                                            is StoryAction.GiveItem -> {
                                                updatedState.addItem(
                                                    com.greenopal.zargon.data.models.Item(
                                                        name = storyAction.itemName,
                                                        description = "Story item",
                                                        type = com.greenopal.zargon.data.models.ItemType.KEY_ITEM
                                                    )
                                                )
                                            }
                                            is StoryAction.BuildBoat -> {
                                                updatedState
                                                    .updateStory(5.5f)
                                                    .addItem(
                                                        com.greenopal.zargon.data.models.Item(
                                                            name = "ship",
                                                            description = "Allows travel on the river",
                                                            type = com.greenopal.zargon.data.models.ItemType.KEY_ITEM
                                                        )
                                                    )
                                            }
                                            is StoryAction.ResurrectBoatman -> {
                                                updatedState.updateStory(5.0f)
                                            }
                                            else -> updatedState
                                        }
                                        viewModel.updateGameState(finalState)
                                        screenState = ScreenState.MAP
                                    },
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(innerPadding)
                                )
                            } ?: run {
                                screenState = ScreenState.MAP
                            }
                        }

                        ScreenState.WEAPON_SHOP -> {
                            WeaponShopScreen(
                                gameState = gameState,
                                onShopExit = { updatedState ->
                                    // Return player to where they were before entering
                                    android.util.Log.d("MainActivity", "Exiting shop - Position: World (${updatedState.worldX}, ${updatedState.worldY}), Char (${updatedState.characterX}, ${updatedState.characterY})")
                                    android.util.Log.d("MainActivity", "Exiting shop - Gold: ${updatedState.character.gold}")
                                    viewModel.updateGameState(updatedState)
                                    android.util.Log.d("MainActivity", "After shop exit - Position: World (${viewModel.gameState.value.worldX}, ${viewModel.gameState.value.worldY}), Char (${viewModel.gameState.value.characterX}, ${viewModel.gameState.value.characterY})")
                                    screenState = ScreenState.MAP
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        ScreenState.HEALER -> {
                            HealerScreen(
                                gameState = gameState,
                                onSaveGame = { stateToSave ->
                                    // Save the current state from healer (including updated HP/MP)
                                    android.util.Log.d("MainActivity", "Saving game - HP: ${stateToSave.character.currentDP}/${stateToSave.character.maxDP}, MP: ${stateToSave.character.currentMP}/${stateToSave.character.maxMP}")
                                    saveRepository.saveGame(stateToSave, 1)
                                },
                                onHealerExit = { updatedState ->
                                    // Return player to where they were before entering
                                    android.util.Log.d("MainActivity", "Exiting healer - Position: World (${updatedState.worldX}, ${updatedState.worldY}), Char (${updatedState.characterX}, ${updatedState.characterY})")
                                    android.util.Log.d("MainActivity", "Exiting healer - HP: ${updatedState.character.currentDP}/${updatedState.character.maxDP}")
                                    viewModel.updateGameState(updatedState)
                                    android.util.Log.d("MainActivity", "After healer exit - Position: World (${viewModel.gameState.value.worldX}, ${viewModel.gameState.value.worldY}), Char (${viewModel.gameState.value.characterX}, ${viewModel.gameState.value.characterY})")
                                    screenState = ScreenState.MAP
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        ScreenState.GAME_OVER -> {
                            GameOverScreen(
                                finalGameState = gameState,
                                onReturnToTitle = {
                                    viewModel.newGame()
                                    screenState = ScreenState.TITLE
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        ScreenState.VICTORY -> {
                            VictoryScreen(
                                finalGameState = gameState,
                                onReturnToTitle = {
                                    viewModel.newGame()
                                    screenState = ScreenState.TITLE
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }
                    }
                }
            }
        }
    }
}