package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ReflectiveGameIcon(
    contentDesc: String,
    iconSize: Dp
) {
    val painter = ColorPainter(Color.Red)

    Column(
        modifier = Modifier
            .width(iconSize)
            .height(iconSize + (iconSize * 0.25f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painter,
            contentDescription = contentDesc,
            modifier = Modifier.size(iconSize),
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .graphicsLayer {
                    scaleY = -1f
                },
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter
        )


    }
}
