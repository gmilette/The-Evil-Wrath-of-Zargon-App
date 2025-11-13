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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.greenopal.zargon.domain.graphics.Sprite
import com.greenopal.zargon.domain.graphics.SpriteParser
import com.greenopal.zargon.ui.components.SpriteView
import com.greenopal.zargon.ui.components.StatsCard
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
                var sprite by remember { mutableStateOf<Sprite?>(null) }
                var spriteCount by remember { mutableStateOf(0) }

                LaunchedEffect(Unit) {
                    val sprites = spriteParser.parseAllSprites()
                    spriteCount = sprites.size
                    // Try to load oldman sprite, or use placeholder
                    sprite = sprites["oldman"] ?: spriteParser.createPlaceholderSprite("player")
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
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
                                sprite = sprite,
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
                        }
                    }
                }
            }
        }
    }
}