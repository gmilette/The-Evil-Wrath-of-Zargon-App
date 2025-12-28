package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.hilt.navigation.compose.hiltViewModel
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.Item
import com.greenopal.zargon.domain.graphics.Sprite
import com.greenopal.zargon.domain.map.GameMap
import com.greenopal.zargon.domain.map.TileType
import com.greenopal.zargon.ui.viewmodels.MapViewModel
import com.greenopal.zargon.ui.viewmodels.TileInteraction
import com.greenopal.zargon.ui.components.WorldMagicMenu
import com.greenopal.zargon.domain.world.WorldSpell
import javax.inject.Inject

/**
 * Map exploration screen showing tile grid and player
 * Based on QBASIC crossroad procedure (ZARGON.BAS:890)
 */
@Composable
fun MapScreen(
    gameState: GameState,
    playerSprites: Map<String, Sprite?>,
    tileBitmapCache: com.greenopal.zargon.domain.graphics.TileBitmapCache?,
    onEnterBattle: (GameState) -> Unit,
    onInteract: (TileInteraction) -> Unit,
    onOpenMenu: () -> Unit,
    onPositionChanged: (GameState) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: MapViewModel = hiltViewModel()
) {
    var foundItem by remember { mutableStateOf<Item?>(null) }

    var showSpellMenu by remember { mutableStateOf(false) }
    var spellResultMessage by remember { mutableStateOf<String?>(null) }

    var lastShopClickTime by remember { mutableStateOf(0L) }

    // Track last position to determine movement direction
    var lastX by remember { mutableStateOf(gameState.characterX) }
    var lastY by remember { mutableStateOf(gameState.characterY) }
    var currentDirection by remember { mutableStateOf("front") }
    var lastInteractedPosition by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Select sprite based on direction
    val playerSprite = playerSprites[currentDirection] ?: playerSprites["front"]

    // Update direction when position changes
    LaunchedEffect(gameState.characterX, gameState.characterY) {
        val newX = gameState.characterX
        val newY = gameState.characterY

        // Determine direction based on movement
        currentDirection = when {
            newY < lastY -> "back"      // Moved up
            newY > lastY -> "front"     // Moved down
            newX < lastX -> "left"      // Moved left
            newX > lastX -> "right"     // Moved right
            else -> currentDirection    // No movement, keep current direction
        }

        // Update last position
        lastX = newX
        lastY = newY
    }

    // Preload tile bitmaps for better performance
    LaunchedEffect(tileBitmapCache) {
        tileBitmapCache?.preloadCommonTiles(32)
    }

    // Initialize map
    LaunchedEffect(gameState.worldX, gameState.worldY) {
        viewModel.loadMap(gameState.worldX, gameState.worldY)
        viewModel.setGameState(gameState)
    }

    val currentMap by viewModel.currentMap.collectAsState()
    val currentGameState by viewModel.gameState.collectAsState()

    // Sync position changes back to MainActivity
    LaunchedEffect(currentGameState) {
        currentGameState?.let { state ->
            android.util.Log.d("MapScreen", "Position changed in MapViewModel - notifying MainActivity: World (${state.worldX}, ${state.worldY}), Char (${state.characterX}, ${state.characterY})")
            onPositionChanged(state)

            // Check for random encounters
            viewModel.checkForEncounter(state)?.let { encounterState ->
                onEnterBattle(encounterState)
            }

            // Auto-interact with huts when player moves onto them
            val currentPosition = Pair(state.characterX, state.characterY)
            if (currentPosition != lastInteractedPosition) {
                val interaction = viewModel.getCurrentInteraction()
                if (interaction != null) {
                    android.util.Log.d("MapScreen", "Auto-entering hut at position $currentPosition")
                    lastInteractedPosition = currentPosition
                    onInteract(interaction)
                }
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
                    onEnterBattle = onEnterBattle,
                    modifier = Modifier.fillMaxWidth()
                )


                // Map display
                MapView(
                    map = currentMap!!,
                    playerX = currentGameState!!.characterX,
                    playerY = currentGameState!!.characterY,
                    playerSprite = playerSprite,
                    tileBitmapCache = tileBitmapCache,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

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
                    onCast = {
                        showSpellMenu = true
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

        // Spell casting menu
        if (showSpellMenu && currentGameState != null) {
            WorldMagicMenu(
                spellLevel = currentGameState!!.character.level,
                currentMP = currentGameState!!.character.currentMP,
                onSpellSelected = { spell ->
                    val (updatedState, message) = spell.cast(currentGameState!!)
                    viewModel.setGameState(updatedState)
                    onPositionChanged(updatedState)
                    showSpellMenu = false
                    spellResultMessage = message
                },
                onCancel = { showSpellMenu = false }
            )
        }

        // Spell result dialog
        if (spellResultMessage != null) {
            AlertDialog(
                onDismissRequest = { spellResultMessage = null },
                title = {
                    Text(
                        text = "Spell Cast",
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Text(
                        text = spellResultMessage!!,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { spellResultMessage = null }) {
                        Text("OK")
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            )
        }
    }
}

@Composable
private fun HeaderBar(
    gameState: GameState,
    onOpenMenu: () -> Unit,
    onEnterBattle: (GameState) -> Unit,
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
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                android.util.Log.d("MapScreen", "Double-clicked on JOE - triggering battle")
                                onEnterBattle(gameState)
                            }
                        )
                    }
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
        IconButton(
            onClick = onOpenMenu,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
private fun MapView(
    map: GameMap,
    playerX: Int,
    playerY: Int,
    playerSprite: Sprite?,
    tileBitmapCache: com.greenopal.zargon.domain.graphics.TileBitmapCache? = null,
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
                var loggedTiles = mutableSetOf<String>()
                for (y in 0 until map.height) {
                    for (x in 0 until map.width) {
                        val tile = map.getTile(x, y)
                        if (tile != null) {
                            // Log unique tile names being requested (only once per type)
                            if (!loggedTiles.contains(tile.name)) {
                                android.util.Log.d("MapScreen", "Requesting tile: '${tile.name}' for TileType: ${tile}")
                                loggedTiles.add(tile.name)
                            }

                            // Try to use textured bitmap from cache, fall back to colored rectangle
                            val tileBitmap = tileBitmapCache?.getBitmap(tile.name, tileSize.toInt())

                            if (tileBitmap != null) {
                                // Draw textured bitmap tile, stretching to fill exact tile size
                                drawIntoCanvas { canvas ->
                                    val left = offsetX + x * tileSize
                                    val top = offsetY + y * tileSize
                                    val destRect = android.graphics.RectF(
                                        left,
                                        top,
                                        left + tileSize,
                                        top + tileSize
                                    )
                                    canvas.nativeCanvas.drawBitmap(
                                        tileBitmap,
                                        null,
                                        destRect,
                                        null
                                    )
                                }
                            } else {
                                // Fallback to colored rectangle if bitmap not available
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

                // Grid lines disabled - tiles now fill entire grid cells
                // drawGrid(
                //     width = map.width,
                //     height = map.height,
                //     tileSize = tileSize,
                //     offsetX = offsetX,
                //     offsetY = offsetY
                // )
            }
        }
    }
}

private fun DrawScope.drawTileSprite(
    sprite: Sprite,
    x: Int,
    y: Int,
    tileSize: Float,
    offsetX: Float,
    offsetY: Float
) {
    val startX = offsetX + x * tileSize
    val startY = offsetY + y * tileSize

    val pixelWidth = tileSize / sprite.width
    val pixelHeight = tileSize / sprite.height

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

private fun getTileSpriteForType(tile: TileType, sprites: Map<String, Sprite>): Sprite? {
    return when (tile) {
        TileType.GRASS -> sprites["GRASS"] ?: sprites["Grass"]
        TileType.SAND -> sprites["SAND"] ?: sprites["Sand"]
        TileType.TREE, TileType.TREE2 -> sprites["TREE"] ?: sprites["Trees1"]
        TileType.ROCK, TileType.ROCK2 -> sprites["ROCK"] ?: sprites["Rock-1"]
        TileType.WATER -> sprites["WATER"] ?: sprites["Water"]
        TileType.GRAVE -> sprites["GRAVE"] ?: sprites["Gravestone"]
        else -> null
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
    onCast: () -> Unit,
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

            // Search and Cast buttons
            Row(
                modifier = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSearch,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("SEARCH")
                }

                Button(
                    onClick = onCast,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Text("CAST")
                }
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
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        )
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
