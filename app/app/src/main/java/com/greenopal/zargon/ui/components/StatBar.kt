package com.greenopal.zargon.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Animated progress bar for HP/MP
 */
@Composable
fun StatBar(
    current: Int,
    max: Int,
    color: Color,
    backgroundColor: Color = Color.DarkGray,
    modifier: Modifier = Modifier
) {
    val percentage = if (max > 0) current.toFloat() / max.toFloat() else 0f

    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 500),
        label = "statBarAnimation"
    )

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .width(100.dp)
                .height(12.dp)
        ) {
            val width = size.width
            val height = size.height
            val cornerRadius = height / 2

            // Background
            drawRoundRect(
                color = backgroundColor,
                topLeft = Offset(0f, 0f),
                size = Size(width, height),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )

            // Foreground (fill)
            if (animatedPercentage > 0f) {
                drawRoundRect(
                    color = color,
                    topLeft = Offset(0f, 0f),
                    size = Size(width * animatedPercentage, height),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }

            // Border
            drawRoundRect(
                color = Color.Black,
                topLeft = Offset(0f, 0f),
                size = Size(width, height),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2f)
            )
        }
    }
}

/**
 * HP bar (red)
 */
@Composable
fun HPBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    StatBar(
        current = current,
        max = max,
        color = Color(0xFFFF3333),
        modifier = modifier
    )
}

/**
 * MP bar (blue)
 */
@Composable
fun MPBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    StatBar(
        current = current,
        max = max,
        color = Color(0xFF3333FF),
        modifier = modifier
    )
}

/**
 * XP bar (yellow/gold)
 */
@Composable
fun XPBar(
    current: Int,
    max: Int,
    modifier: Modifier = Modifier
) {
    StatBar(
        current = current,
        max = max,
        color = Color(0xFFFFD700),
        modifier = modifier
    )
}
