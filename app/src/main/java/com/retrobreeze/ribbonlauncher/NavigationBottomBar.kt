package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun NavigationBottomBar(
    modifier: Modifier = Modifier,
    onRightClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val gradient = remember(isDark) {
        val tint = if (isDark) Color.Black else Color.White
        Brush.verticalGradient(
            colors = listOf(tint.copy(alpha = if (isDark) 0.12f else 0.18f), Color.Transparent)
        )
    }


    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(gradient)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
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
    }
}

@Preview
@Composable
fun NavigationBottomBarPreview() {
    NavigationBottomBar()
}
