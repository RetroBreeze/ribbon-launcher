package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun NavigationBottomBar(
    modifier: Modifier = Modifier,
    onRightClick: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .size(64.dp)
            .indication(interactionSource, ripple())
            .pointerInput(onRightClick) {
                detectTapGestures(onPress = {
                    onRightClick()
                    awaitRelease()
                })
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Apps,
            contentDescription = null
        )
    }
}

@Preview
@Composable
fun NavigationBottomBarPreview() {
    NavigationBottomBar()
}
