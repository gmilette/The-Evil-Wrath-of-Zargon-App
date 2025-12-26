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
import com.greenopal.zargon.domain.story.StoryProgressionChecker
import com.greenopal.zargon.ui.components.SpriteView
import com.greenopal.zargon.ui.components.StatsCard
import com.greenopal.zargon.ui.screens.BattleScreen
import com.greenopal.zargon.ui.screens.DialogScreen
import com.greenopal.zargon.ui.screens.FountainScreen
import com.greenopal.zargon.ui.screens.GameOverScreen
import com.greenopal.zargon.ui.screens.HealerScreen
import com.greenopal.zargon.ui.screens.HintsScreen
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
    TITLE, MENU, MAP, BATTLE, STATS, QUEST_PROGRESS, DIALOG, WEAPON_SHOP, HEALER, FOUNTAIN, GAME_OVER, VICTORY, HINTS
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var spriteParser: SpriteParser

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
                var playerSprites by remember { mutableStateOf<Map<String, Sprite?>>(emptyMap()) }
                var monsterSprites by remember { mutableStateOf<Map<String, Sprite?>>(emptyMap()) }
                var spriteCount by remember { mutableStateOf(0) }
                var screenState by remember { mutableStateOf(ScreenState.TITLE) }
                var currentNpcType by remember { mutableStateOf<NpcType?>(null) }
                var isExplorationMode by remember { mutableStateOf(false) }
                var saveSlots by remember { mutableStateOf(saveRepository.getAllSaves()) }

                LaunchedEffect(Unit) {
                    val sprites = spriteParser.parseAllSprites()
                    spriteCount = sprites.size

                    // Load all directional player sprites from drawable resources
                    playerSprites = mapOf(
                        "front" to tileBitmapCache.createSpriteFromDrawable("dude_front1"),
                        "back" to tileBitmapCache.createSpriteFromDrawable("dude_back1"),
                        "left" to tileBitmapCache.createSpriteFromDrawable("dude_sidel"),
                        "right" to tileBitmapCache.createSpriteFromDrawable("dude_sider")
                    )

                    // Default player sprite (front-facing)
                    playerSprite = playerSprites["front"] ?: spriteParser.createPlaceholderSprite("player")

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
                            TitleScreen(
                                saveSlots = saveSlots,
                                onNewGame = { slot ->
                                    android.util.Log.d("MainActivity", "Starting new game in slot $slot")
                                    viewModel.newGame(slot)
                                    isExplorationMode = true
                                    screenState = ScreenState.MAP
                                },
                                onContinue = { slot ->
                                    saveRepository.loadGame(slot)?.let { savedState ->
                                        viewModel.updateGameState(savedState)
                                        isExplorationMode = true
                                        screenState = ScreenState.MAP
                                    }
                                },
                                onDeleteSave = { slot ->
                                    saveRepository.deleteSave(slot)
                                    saveSlots = saveRepository.getAllSaves()
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
                                onViewHints = {
                                    screenState = ScreenState.HINTS
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
                                playerSprites = playerSprites,
                                tileBitmapCache = tileBitmapCache,
                                onEnterBattle = { encounterState ->
                                    // Update game state with encounter
                                    viewModel.updateGameState(encounterState)
                                    screenState = ScreenState.BATTLE
                                },
                                onInteract = { interaction ->
                                    when (interaction) {
                                        is TileInteraction.NpcDialog -> {
                                            // Check if it's a fountain - use fountain screen instead
                                            if (interaction.npcType == NpcType.FOUNTAIN) {
                                                android.util.Log.d("MainActivity", "Entering fountain - Position: World (${gameState.worldX}, ${gameState.worldY}), Char (${gameState.characterX}, ${gameState.characterY})")
                                                screenState = ScreenState.FOUNTAIN
                                            } else {
                                                currentNpcType = interaction.npcType
                                                screenState = ScreenState.DIALOG
                                            }
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

                                            // After battle victory, return to map (or victory screen if ZARGON defeated)
                                            screenState = if (defeatedZargon) {
                                                ScreenState.VICTORY
                                            } else {
                                                ScreenState.MAP
                                            }
                                        }
                                        is BattleResult.Defeat -> {
                                            // Player died - game over
                                            screenState = ScreenState.GAME_OVER
                                        }
                                        is BattleResult.Fled -> {
                                            // Fled - return to map
                                            screenState = ScreenState.MAP
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
                                DialogScreen(
                                    npcType = npcType,
                                    dialogProvider = dialogProvider,
                                    gameState = gameState,
                                    onDialogEnd = { updatedState, storyAction ->
                                        android.util.Log.d("MainActivity", "Dialog ended with story action: $storyAction")
                                        android.util.Log.d("MainActivity", "Current story status: ${updatedState.storyStatus}")

                                        // Handle story actions
                                        val finalState = when (storyAction) {
                                            is StoryAction.AdvanceStory -> {
                                                android.util.Log.d("MainActivity", "Advancing story from ${updatedState.storyStatus} to ${storyAction.newStatus}")
                                                updatedState.updateStory(storyAction.newStatus)
                                            }
                                            is StoryAction.GiveItem -> {
                                                val stateWithItem = updatedState.addItem(
                                                    com.greenopal.zargon.data.models.Item(
                                                        name = storyAction.itemName,
                                                        description = "Story item",
                                                        type = com.greenopal.zargon.data.models.ItemType.KEY_ITEM
                                                    )
                                                )
                                                // Check if story should auto-advance
                                                StoryProgressionChecker.checkAndAdvanceStory(stateWithItem)
                                            }
                                            is StoryAction.TakeItem -> {
                                                // Remove item from inventory
                                                val itemToRemove = updatedState.inventory.find {
                                                    it.name.equals(storyAction.itemName, ignoreCase = true)
                                                }
                                                if (itemToRemove != null) {
                                                    updatedState.removeItem(itemToRemove)
                                                } else {
                                                    updatedState
                                                }
                                            }
                                            is StoryAction.HealPlayer -> {
                                                // Fully restore HP and MP
                                                val healedCharacter = updatedState.character.copy(
                                                    currentDP = updatedState.character.maxDP,
                                                    currentMP = updatedState.character.maxMP
                                                )
                                                android.util.Log.d("MainActivity", "Fountain healing - HP: ${healedCharacter.currentDP}/${healedCharacter.maxDP}, MP: ${healedCharacter.currentMP}/${healedCharacter.maxMP}")
                                                updatedState.updateCharacter(healedCharacter)
                                            }
                                            is StoryAction.BuildBoat -> {
                                                val stateWithShip = updatedState
                                                    .updateStory(5.5f)
                                                    .addItem(
                                                        com.greenopal.zargon.data.models.Item(
                                                            name = "ship",
                                                            description = "Allows travel on the river",
                                                            type = com.greenopal.zargon.data.models.ItemType.KEY_ITEM
                                                        )
                                                    )
                                                // Check if story should auto-advance
                                                StoryProgressionChecker.checkAndAdvanceStory(stateWithShip)
                                            }
                                            is StoryAction.ResurrectBoatman -> {
                                                updatedState.updateStory(5.0f)
                                            }
                                            is StoryAction.IncreaseAttack -> {
                                                if (updatedState.character.gold >= storyAction.cost) {
                                                    val updatedCharacter = updatedState.character.copy(
                                                        gold = updatedState.character.gold - storyAction.cost,
                                                        baseAP = updatedState.character.baseAP + 1
                                                    )
                                                    updatedState.updateCharacter(updatedCharacter)
                                                } else {
                                                    updatedState
                                                }
                                            }
                                            is StoryAction.IncreaseDefense -> {
                                                if (updatedState.character.gold >= storyAction.cost) {
                                                    val updatedCharacter = updatedState.character.copy(
                                                        gold = updatedState.character.gold - storyAction.cost,
                                                        baseDP = updatedState.character.baseDP + 1,
                                                        currentDP = updatedState.character.currentDP + 1
                                                    )
                                                    updatedState.updateCharacter(updatedCharacter)
                                                } else {
                                                    updatedState
                                                }
                                            }
                                            is StoryAction.MultiAction -> {
                                                // Process multiple actions in sequence
                                                val finalMultiState = storyAction.actions.fold(updatedState) { currentState, action ->
                                                    when (action) {
                                                        is StoryAction.AdvanceStory -> currentState.updateStory(action.newStatus)
                                                        is StoryAction.GiveItem -> {
                                                            val withItem = currentState.addItem(
                                                                com.greenopal.zargon.data.models.Item(
                                                                    name = action.itemName,
                                                                    description = "Story item",
                                                                    type = com.greenopal.zargon.data.models.ItemType.KEY_ITEM
                                                                )
                                                            )
                                                            // Check story progression after giving item
                                                            StoryProgressionChecker.checkAndAdvanceStory(withItem)
                                                        }
                                                        is StoryAction.TakeItem -> {
                                                            val itemToRemove = currentState.inventory.find {
                                                                it.name.equals(action.itemName, ignoreCase = true)
                                                            }
                                                            if (itemToRemove != null) currentState.removeItem(itemToRemove) else currentState
                                                        }
                                                        is StoryAction.HealPlayer -> {
                                                            val healedChar = currentState.character.copy(
                                                                currentDP = currentState.character.maxDP,
                                                                currentMP = currentState.character.maxMP
                                                            )
                                                            currentState.updateCharacter(healedChar)
                                                        }
                                                        is StoryAction.BuildBoat -> {
                                                            val withShip = currentState.updateStory(5.5f).addItem(
                                                                com.greenopal.zargon.data.models.Item(name = "ship", description = "Allows travel on the river", type = com.greenopal.zargon.data.models.ItemType.KEY_ITEM)
                                                            )
                                                            // Check story progression after building boat
                                                            StoryProgressionChecker.checkAndAdvanceStory(withShip)
                                                        }
                                                        is StoryAction.ResurrectBoatman -> currentState.updateStory(5.0f)
                                                        is StoryAction.IncreaseAttack -> {
                                                            if (currentState.character.gold >= action.cost) {
                                                                val updatedChar = currentState.character.copy(
                                                                    gold = currentState.character.gold - action.cost,
                                                                    baseAP = currentState.character.baseAP + 1
                                                                )
                                                                currentState.updateCharacter(updatedChar)
                                                            } else {
                                                                currentState
                                                            }
                                                        }
                                                        is StoryAction.IncreaseDefense -> {
                                                            if (currentState.character.gold >= action.cost) {
                                                                val updatedChar = currentState.character.copy(
                                                                    gold = currentState.character.gold - action.cost,
                                                                    baseDP = currentState.character.baseDP + 1,
                                                                    currentDP = currentState.character.currentDP + 1
                                                                )
                                                                currentState.updateCharacter(updatedChar)
                                                            } else {
                                                                currentState
                                                            }
                                                        }
                                                        else -> currentState
                                                    }
                                                }
                                                // Final story progression check after all multi-actions
                                                StoryProgressionChecker.checkAndAdvanceStory(finalMultiState)
                                            }
                                            else -> updatedState
                                        }
                                        android.util.Log.d("MainActivity", "Final story status after actions: ${finalState.storyStatus}")
                                        viewModel.updateGameState(finalState)

                                        // Always return to map after dialog ends
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
                                    android.util.Log.d("MainActivity", "Saving game to slot ${stateToSave.saveSlot} - HP: ${stateToSave.character.currentDP}/${stateToSave.character.maxDP}, MP: ${stateToSave.character.currentMP}/${stateToSave.character.maxMP}")
                                    saveRepository.saveGame(stateToSave, stateToSave.saveSlot)
                                    saveSlots = saveRepository.getAllSaves()
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

                        ScreenState.FOUNTAIN -> {
                            FountainScreen(
                                gameState = gameState,
                                onSaveGame = { stateToSave ->
                                    android.util.Log.d("MainActivity", "Saving game to slot ${stateToSave.saveSlot} at fountain - Position: World (${stateToSave.worldX}, ${stateToSave.worldY}), Char (${stateToSave.characterX}, ${stateToSave.characterY})")
                                    android.util.Log.d("MainActivity", "Saving game - HP: ${stateToSave.character.currentDP}/${stateToSave.character.maxDP}, MP: ${stateToSave.character.currentMP}/${stateToSave.character.maxMP}")
                                    saveRepository.saveGame(stateToSave, stateToSave.saveSlot)
                                    saveSlots = saveRepository.getAllSaves()
                                },
                                onFountainExit = { updatedState ->
                                    // Return player to where they were before entering
                                    android.util.Log.d("MainActivity", "Exiting fountain - Position: World (${updatedState.worldX}, ${updatedState.worldY}), Char (${updatedState.characterX}, ${updatedState.characterY})")
                                    android.util.Log.d("MainActivity", "Exiting fountain - HP: ${updatedState.character.currentDP}/${updatedState.character.maxDP}")
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

                        ScreenState.VICTORY -> {
                            VictoryScreen(
                                finalGameState = gameState,
                                onReturnToTitle = {
                                    viewModel.newGame()
                                    screenState = ScreenState.TITLE
                                },
                                onReturnToGEF = { updatedGameState ->
                                    // Update game state with new position and Zargon trophy
                                    viewModel.updateGameState(updatedGameState)
                                    // Return to map screen
                                    screenState = ScreenState.MAP
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            )
                        }

                        ScreenState.HINTS -> {
                            HintsScreen(
                                onBack = {
                                    screenState = ScreenState.MENU
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