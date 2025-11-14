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
import com.greenopal.zargon.domain.story.NpcDialogProvider
import com.greenopal.zargon.ui.components.SpriteView
import com.greenopal.zargon.ui.components.StatsCard
import com.greenopal.zargon.ui.screens.BattleScreen
import com.greenopal.zargon.ui.screens.DialogScreen
import com.greenopal.zargon.ui.screens.GameOverScreen
import com.greenopal.zargon.ui.screens.HealerScreen
import com.greenopal.zargon.ui.screens.MapScreen
import com.greenopal.zargon.ui.screens.MenuScreen
import com.greenopal.zargon.ui.screens.TitleScreen
import com.greenopal.zargon.ui.screens.WeaponShopScreen
import com.greenopal.zargon.ui.theme.ZargonTheme
import com.greenopal.zargon.ui.viewmodels.GameViewModel
import com.greenopal.zargon.ui.viewmodels.TileInteraction
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

enum class ScreenState {
    TITLE, MENU, MAP, BATTLE, STATS, DIALOG, WEAPON_SHOP, HEALER, GAME_OVER
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var spriteParser: SpriteParser

    @Inject
    lateinit var dialogProvider: NpcDialogProvider

    @Inject
    lateinit var saveRepository: SaveGameRepository

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
                        "bat" to sprites["demon"], // Using demon sprite as placeholder
                        "babble" to sprites["demon"],
                        "spook" to sprites["demon"],
                        "slime" to sprites["demon"],
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
                                    // Reset to new game state
                                    viewModel.newGame()
                                    screenState = ScreenState.MENU
                                },
                                onContinue = { slot ->
                                    // Load saved game
                                    saveRepository.loadGame(slot)?.let { savedState ->
                                        viewModel.updateGameState(savedState)
                                        screenState = ScreenState.MENU
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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        ScreenState.MAP -> {
                            MapScreen(
                                gameState = gameState,
                                playerSprite = playerSprite,
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
                                            screenState = ScreenState.WEAPON_SHOP
                                        }
                                        is TileInteraction.Healer -> {
                                            screenState = ScreenState.HEALER
                                        }
                                        is TileInteraction.Castle -> {
                                            // TODO: Implement castle/final boss
                                            screenState = ScreenState.MENU
                                        }
                                    }
                                },
                                onOpenMenu = {
                                    screenState = ScreenState.MENU
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
                                    viewModel.updateGameState(updatedGameState)

                                    // Handle battle result based on outcome
                                    when (result) {
                                        is BattleResult.Victory -> {
                                            // Return to map if exploring, menu if testing
                                            screenState = if (isExplorationMode) {
                                                ScreenState.MAP
                                            } else {
                                                ScreenState.MENU
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
                            // Stats screen
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "Character Stats",
                                        style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Display sprite
                                    SpriteView(
                                        sprite = playerSprite,
                                        size = 150.dp
                                    )

                                    if (spriteCount > 0) {
                                        Text("Loaded $spriteCount sprites from bomb.sht")
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Display stats
                                    StatsCard(
                                        stats = gameState.character,
                                        modifier = Modifier.padding(16.dp)
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Back button
                                    Button(
                                        onClick = { screenState = ScreenState.MENU },
                                        modifier = Modifier.fillMaxWidth(0.6f)
                                    ) {
                                        Text("Back to Menu")
                                    }
                                }
                            }
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
                                                updatedState.updateStory(5.5f)
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
                                    viewModel.updateGameState(updatedState)
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
                                onSaveGame = {
                                    saveRepository.saveGame(gameState, 1)
                                },
                                onHealerExit = { updatedState ->
                                    viewModel.updateGameState(updatedState)
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
                    }
                }
            }
        }
    }
}