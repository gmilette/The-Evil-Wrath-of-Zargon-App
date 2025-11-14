package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.domain.graphics.Sprite
import com.greenopal.zargon.domain.map.GameMap
import com.greenopal.zargon.ui.viewmodels.MapViewModel
import com.greenopal.zargon.ui.viewmodels.TileInteraction

/**
 * Map exploration screen showing tile grid and player
 * Based on QBASIC crossroad procedure (ZARGON.BAS:890)
 */
@Composable
fun MapScreen(
    gameState: GameState,
    playerSprite: Sprite?,
    onEnterBattle: (GameState) -> Unit,
    onInteract: (TileInteraction) -> Unit,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    // Initialize map
    LaunchedEffect(gameState.worldX, gameState.worldY) {
        viewModel.loadMap(gameState.worldX, gameState.worldY)
        viewModel.setGameState(gameState)
    }

    val currentMap by viewModel.currentMap.collectAsState()
    val currentGameState by viewModel.gameState.collectAsState()

    // Check for random encounters
    LaunchedEffect(currentGameState) {
        currentGameState?.let { state ->
            viewModel.checkForEncounter(state)?.let { encounterState ->
                onEnterBattle(encounterState)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (currentMap != null && currentGameState != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Header with stats and menu
                HeaderBar(
                    gameState = currentGameState!!,
                    onOpenMenu = onOpenMenu,
                    modifier = Modifier.fillMaxWidth()
                )

                // Map display
                MapView(
                    map = currentMap!!,
                    playerX = currentGameState!!.characterX,
                    playerY = currentGameState!!.characterY,
                    playerSprite = playerSprite,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Interact button (if on interactive tile)
                val currentInteraction = viewModel.getCurrentInteraction()
                if (currentInteraction != null) {
                    Button(
                        onClick = { onInteract(currentInteraction) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text(
                            text = when (currentInteraction) {
                                is TileInteraction.NpcDialog -> "Talk to ${currentInteraction.npcType.displayName}"
                                is TileInteraction.WeaponShop -> "Enter Weapon Shop"
                                is TileInteraction.Healer -> "Visit Healer"
                                is TileInteraction.Castle -> "Enter Castle"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Movement controls
                MovementControls(
                    onMove = { direction ->
                        viewModel.movePlayer(direction)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            // Loading
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Loading Map...",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun HeaderBar(
    gameState: GameState,
    onOpenMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Character stats
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        ) {
            Row(
                modifier = Modifier.padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "JOE Lv${gameState.character.level}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "HP:${gameState.character.currentDP}/${gameState.character.maxDP}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "MP:${gameState.character.currentMP}/${gameState.character.maxMP}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Gold:${gameState.character.gold}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFD700)
                )
            }
        }

        // Menu button
        Button(
            onClick = onOpenMenu,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("MENU")
        }
    }
}

@Composable
private fun MapView(
    map: GameMap,
    playerX: Int,
    playerY: Int,
    playerSprite: Sprite?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        ),
        border = BorderStroke(3.dp, MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val tileWidth = size.width / map.width
                val tileHeight = size.height / map.height
                val tileSize = minOf(tileWidth, tileHeight)

                // Calculate offset to center the map
                val mapWidth = tileSize * map.width
                val mapHeight = tileSize * map.height
                val offsetX = (size.width - mapWidth) / 2
                val offsetY = (size.height - mapHeight) / 2

                // Draw tiles
                for (y in 0 until map.height) {
                    for (x in 0 until map.width) {
                        val tile = map.getTile(x, y)
                        if (tile != null) {
                            drawRect(
                                color = tile.displayColor,
                                topLeft = Offset(
                                    offsetX + x * tileSize,
                                    offsetY + y * tileSize
                                ),
                                size = Size(tileSize, tileSize)
                            )
                        }
                    }
                }

                // Draw player sprite
                if (playerSprite != null) {
                    drawPlayerSprite(
                        sprite = playerSprite,
                        x = playerX,
                        y = playerY,
                        tileSize = tileSize,
                        offsetX = offsetX,
                        offsetY = offsetY
                    )
                }

                // Draw grid lines (optional, for clarity)
                drawGrid(
                    width = map.width,
                    height = map.height,
                    tileSize = tileSize,
                    offsetX = offsetX,
                    offsetY = offsetY
                )
            }
        }
    }
}

private fun DrawScope.drawPlayerSprite(
    sprite: Sprite,
    x: Int,
    y: Int,
    tileSize: Float,
    offsetX: Float,
    offsetY: Float
) {
    val spriteSize = tileSize * 0.8f // Make sprite slightly smaller than tile
    val spritePadding = (tileSize - spriteSize) / 2

    val startX = offsetX + x * tileSize + spritePadding
    val startY = offsetY + y * tileSize + spritePadding

    val pixelWidth = spriteSize / sprite.width
    val pixelHeight = spriteSize / sprite.height

    for (py in 0 until sprite.height) {
        for (px in 0 until sprite.width) {
            val color = sprite.getPixel(px, py)
            // Skip transparent pixels
            if (color.alpha > 0f) {
                drawRect(
                    color = color,
                    topLeft = Offset(
                        startX + px * pixelWidth,
                        startY + py * pixelHeight
                    ),
                    size = Size(pixelWidth, pixelHeight)
                )
            }
        }
    }
}

private fun DrawScope.drawGrid(
    width: Int,
    height: Int,
    tileSize: Float,
    offsetX: Float,
    offsetY: Float
) {
    val gridColor = Color.Gray.copy(alpha = 0.3f)

    // Vertical lines
    for (x in 0..width) {
        drawLine(
            color = gridColor,
            start = Offset(offsetX + x * tileSize, offsetY),
            end = Offset(offsetX + x * tileSize, offsetY + height * tileSize),
            strokeWidth = 1f
        )
    }

    // Horizontal lines
    for (y in 0..height) {
        drawLine(
            color = gridColor,
            start = Offset(offsetX, offsetY + y * tileSize),
            end = Offset(offsetX + width * tileSize, offsetY + y * tileSize),
            strokeWidth = 1f
        )
    }
}

@Composable
private fun MovementControls(
    onMove: (Direction) -> Unit,
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
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Up button
            DirectionButton(
                direction = Direction.UP,
                onClick = { onMove(Direction.UP) },
                text = "↑"
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Left, Down, Right buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DirectionButton(
                    direction = Direction.LEFT,
                    onClick = { onMove(Direction.LEFT) },
                    text = "←"
                )

                DirectionButton(
                    direction = Direction.DOWN,
                    onClick = { onMove(Direction.DOWN) },
                    text = "↓"
                )

                DirectionButton(
                    direction = Direction.RIGHT,
                    onClick = { onMove(Direction.RIGHT) },
                    text = "→"
                )
            }
        }
    }
}

@Composable
private fun DirectionButton(
    direction: Direction,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.size(80.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}
