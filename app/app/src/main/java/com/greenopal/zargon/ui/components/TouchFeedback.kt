package com.greenopal.zargon.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.greenopal.zargon.ui.theme.Gold
import com.greenopal.zargon.ui.theme.Ember

@Composable
fun rememberPressScale(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.96f,
): Float {
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue   = if (isPressed) pressedScale else 1f,
        animationSpec = if (isPressed) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessHigh)
        } else {
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        },
        label = "pressScale",
    )

    return scale
}

@Composable
fun rememberMedievalRipple() = ripple(
    bounded = true,
    color   = Gold,
)

@Composable
fun rememberEmberRipple() = ripple(
    bounded = true,
    color   = Ember,
)

@Composable
fun Modifier.medievalClickable(
    onClick:      () -> Unit,
    enabled:      Boolean = true,
    emberVariant: Boolean = false,
    enableHaptic: Boolean = true,
    pressedScale: Float   = 0.96f,
): Modifier {
    val interactionSource = remember { MutableInteractionSource() }
    val scale             = rememberPressScale(interactionSource, pressedScale)
    val ripple            = if (emberVariant) rememberEmberRipple() else rememberMedievalRipple()
    val haptic            = LocalHapticFeedback.current

    return this
        .graphicsLayer {
            scaleX = if (enabled) scale else 1f
            scaleY = if (enabled) scale else 1f
        }
        .indication(interactionSource, if (enabled) ripple else null)
        .clickable(
            interactionSource = interactionSource,
            indication        = null,
            enabled           = enabled,
            onClick           = {
                if (enableHaptic) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        )
}
