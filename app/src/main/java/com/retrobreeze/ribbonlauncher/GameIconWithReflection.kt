package com.retrobreeze.ribbonlauncher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.retrobreeze.ribbonlauncher.util.isIconLikelyCircular

/**
 * Displays a game icon with a subtle reflection beneath it.
 */
@Composable
fun GameIconWithReflection(
    icon: Drawable,
    contentDesc: String,
    modifier: Modifier = Modifier,
    iconSize: Dp = 100.dp,
    reflectionRatio: Float = 0.4f
) {
    val isCircular = isIconLikelyCircular(icon)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main icon
        Box(
            modifier = Modifier.size(iconSize),
            contentAlignment = Alignment.Center
        ) {
            if (isCircular) {
                GameIconFancy(icon = icon, contentDesc = contentDesc)
            } else {
                GameIconSimple(icon = icon, contentDesc = contentDesc)
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Reflection
        Box(
            modifier = Modifier
                .size(iconSize * reflectionRatio)
                .clipToBounds()
                .graphicsLayer {
                    scaleY = -1f
                    // flip around the top edge
                    transformOrigin = TransformOrigin(0.5f, 0f)
                }
                .drawWithContent {
                    drawContent()
                    drawRect(
                        brush = Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.4f),
                            1f to Color.Transparent
                        ),
                        blendMode = BlendMode.DstIn
                    )
                },
            contentAlignment = Alignment.TopCenter
        ) {
            if (isCircular) {
                GameIconFancy(icon = icon, contentDesc = "")
            } else {
                GameIconSimple(icon = icon, contentDesc = "")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameIconWithReflectionPreview() {
    val drawable = android.graphics.drawable.ColorDrawable(android.graphics.Color.DKGRAY)
    GameIconWithReflection(icon = drawable, contentDesc = "Preview")
}
