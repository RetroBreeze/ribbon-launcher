package com.retrobreeze.ribbonlauncher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AppEditMenu(
    visible: Boolean,
    onPinToggle: () -> Unit,
    onCustomTitle: () -> Unit,
    onCustomIcon: () -> Unit,
    onCustomWallpaper: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(
            expandFrom = Alignment.Start,
            animationSpec = tween()
        ) + fadeIn(),
        exit = shrinkHorizontally(
            shrinkTowards = Alignment.Start,
            animationSpec = tween()
        ) + fadeOut(),
        modifier = modifier
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.PushPin,
                contentDescription = "Pin", 
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onPinToggle() }
            )
            Spacer(Modifier.width(8.dp))
            Divider(
                modifier = Modifier
                    .height(24.dp)
                    .width(1.dp)
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Custom Title",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onCustomTitle() }
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Custom Icon",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onCustomIcon() }
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Photo,
                contentDescription = "Custom Wallpaper",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onCustomWallpaper() }
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Replay,
                contentDescription = "Reset All",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onReset() }
            )
        }
    }
}

@Preview
@Composable
private fun AppEditMenuPreview() {
    AppEditMenu(
        visible = true,
        onPinToggle = {},
        onCustomTitle = {},
        onCustomIcon = {},
        onCustomWallpaper = {},
        onReset = {}
    )
}

