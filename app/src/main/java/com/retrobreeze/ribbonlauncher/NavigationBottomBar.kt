package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(gradient)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onLeftClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text("Left")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onCenterClick,
                modifier = Modifier
                    .weight(3f)
                    .fillMaxHeight()
            ) {
                Text("Center")
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onRightClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Text("Right")
            }
        }
    }
}

@Preview
@Composable
fun NavigationBottomBarPreview() {
    NavigationBottomBar()
}
