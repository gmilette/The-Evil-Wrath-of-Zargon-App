package com.greenopal.zargon.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.greenopal.zargon.R
import com.greenopal.zargon.data.models.GameState
import com.greenopal.zargon.data.models.Item
import com.greenopal.zargon.data.models.MapItems
import com.greenopal.zargon.data.models.PrestigeBonus
import com.greenopal.zargon.domain.graphics.Sprite
import com.greenopal.zargon.domain.map.GameMap
import com.greenopal.zargon.ui.components.DungeonBackground
import com.greenopal.zargon.ui.components.MedievalButton
import com.greenopal.zargon.ui.components.MedievalButtonVariant
import com.greenopal.zargon.ui.components.MedievalPanel
import com.greenopal.zargon.ui.components.PlayerHudBar
import com.greenopal.zargon.ui.components.WorldMagicMenu
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.GoldBright
import com.greenopal.zargon.ui.theme.PanelBg
import com.greenopal.zargon.ui.theme.Parchment
import com.greenopal.zargon.ui.viewmodels.MapViewModel
import com.greenopal.zargon.ui.viewmodels.TileInteraction

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

    var lastX by remember { mutableStateOf(gameState.characterX) }
    var lastY by remember { mutableStateOf(gameState.characterY) }
    var currentDirection by remember { mutableStateOf("front") }
    var lastInteractedPosition by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    val playerSprite = playerSprites[currentDirection] ?: playerSprites["front"]

    LaunchedEffect(gameState.characterX, gameState.characterY) {
        val newX = gameState.characterX
        val newY = gameState.characterY
        currentDirection = when {
            newY < lastY -> "back"
            newY > lastY -> "front"
            newX < lastX -> "left"
            newX > lastX -> "right"
            else         -> currentDirection
        }
        lastX = newX
        lastY = newY
    }

    LaunchedEffect(tileBitmapCache) {
        tileBitmapCache?.preloadCommonTiles(32)
    }

    LaunchedEffect(gameState.worldX, gameState.worldY) {
        viewModel.loadMap(gameState.worldX, gameState.worldY)
        viewModel.setGameState(gameState)
    }

    val currentMap by viewModel.currentMap.collectAsState()
    val currentGameState by viewModel.gameState.collectAsState()

    LaunchedEffect(currentGameState) {
        currentGameState?.let { state ->
            android.util.Log.d("MapScreen", "Position changed in MapViewModel - notifying MainActivity: World (${state.worldX}, ${state.worldY}), Char (${state.characterX}, ${state.characterY})")
            onPositionChanged(state)

            viewModel.checkForEncounter(state)?.let { encounterState ->
                onEnterBattle(encounterState)
            }

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

    DungeonBackground {
        Box(modifier = modifier.fillMaxSize()) {
            if (currentMap != null && currentGameState != null) {
                Column(
                    modifier            = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    PlayerHudBar(
                        name           = "JOE",
                        level          = currentGameState!!.character.level,
                        hp             = currentGameState!!.character.currentHP,
                        maxHp          = currentGameState!!.character.maxHP,
                        mp             = currentGameState!!.character.currentMP,
                        maxMp          = currentGameState!!.character.maxMP,
                        gold           = currentGameState!!.character.gold,
                        onMenuClick    = onOpenMenu,
                        onNameDoubleTap = { onEnterBattle(currentGameState!!) },
                    )

                    val itemMarkers = if (currentGameState!!.showMapItemMarkers) {
                        MapItems.getMarkersForWorld(
                            currentGameState!!.worldX,
                            currentGameState!!.worldY,
                            currentGameState!!.discoveredItems,
                            currentGameState!!.storyStatus
                        )
                    } else emptyList()

                    MapView(
                        map            = currentMap!!,
                        playerX        = currentGameState!!.characterX,
                        playerY        = currentGameState!!.characterY,
                        playerSprite   = playerSprite,
                        tileBitmapCache = tileBitmapCache,
                        itemMarkers    = itemMarkers,
                        modifier       = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )

                    MovementControls(
                        onMove = { direction -> viewModel.movePlayer(direction) },
                        onSearch = {
                            val item = viewModel.searchForItem()
                            foundItem = item ?: Item("", "Nothing found here", type = com.greenopal.zargon.data.models.ItemType.MISC)
                        },
                        onCast    = { showSpellMenu = true },
                        canCast   = currentGameState!!.challengeConfig?.isNoMagic != true,
                        modifier  = Modifier.fillMaxWidth(),
                    )
                }
            } else {
                Box(
                    modifier            = Modifier.fillMaxSize(),
                    contentAlignment    = Alignment.Center,
                ) {
                    Text(
                        text  = "Loading Map...",
                        style = MaterialTheme.typography.titleLarge.copy(color = GoldBright),
                    )
                }
            }

            if (foundItem != null) {
                ItemFoundDialog(
                    item      = foundItem!!,
                    onDismiss = { foundItem = null },
                )
            }

            if (showSpellMenu && currentGameState != null) {
                WorldMagicMenu(
                    spellLevel             = currentGameState!!.character.level,
                    currentMP              = currentGameState!!.character.currentMP,
                    hasMasterSpellbook     = currentGameState!!.prestigeData.isBonusActive(PrestigeBonus.MASTER_SPELLBOOK),
                    onSpellSelected = { spell ->
                        val (updatedState, message) = spell.cast(currentGameState!!)
                        viewModel.setGameState(updatedState)
                        onPositionChanged(updatedState)
                        showSpellMenu = false
                        spellResultMessage = message
                    },
                    onCancel = { showSpellMenu = false },
                )
            }

            if (spellResultMessage != null) {
                AlertDialog(
                    onDismissRequest = { spellResultMessage = null },
                    title = {
                        Text(
                            text      = "Spell Cast",
                            style     = MaterialTheme.typography.headlineSmall.copy(color = GoldBright),
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth(),
                        )
                    },
                    text = {
                        Text(
                            text      = spellResultMessage!!,
                            style     = MaterialTheme.typography.bodyLarge.copy(color = Parchment),
                            textAlign = TextAlign.Center,
                            modifier  = Modifier.fillMaxWidth(),
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { spellResultMessage = null }) {
                            Text("OK", style = MaterialTheme.typography.titleMedium.copy(color = Gold))
                        }
                    },
                    containerColor = PanelBg,
                )
            }
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
    itemMarkers: List<Pair<Int, Int>> = emptyList(),
    modifier: Modifier = Modifier,
) {
    MedievalPanel(
        modifier       = modifier,
        contentPadding = PaddingValues(4.dp),
    ) {
        Box(
            modifier         = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val tileWidth  = size.width / map.width
                val tileHeight = size.height / map.height
                val tileSize   = minOf(tileWidth, tileHeight)

                val mapWidth  = tileSize * map.width
                val mapHeight = tileSize * map.height
                val offsetX   = (size.width - mapWidth) / 2
                val offsetY   = (size.height - mapHeight) / 2

                val loggedTiles = mutableSetOf<String>()
                for (y in 0 until map.height) {
                    for (x in 0 until map.width) {
                        val tile = map.getTile(x, y)
                        if (tile != null) {
                            if (!loggedTiles.contains(tile.name)) {
                                android.util.Log.d("MapScreen", "Requesting tile: '${tile.name}' for TileType: ${tile}")
                                loggedTiles.add(tile.name)
                            }

                            val tileBitmap = tileBitmapCache?.getBitmap(tile.name, tileSize.toInt())
                            if (tileBitmap != null) {
                                drawIntoCanvas { canvas ->
                                    val left = offsetX + x * tileSize
                                    val top  = offsetY + y * tileSize
                                    val destRect = android.graphics.RectF(left, top, left + tileSize, top + tileSize)
                                    canvas.nativeCanvas.drawBitmap(tileBitmap, null, destRect, null)
                                }
                            } else {
                                drawRect(
                                    color   = tile.displayColor,
                                    topLeft = Offset(offsetX + x * tileSize, offsetY + y * tileSize),
                                    size    = Size(tileSize, tileSize),
                                )
                            }
                        }
                    }
                }

                for ((spotX, spotY) in itemMarkers) {
                    val cx     = offsetX + spotX * tileSize + tileSize / 2
                    val cy     = offsetY + spotY * tileSize + tileSize / 2
                    val radius = tileSize * 0.18f
                    drawCircle(color = Color(0xFFFFD700).copy(alpha = 0.8f), radius = radius,        center = Offset(cx, cy))
                    drawCircle(color = Color.White.copy(alpha = 0.9f),        radius = radius * 0.4f, center = Offset(cx, cy))
                }

                if (playerSprite != null) {
                    drawPlayerSprite(
                        sprite  = playerSprite,
                        x       = playerX,
                        y       = playerY,
                        tileSize = tileSize,
                        offsetX = offsetX,
                        offsetY = offsetY,
                    )
                }
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
    offsetY: Float,
) {
    val spriteSize    = tileSize * 0.8f
    val spritePadding = (tileSize - spriteSize) / 2
    val startX        = offsetX + x * tileSize + spritePadding
    val startY        = offsetY + y * tileSize + spritePadding
    val pixelWidth    = spriteSize / sprite.width
    val pixelHeight   = spriteSize / sprite.height

    for (py in 0 until sprite.height) {
        for (px in 0 until sprite.width) {
            val color = sprite.getPixel(px, py)
            if (color.alpha > 0f) {
                drawRect(
                    color   = color,
                    topLeft = Offset(startX + px * pixelWidth, startY + py * pixelHeight),
                    size    = Size(pixelWidth, pixelHeight),
                )
            }
        }
    }
}

@Composable
private fun MovementControls(
    onMove: (Direction) -> Unit,
    onSearch: () -> Unit,
    onCast: () -> Unit,
    canCast: Boolean = true,
    modifier: Modifier = Modifier,
) {
    MedievalPanel(
        modifier       = modifier,
        contentPadding = PaddingValues(12.dp),
    ) {
        Column(
            modifier            = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MedievalButton(
                onClick  = { onMove(Direction.UP) },
                modifier = Modifier.size(64.dp),
            ) {
                Text("↑", style = MaterialTheme.typography.headlineMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MedievalButton(
                    onClick  = { onMove(Direction.LEFT) },
                    modifier = Modifier.size(64.dp),
                ) {
                    Text("←", style = MaterialTheme.typography.headlineMedium)
                }
                MedievalButton(
                    onClick  = { onMove(Direction.DOWN) },
                    modifier = Modifier.size(64.dp),
                ) {
                    Text("↓", style = MaterialTheme.typography.headlineMedium)
                }
                MedievalButton(
                    onClick  = { onMove(Direction.RIGHT) },
                    modifier = Modifier.size(64.dp),
                ) {
                    Text("→", style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(Modifier.height(4.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MedievalButton(
                    onClick  = onSearch,
                    modifier = Modifier.weight(1f),
                    variant  = MedievalButtonVariant.Gold,
                ) {
                    Image(
                        painter            = painterResource(R.drawable.icon_search),
                        contentDescription = "Search",
                        modifier           = Modifier.size(36.dp),
                    )
                }
                MedievalButton(
                    onClick  = onCast,
                    modifier = Modifier.weight(1f),
                    variant  = if (canCast) MedievalButtonVariant.Gold else MedievalButtonVariant.Disabled,
                ) {
                    Image(
                        painter            = painterResource(R.drawable.icon_cast_map),
                        contentDescription = "Cast",
                        modifier           = Modifier.size(36.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemFoundDialog(
    item: Item,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text      = if (item.name.isEmpty()) "Search Result" else "Item Found!",
                style     = MaterialTheme.typography.headlineSmall.copy(color = GoldBright),
                textAlign = TextAlign.Center,
                modifier  = Modifier.fillMaxWidth(),
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier            = Modifier.fillMaxWidth(),
            ) {
                if (item.name.isNotEmpty()) {
                    Text(
                        text      = item.name.uppercase(),
                        style     = MaterialTheme.typography.titleMedium.copy(color = Gold),
                        textAlign = TextAlign.Center,
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Text(
                    text      = item.description,
                    style     = MaterialTheme.typography.bodyMedium.copy(color = Parchment),
                    textAlign = TextAlign.Center,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", style = MaterialTheme.typography.titleMedium.copy(color = Gold))
            }
        },
        containerColor = PanelBg,
    )
}

enum class Direction {
    UP, DOWN, LEFT, RIGHT
}
