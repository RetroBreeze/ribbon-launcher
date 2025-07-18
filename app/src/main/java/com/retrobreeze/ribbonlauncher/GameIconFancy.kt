package com.retrobreeze.ribbonlauncher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.graphics.drawable.toBitmap

@Composable
fun GameIconFancy(icon: Drawable, contentDesc: String) {
    // Convert Drawable to Painter
    val painter: Painter = BitmapPainter(icon.toBitmap().asImageBitmap())

    Box(
        modifier = Modifier
            .size(150.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Blurred background
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = 1.5f
                    scaleY = 1.5f
                }
                .blur(20.dp),
            contentScale = ContentScale.Crop,
            alpha = 0.85f
        )

        // Foreground icon
        Image(
            painter = painter,
            contentDescription = contentDesc,
            modifier = Modifier
                .size(140.dp)
                .clip(RoundedCornerShape(20.dp)),
            contentScale = ContentScale.Fit
        )
    }
}
