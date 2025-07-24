package com.retrobreeze.ribbonlauncher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.retrobreeze.ribbonlauncher.util.contrastingColor

@Composable
fun SettingsOverlay(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = show,
        enter = slideInHorizontally(animationSpec = tween(), initialOffsetX = { -it }),
        exit = slideOutHorizontally(animationSpec = tween(), targetOffsetX = { -it })
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(end = 32.dp, top = 32.dp, bottom = 32.dp)
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                )
        ) {
            val surfaceColor = MaterialTheme.colorScheme.surface
            val contentColor = surfaceColor.contrastingColor()
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(480.dp)
                    .align(Alignment.CenterStart),
                color = surfaceColor,
                contentColor = contentColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Additional settings content can go here
                }
            }
        }
    }
}
