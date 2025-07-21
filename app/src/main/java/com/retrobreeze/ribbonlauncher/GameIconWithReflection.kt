package com.retrobreeze.ribbonlauncher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun GameIconWithReflection(
    icon: Drawable,
    contentDesc: String,
    isCircular: Boolean,
    iconSize: Dp,
    modifier: Modifier = Modifier
) {
    val shape: Shape = if (isCircular) CircleShape else RoundedCornerShape(12.dp)
    val reflectionHeight = iconSize * 0.25f

    Column(
        modifier = modifier
            .size(width = iconSize, height = iconSize + reflectionHeight),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(iconSize)) {
            if (isCircular) {
                GameIconFancy(
                    icon = icon,
                    contentDesc = contentDesc,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                GameIconSimple(
                    icon = icon,
                    contentDesc = contentDesc,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(reflectionHeight)
                .clip(shape)
                .graphicsLayer { scaleY = -1f }
                .drawWithCache {
                    val gradient = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                    onDrawWithContent {
                        drawContent()
                        drawRect(gradient, blendMode = BlendMode.DstIn)
                    }
                }
        ) {
            val painter = remember(icon) { BitmapPainter(icon.toBitmap().asImageBitmap()) }
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = if (isCircular) ContentScale.Fit else ContentScale.Crop
            )
        }
    }
}
