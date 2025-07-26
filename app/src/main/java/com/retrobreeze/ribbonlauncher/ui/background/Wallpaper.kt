package com.retrobreeze.ribbonlauncher.ui.background

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun Wallpaper(
    theme: WallpaperTheme,
    imageUri: Uri?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val key = imageUri?.toString() ?: theme.name
    Crossfade(targetState = key, label = "wallpaperCrossfade") { state ->
        if (imageUri != null && state == imageUri.toString()) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUri)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
            )
        } else {
            AnimatedBackground(theme = theme, modifier = modifier)
        }
    }
}
