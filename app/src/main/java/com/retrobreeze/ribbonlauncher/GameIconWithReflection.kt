package com.retrobreeze.ribbonlauncher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.retrobreeze.ribbonlauncher.util.isIconLikelyCircular

/**
 * Displays a game icon with a subtle reflection beneath it.
 */
@Composable
fun GameIconWithReflection(
    icon: Drawable,
    contentDesc: String,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    reflectionRatio: Float = 0.4f
) {
    val isCircular = isIconLikelyCircular(icon)
    val reflectionHeight = iconSize * reflectionRatio

    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .size(iconSize)
        ) {
            if (isCircular) {
                GameIconFancy(icon = icon, contentDesc = contentDesc)
            } else {
                GameIconSimple(icon = icon, contentDesc = contentDesc)
            }
        }

        val offsetPx = with(LocalDensity.current) { iconSize.roundToPx() }

        Box(
            modifier = Modifier
                .offset { IntOffset(0, offsetPx) }
                .height(reflectionHeight)
                .width(iconSize)
                .graphicsLayer { clip = true },
            contentAlignment = Alignment.TopCenter
        ) {
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .graphicsLayer {
                        scaleY = -1f
                        transformOrigin = TransformOrigin(0.5f, 0f)
                    }
            ) {
                if (isCircular) {
                    GameIconFancy(icon = icon, contentDesc = "")
                } else {
                    GameIconSimple(icon = icon, contentDesc = "")
                }
            }
        }
    }
}


@Preview
@Composable
fun GameIconWithReflectionPreview() {
    val drawable = android.graphics.drawable.ColorDrawable(android.graphics.Color.DKGRAY)
    GameIconWithReflection(
        icon = drawable,
        contentDesc = "Preview",
        iconSize = 100.dp
    )
}

