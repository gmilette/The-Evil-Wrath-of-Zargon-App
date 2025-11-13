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
import com.greenopal.zargon.domain.battle.BattleResult
import com.greenopal.zargon.domain.graphics.Sprite
import com.greenopal.zargon.domain.graphics.SpriteParser
import com.greenopal.zargon.ui.components.SpriteView
import com.greenopal.zargon.ui.components.StatsCard
import com.greenopal.zargon.ui.screens.BattleScreen
import com.greenopal.zargon.ui.theme.ZargonTheme
import com.greenopal.zargon.ui.viewmodels.GameViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var spriteParser: SpriteParser

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
                var inBattle by remember { mutableStateOf(false) }

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
                    if (inBattle) {
                        // Battle screen
                        BattleScreen(
                            gameState = gameState,
                            playerSprite = playerSprite,
                            monsterSprites = monsterSprites,
                            onBattleEnd = { result, updatedGameState ->
                                // Update game state
                                viewModel.updateGameState(updatedGameState)

                                // Show result message (could add dialog here)
                                when (result) {
                                    is BattleResult.Victory -> {
                                        // Handle victory (XP, gold - Phase 4)
                                    }
                                    is BattleResult.Defeat -> {
                                        // Handle defeat (game over)
                                    }
                                    is BattleResult.Fled -> {
                                        // Fled successfully
                                    }
                                    else -> {}
                                }

                                // Return to main screen
                                inBattle = false
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        )
                    } else {
                        // Main screen
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
                                    text = "The Evil Wrath of Zargon",
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

                                // Battle button
                                Button(
                                    onClick = { inBattle = true },
                                    modifier = Modifier.fillMaxWidth(0.6f)
                                ) {
                                    Text("Start Battle (Test)")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}