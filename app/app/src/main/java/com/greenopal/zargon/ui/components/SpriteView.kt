package com.greenopal.zargon.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.greenopal.zargon.domain.graphics.Sprite

/**
 * Renders a sprite using Canvas.
 * Scales the sprite to fit the given size while maintaining pixel-perfect appearance.
 */
@Composable
fun SpriteView(
    sprite: Sprite?,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    backgroundColor: Color = Color.Black,
    showBorder: Boolean = true
) {
    Box(
        modifier = modifier
            .size(size)
            .then(
                if (showBorder) Modifier.border(1.dp, MaterialTheme.colorScheme.primary)
                else Modifier
            )
            .background(backgroundColor)
    ) {
        if (sprite != null) {
            Canvas(modifier = Modifier.size(size)) {
                val canvasWidth = this.size.width
                val canvasHeight = this.size.height

                // Calculate pixel size for scaling
                val pixelWidth = canvasWidth / sprite.width
                val pixelHeight = canvasHeight / sprite.height

                // Draw each pixel as a rectangle
                for (y in 0 until sprite.height) {
                    for (x in 0 until sprite.width) {
                        val color = sprite.getPixel(x, y)

                        // Skip transparent pixels
                        if (color.alpha > 0f) {
                            drawRect(
                                color = color,
                                topLeft = Offset(
                                    x = x * pixelWidth,
                                    y = y * pixelHeight
                                ),
                                size = Size(pixelWidth, pixelHeight)
                            )
                        }
                    }
                }
            }
        }
    }
}
