package com.retrobreeze.ribbonlauncher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun PinIndicator(
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(animationSpec = tween(), initialOffsetX = { -it }),
        exit = slideOutHorizontally(animationSpec = tween(), targetOffsetX = { -it }),
        modifier = modifier
    ) {
        Icon(imageVector = Icons.Default.PushPin, contentDescription = "Pin")
    }
}

@Preview
@Composable
private fun PinIndicatorPreview() {
    PinIndicator(visible = true)
}
