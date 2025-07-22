package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.foundation.layout.size

enum class ArrowDirection { LEFT, RIGHT }

@Composable
fun CarouselArrow(
    direction: ArrowDirection,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = 24.dp,
    height: Dp
) {
    val arrowModifier = modifier
        .clickable(enabled = enabled, onClick = onClick)

    Canvas(
        modifier = arrowModifier
            .size(width = width, height = height)
    ) {
        val path = Path()
        if (direction == ArrowDirection.LEFT) {
            path.moveTo(size.width, 0f)
            path.lineTo(0f, size.height / 2f)
            path.lineTo(size.width, size.height)
        } else {
            path.moveTo(0f, 0f)
            path.lineTo(size.width, size.height / 2f)
            path.lineTo(0f, size.height)
        }
        path.close()
        drawPath(
            path = path,
            color = Color.DarkGray.copy(alpha = if (enabled) 0.5f else 0.15f),
            style = Fill
        )
    }
}
