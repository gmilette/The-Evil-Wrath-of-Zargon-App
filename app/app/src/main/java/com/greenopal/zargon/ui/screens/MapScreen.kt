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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.Item
import com.greenopal.zargon.domain.graphics.Sprite
import com.greenopal.zargon.domain.map.GameMap
import com.greenopal.zargon.domain.map.TileType
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
    // State for found item dialog
    var foundItem by remember { mutableStateOf<Item?>(null) }

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

                // Map legend
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem("W", "Shop", Color(0xFF8B4513))
                        LegendItem("H", "Healer", Color(0xFFFF69B4))
                        LegendItem("h", "NPC", Color(0xFFAA5500))
                        LegendItem("C", "Castle", Color(0xFF4B0082))
                    }
                }

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
                    onSearch = {
                        val item = viewModel.searchForItem()
                        if (item != null) {
                            foundItem = item
                        } else {
                            foundItem = Item("", "Nothing found here", type = com.greenopal.zargon.data.models.ItemType.MISC)
                        }
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

        // Found item dialog
        if (foundItem != null) {
            ItemFoundDialog(
                item = foundItem!!,
                onDismiss = { foundItem = null }
            )
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

                            // Draw labels for special tiles
                            val label = when (tile) {
                                TileType.WEAPON_SHOP -> "W"
                                TileType.HEALER -> "H"
                                TileType.HUT -> "h"
                                TileType.CASTLE -> "C"
                                else -> null
                            }

                            if (label != null && tileSize > 20f) {
                                drawIntoCanvas { canvas ->
                                    val paint = android.graphics.Paint().apply {
                                        color = Color.White.toArgb()
                                        textSize = (tileSize * 0.7f).coerceAtMost(24f)
                                        textAlign = android.graphics.Paint.Align.CENTER
                                        isFakeBoldText = true
                                    }
                                    canvas.nativeCanvas.drawText(
                                        label,
                                        offsetX + x * tileSize + tileSize / 2,
                                        offsetY + y * tileSize + tileSize / 2 + paint.textSize / 3,
                                        paint
                                    )
                                }
                            }
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
    onSearch: () -> Unit,
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

            Spacer(modifier = Modifier.height(12.dp))

            // Search button
            Button(
                onClick = onSearch,
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Text("SEARCH (S)")
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

/**
 * Dialog shown when item is found
 * Based on QBASIC found() SUB display (ZARGON.BAS:1454-1485)
 */
@Composable
private fun ItemFoundDialog(
    item: Item,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (item.name.isEmpty()) "Search Result" else "Item Found!",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (item.name.isNotEmpty()) {
                    Text(
                        text = item.name.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    )
}

@Composable
private fun LegendItem(
    symbol: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}
