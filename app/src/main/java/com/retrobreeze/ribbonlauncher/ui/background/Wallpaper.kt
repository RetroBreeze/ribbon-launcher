package com.retrobreeze.ribbonlauncher.ui.background

import android.net.Uri
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest

@Composable
fun Wallpaper(
    theme: WallpaperTheme,
    imageUri: Uri?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val request = imageUri?.let {
        ImageRequest.Builder(context).data(it).crossfade(true).build()
    }
    Crossfade(targetState = request, label = "wallpaperCrossfade") { target ->
        if (target != null) {
            SubcomposeAsyncImage(
                model = target,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = { AnimatedBackground(theme = theme, modifier = modifier) },
                modifier = modifier
            )
        } else {
            AnimatedBackground(theme = theme, modifier = modifier)
        }
    }
}
