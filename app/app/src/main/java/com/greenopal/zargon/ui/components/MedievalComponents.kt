package com.greenopal.zargon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.greenopal.zargon.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────
//  MODIFIER EXTENSIONS
// ─────────────────────────────────────────────────────────────────────────

fun Modifier.medievalPanel(cornerRadius: Dp = 3.dp): Modifier = this.drawBehind {
    val cr = cornerRadius.toPx()

    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(PanelLight, PanelBg, PanelMid),
            start  = Offset(0f, 0f),
            end    = Offset(size.width, size.height),
        ),
        cornerRadius = CornerRadius(cr),
    )

    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Gold.copy(alpha = 0.10f), Color.Transparent),
            startY = 0f, endY = size.height * 0.3f,
        ),
        cornerRadius = CornerRadius(cr),
    )

    drawRoundRect(
        color        = GoldDeep,
        cornerRadius = CornerRadius(cr),
        style        = Stroke(width = 1.dp.toPx()),
    )

    val inset2 = 1.dp.toPx()
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(GoldBright, Gold, GoldBright),
            start  = Offset(0f, 0f),
            end    = Offset(size.width, size.height),
        ),
        topLeft      = Offset(inset2, inset2),
        size         = Size(size.width - inset2 * 2, size.height - inset2 * 2),
        cornerRadius = CornerRadius((cr - inset2).coerceAtLeast(0f)),
        style        = Stroke(width = 1.dp.toPx()),
    )

    val inset3 = 2.dp.toPx()
    drawRoundRect(
        color        = GoldDark,
        topLeft      = Offset(inset3, inset3),
        size         = Size(size.width - inset3 * 2, size.height - inset3 * 2),
        cornerRadius = CornerRadius((cr - inset3).coerceAtLeast(0f)),
        style        = Stroke(width = 1.dp.toPx()),
    )

    drawRoundRect(
        color        = Gold.copy(alpha = 0.18f),
        topLeft      = Offset(-4.dp.toPx(), -4.dp.toPx()),
        size         = Size(size.width + 8.dp.toPx(), size.height + 8.dp.toPx()),
        cornerRadius = CornerRadius(cr + 4.dp.toPx()),
        style        = Stroke(width = 6.dp.toPx()),
    )
}

fun Modifier.emberPanel(cornerRadius: Dp = 3.dp): Modifier = this.drawBehind {
    val cr = cornerRadius.toPx()
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(EmberDark, Color(0xFF1A0800), EmberDark),
            start  = Offset(0f, 0f), end = Offset(size.width, size.height),
        ),
        cornerRadius = CornerRadius(cr),
    )
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(EmberBright, Ember, EmberBright),
            start  = Offset(0f, 0f), end = Offset(size.width, size.height),
        ),
        topLeft      = Offset(1.dp.toPx(), 1.dp.toPx()),
        size         = Size(size.width - 2.dp.toPx(), size.height - 2.dp.toPx()),
        cornerRadius = CornerRadius((cr - 1.dp.toPx()).coerceAtLeast(0f)),
        style        = Stroke(width = 1.dp.toPx()),
    )
    drawRoundRect(
        color        = Ember.copy(alpha = 0.25f),
        topLeft      = Offset(-3.dp.toPx(), -3.dp.toPx()),
        size         = Size(size.width + 6.dp.toPx(), size.height + 6.dp.toPx()),
        cornerRadius = CornerRadius(cr + 3.dp.toPx()),
        style        = Stroke(width = 5.dp.toPx()),
    )
}

// ─────────────────────────────────────────────────────────────────────────
//  PANEL
// ─────────────────────────────────────────────────────────────────────────

@Composable
fun MedievalPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 3.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    showCornerGems: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .medievalPanel(cornerRadius)
            .padding(contentPadding),
    ) {
        content()
        if (showCornerGems) {
            CornerGems()
        }
    }
}

@Composable
fun BoxScope.CornerGems() {
    val gemStyle = MaterialTheme.typography.labelSmall.copy(
        color    = GoldBright,
        fontSize = 12.sp,
    )
    Text("✦", style = gemStyle, modifier = Modifier.align(Alignment.TopStart).offset((-5).dp, (-5).dp))
    Text("✦", style = gemStyle, modifier = Modifier.align(Alignment.TopEnd).offset(5.dp, (-5).dp))
}

// ─────────────────────────────────────────────────────────────────────────
//  BUTTONS
// ─────────────────────────────────────────────────────────────────────────

enum class MedievalButtonVariant { Gold, Ember, Equipped, Disabled }

