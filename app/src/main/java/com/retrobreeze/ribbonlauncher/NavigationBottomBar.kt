package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.indication
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
    onLeftClick: () -> Unit = {},
    onCenterClick: () -> Unit = {},
    onRightClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme()
    val gradient = remember(isDark) {
        val tint = if (isDark) Color.Black else Color.White
        Brush.verticalGradient(
            colors = listOf(tint.copy(alpha = if (isDark) 0.12f else 0.18f), Color.Transparent)
        )
    }

    val colorScheme = MaterialTheme.colorScheme
    val buttonGradient = remember(isDark, colorScheme) {
        val start = if (isDark) colorScheme.primaryContainer else colorScheme.primary
        val end = if (isDark) colorScheme.primary else colorScheme.primaryContainer
        Brush.verticalGradient(listOf(start, end))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(gradient)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onLeftClick,
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .background(buttonGradient, RoundedCornerShape(0.dp)),
                shape = RoundedCornerShape(0.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            ) {
                Text("Left")
            }
            Spacer(modifier = Modifier.width(4.dp))
            Button(
                onClick = onCenterClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(buttonGradient, RoundedCornerShape(0.dp)),
                shape = RoundedCornerShape(0.dp),
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            ) {
                Text("Center")
            }
            Spacer(modifier = Modifier.width(4.dp))
            val interactionSource = remember { MutableInteractionSource() }
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .fillMaxHeight()
                    .background(buttonGradient, RoundedCornerShape(0.dp))
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
