package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ReflectiveVectorIcon(
    imageVector: ImageVector,
    contentDesc: String?,
    iconSize: Dp
) {
    Column(
        modifier = Modifier
            .width(iconSize)
            .height(iconSize + (iconSize * 0.25f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDesc,
            modifier = Modifier.size(iconSize)
        )

        Box(
            modifier = Modifier
                .height(iconSize * 0.25f)
                .width(iconSize)
                .clip(RoundedCornerShape(12.dp))
                .drawWithCache {
                    val gradient = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
                        startY = 0f,
                        endY = size.height
                    )
                    onDrawWithContent {
                        with(drawContext.canvas) {
                            saveLayer(bounds = size.toRect(), paint = Paint())
                            drawContent()
                            drawRect(gradient, blendMode = BlendMode.DstIn)
                            restore()
                        }
                    }
                }
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { scaleY = -1f },
                tint = LocalContentColor.current
            )
        }
    }
}