@Composable
fun MedievalButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    variant: MedievalButtonVariant = MedievalButtonVariant.Gold,
    subLabel: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val bgBrush = when (variant) {
        MedievalButtonVariant.Ember    -> Brush.verticalGradient(listOf(Color(0xFF2E1000), Color(0xFF1A0800)))
        MedievalButtonVariant.Equipped -> Brush.verticalGradient(listOf(Color(0xFF2A1E08), Color(0xFF1A1206)))
        else                           -> Brush.verticalGradient(listOf(PanelLight, PanelBg, Color(0xFF160E06)))
    }

    val borderBrush = when (variant) {
        MedievalButtonVariant.Ember    -> Brush.linearGradient(listOf(EmberBright, Ember, EmberBright))
        MedievalButtonVariant.Equipped -> Brush.linearGradient(listOf(GoldBright, Gold, GoldBright))
        MedievalButtonVariant.Disabled -> Brush.linearGradient(listOf(GoldDeep, GoldDeep))
        else                           -> Brush.linearGradient(listOf(GoldBright, Gold, GoldBright))
    }

    val alpha = if (variant == MedievalButtonVariant.Disabled) 0.4f else 1f

    Box(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .drawBehind {
                val cr = 3.dp.toPx()
                drawRoundRect(brush = bgBrush, cornerRadius = CornerRadius(cr))
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        listOf(Gold.copy(alpha = 0.18f), Color.Transparent),
                        startY = 0f, endY = size.height * 0.4f,
                    ),
                    cornerRadius = CornerRadius(cr),
                )
                drawRoundRect(
                    brush        = borderBrush,
                    cornerRadius = CornerRadius(cr),
                    style        = Stroke(width = 1.5.dp.toPx()),
                )
            }
            .medievalClickable(
                onClick      = onClick,
                enabled      = variant != MedievalButtonVariant.Disabled,
                emberVariant = variant == MedievalButtonVariant.Ember,
            )
            .padding(horizontal = 16.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            content()
            subLabel?.let {
                Spacer(Modifier.height(3.dp))
                Text(
                    text      = it,
                    style     = MaterialTheme.typography.bodySmall,
                    color     = ParchmentDim,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────
//  TYPOGRAPHY COMPONENTS
// ─────────────────────────────────────────────────────────────────────────

@Composable
fun GameTitle(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 30.sp,
) {
    Text(
        text      = text,
        modifier  = modifier,
        style     = MaterialTheme.typography.displayLarge.copy(
            fontSize  = fontSize,
            color     = GoldBright,
            shadow    = Shadow(
                color      = Gold.copy(alpha = 0.8f),
                blurRadius = 24f,
                offset     = Offset.Zero,
            ),
        ),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun ScreenHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text,
        modifier = modifier.fillMaxWidth(),
        style    = MaterialTheme.typography.headlineLarge.copy(
            color  = GoldBright,
            shadow = Shadow(color = Gold.copy(alpha = 0.5f), blurRadius = 16f, offset = Offset.Zero),
        ),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun PanelHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text     = text,
        modifier = modifier.fillMaxWidth(),
        style    = MaterialTheme.typography.headlineMedium.copy(
            color  = GoldBright,
            shadow = Shadow(color = Gold.copy(alpha = 0.4f), blurRadius = 10f, offset = Offset.Zero),
        ),
        textAlign = TextAlign.Center,
    )
}

@Composable
fun FlavorText(text: String, modifier: Modifier = Modifier) {
    Text(
        text      = text,
        modifier  = modifier.fillMaxWidth(),
        style     = MaterialTheme.typography.bodyMedium.copy(color = ParchmentDim),
        textAlign = TextAlign.Center,
    )
}

// ─────────────────────────────────────────────────────────────────────────
//  SEPARATOR
// ─────────────────────────────────────────────────────────────────────────

@Composable
fun OrnateSeparator(modifier: Modifier = Modifier) {
    Row(
        modifier          = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(Color.Transparent, GoldDark)))
        )
        Text(
            text  = " ◆ ",
            style = MaterialTheme.typography.labelSmall.copy(
                color  = Gold,
                fontSize = 10.sp,
                shadow = Shadow(color = Gold.copy(alpha = 0.8f), blurRadius = 6f, offset = Offset.Zero),
            ),
        )
        Box(
            Modifier
                .weight(1f)
                .height(1.dp)
                .background(Brush.horizontalGradient(listOf(GoldDark, Color.Transparent)))
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
//  SCROLL BANNER
// ─────────────────────────────────────────────────────────────────────────

@Composable
fun ScrollBanner(text: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color.Transparent, Color(0xFF3A2A10), Color(0xFF2E2208), Color(0xFF3A2A10), Color.Transparent)
                )
            )
            .border(
                width = Dp.Hairline,
                brush = Brush.horizontalGradient(listOf(Color.Transparent, GoldDark, Color.Transparent)),
                shape = RoundedCornerShape(0.dp),
            )
            .padding(vertical = 7.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text  = text.uppercase(),
            style = MaterialTheme.typography.headlineSmall.copy(
                color         = Parchment,
                letterSpacing = 0.12.sp,
                shadow        = Shadow(color = Color.Black, blurRadius = 4f, offset = Offset(1f, 1f)),
            ),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
//  STAT BAR
// ─────────────────────────────────────────────────────────────────────────

enum class StatBarType { HP, MP, XP }

@Composable
fun MedievalStatBar(
    current: Int,
    max: Int,
    type: StatBarType = StatBarType.HP,
    modifier: Modifier = Modifier,
    height: Dp = 10.dp,
) {
    val pct = if (max > 0) current.toFloat() / max.toFloat() else 0f
    val animatedPct by animateFloatAsState(
        targetValue   = pct.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 400, easing = EaseOut),
        label         = "statBar",
    )

    val (trackColor, fillBrush, glowColor) = when (type) {
        StatBarType.HP -> Triple(
            HpRedDark,
            Brush.horizontalGradient(listOf(HpRed, HpRedBright)),
            HpRed.copy(alpha = 0.6f),
        )
        StatBarType.MP -> Triple(
            Color(0xFF0A1A3A),
            Brush.horizontalGradient(listOf(MpBlue, MpBlueBright)),
            MpBlueBright.copy(alpha = 0.5f),
        )
        StatBarType.XP -> Triple(
            Color(0xFF0A1A08),
            Brush.horizontalGradient(listOf(XpGreen, XpGreenBright)),
            XpGreenBright.copy(alpha = 0.5f),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(1.dp))
            .background(trackColor)
            .border(Dp.Hairline, trackColor.copy(alpha = 0.5f), RoundedCornerShape(1.dp))
    ) {
        Box(
            Modifier
                .fillMaxWidth(animatedPct)
                .fillMaxHeight()
                .background(glowColor.copy(alpha = 0.3f))
        )
        Box(
            Modifier
                .fillMaxWidth(animatedPct)
                .fillMaxHeight()
                .background(fillBrush)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────
//  DUNGEON BACKGROUND
// ─────────────────────────────────────────────────────────────────────────

@Composable
fun DungeonBackground(content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF18100A), StoneBg, StoneDark),
                        center = Offset(size.width / 2f, size.height * 0.3f),
                        radius = size.width * 1.2f,
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Gold.copy(alpha = 0.04f), Color.Transparent),
                        startY = 0f, endY = size.height * 0.4f,
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Ember.copy(alpha = 0.04f)),
                        startY = size.height * 0.7f, endY = size.height,
                    )
                )
                val lineColor = Color.Black.copy(alpha = 0.07f)
                val blockH    = 48.dp.toPx()
                var y = blockH
                while (y < size.height) {
                    drawLine(lineColor, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                    y += blockH
                }
                val blockW = 72.dp.toPx()
                var row = 0
                var rowY = 0f
                while (rowY < size.height) {
                    val offset = if (row % 2 == 0) 0f else blockW / 2f
                    var x = offset
                    while (x < size.width) {
                        drawLine(lineColor, Offset(x, rowY), Offset(x, rowY + blockH), strokeWidth = 1f)
                        x += blockW
                    }
                    rowY += blockH
                    row++
                }
            },
    ) {
        content()
    }
}

