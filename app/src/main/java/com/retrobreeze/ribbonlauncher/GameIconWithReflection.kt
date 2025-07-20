package com.retrobreeze.ribbonlauncher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
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
    reflectionRatio: Float = 0.4f
) {
    val isCircular = isIconLikelyCircular(icon)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            if (isCircular) {
                GameIconFancy(icon = icon, contentDesc = contentDesc)
            } else {
                GameIconSimple(icon = icon, contentDesc = contentDesc)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(reflectionRatio)
                .graphicsLayer {
                    scaleY = -1f
                }
        ) {
            if (isCircular) {
                GameIconFancy(icon = icon, contentDesc = "")
            } else {
                GameIconSimple(icon = icon, contentDesc = "")
            }
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            0f to Color.Black.copy(alpha = 0.4f),
                            1f to Color.Transparent
                        )
                    )
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
    }
}


@Preview
@Composable
fun GameIconWithReflectionPreview() {
    val drawable = android.graphics.drawable.ColorDrawable(android.graphics.Color.DKGRAY)
    GameIconWithReflection(icon = drawable, contentDesc = "Preview")
}

