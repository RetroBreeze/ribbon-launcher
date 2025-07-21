package com.retrobreeze.ribbonlauncher.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap

@Composable
fun GameIconSimple(icon: Drawable, contentDesc: String, modifier: Modifier = Modifier) {
    val painter = BitmapPainter(icon.toBitmap().asImageBitmap())

    Image(
        painter = painter,
        contentDescription = contentDesc,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}
