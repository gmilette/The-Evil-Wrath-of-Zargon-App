package com.greenopal.zargon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Damage number animation that floats upward and fades
 */
@Composable
fun DamageNumber(
    damage: Int,
    isHeal: Boolean = false,
    onComplete: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }

    val offsetY by animateFloatAsState(
        targetValue = if (visible) -50f else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        finishedListener = { onComplete() },
        label = "damageOffset"
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 0f else 1f,
        animationSpec = tween(durationMillis = 1000),
        label = "damageAlpha"
    )

    LaunchedEffect(Unit) {
        delay(50)
        visible = false
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isHeal) "+$damage" else "-$damage",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHeal) {
                Color.Green.copy(alpha = 1f - alpha)
            } else {
                Color.Red.copy(alpha = 1f - alpha)
            }
        )
    }
}

/**
 * Flash effect when taking damage
 */
@Composable
fun HitFlash(
    onComplete: () -> Unit
) {
    val alpha by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 300),
        finishedListener = { onComplete() },
        label = "flashAlpha"
    )

    LaunchedEffect(Unit) {
        // Trigger the animation
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(
            color = Color.Red.copy(alpha = 0.4f * (1f - alpha)),
            style = Fill
        )
    }
}

/**
 * Attack slash animation
 */
@Composable
fun AttackSlash(
    onComplete: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        finishedListener = { onComplete() },
        label = "slashProgress"
    )

    LaunchedEffect(Unit) {
        // Trigger animation
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val length = 150f * progress

        // Draw diagonal slash
        drawLine(
            color = Color.Yellow.copy(alpha = 1f - progress),
            start = androidx.compose.ui.geometry.Offset(
                centerX - length,
                centerY - length
            ),
            end = androidx.compose.ui.geometry.Offset(
                centerX + length,
                centerY + length
            ),
            strokeWidth = 8f
        )
    }
}

/**
 * Healing sparkle effect
 */
@Composable
fun HealingSparkles(
    onComplete: () -> Unit
) {
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        finishedListener = { onComplete() },
        label = "sparkleProgress"
    )

    LaunchedEffect(Unit) {
        // Trigger animation
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2
        val centerY = size.height / 2

        // Draw multiple sparkles
        for (i in 0..5) {
            val angle = (i * 60f + progress * 360f) * (Math.PI / 180f).toFloat()
            val radius = 80f * progress
            val x = centerX + radius * kotlin.math.cos(angle)
            val y = centerY + radius * kotlin.math.sin(angle)

            drawCircle(
                color = Color.Cyan.copy(alpha = (1f - progress) * 0.8f),
                radius = 8f,
                center = androidx.compose.ui.geometry.Offset(x, y)
            )
        }
    }
}