// ─────────────────────────────────────────────────────────────────────────
//  HUD BAR
// ─────────────────────────────────────────────────────────────────────────

@Composable
fun PlayerHudBar(
    name: String,
    level: Int,
    hp: Int, maxHp: Int,
    mp: Int, maxMp: Int,
    gold: Int,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNameDoubleTap: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRect(
                    brush = Brush.horizontalGradient(listOf(PanelBg, Color(0xFF201408), PanelBg))
                )
                drawLine(GoldDark, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                drawLine(GoldDark, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                drawRect(Brush.horizontalGradient(listOf(Gold.copy(alpha = 0.06f), Color.Transparent, Gold.copy(alpha = 0.06f))))
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        val nameModifier = if (onNameDoubleTap != null) {
            Modifier.pointerInput(Unit) {
                detectTapGestures(onDoubleTap = { onNameDoubleTap() })
            }
        } else Modifier
        Text(
            text     = "$name Lv$level",
            style    = MaterialTheme.typography.titleSmall.copy(color = GoldBright, fontWeight = FontWeight.Bold),
            modifier = nameModifier,
        )
        HudPipe()
        Text(
            text  = "HP $hp/$maxHp",
            style = MaterialTheme.typography.titleSmall.copy(color = HpRedBright),
        )
        HudPipe()
        Text(
            text  = "MP $mp/$maxMp",
            style = MaterialTheme.typography.titleSmall.copy(color = MpBlueBright),
        )
        HudPipe()
        Text(
            text  = "Gold $gold",
            style = MaterialTheme.typography.titleSmall.copy(color = Gold),
        )
        Spacer(Modifier.weight(1f))
        Text(
            text     = "☰",
            style    = MaterialTheme.typography.titleMedium.copy(color = Gold),
            modifier = Modifier.clickable(onClick = onMenuClick),
        )
    }
}

@Composable
private fun HudPipe() {
    Text(
        text  = "|",
        style = MaterialTheme.typography.labelSmall.copy(color = GoldDark),
    )
}
