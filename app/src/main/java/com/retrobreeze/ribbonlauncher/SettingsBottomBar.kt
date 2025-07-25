package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SettingsBottomBar(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(64.dp)
            .indication(interactionSource, ripple())
            .pointerInput(onClick) {
                detectTapGestures(onPress = {
                    onClick()
                    awaitRelease()
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings"
        )
    }
}

@Preview
@Composable
fun SettingsBottomBarPreview() {
    SettingsBottomBar()
}
