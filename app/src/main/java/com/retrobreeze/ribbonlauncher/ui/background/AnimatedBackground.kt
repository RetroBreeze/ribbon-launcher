package com.retrobreeze.ribbonlauncher.ui.background

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import com.retrobreeze.ribbonlauncher.ui.background.WallpaperTheme
import kotlin.math.PI
import kotlin.math.sin
import com.retrobreeze.ribbonlauncher.util.rememberParallaxOffset

@Composable
fun AnimatedBackground(
    theme: WallpaperTheme,
    modifier: Modifier = Modifier
) {
    val parallaxOffset = rememberParallaxOffset()
    val offsetConverter = remember {
        TwoWayConverter<Offset, AnimationVector2D>(
            convertToVector = { AnimationVector2D(it.x, it.y) },
            convertFromVector = { Offset(it.v1, it.v2) }
        )
    }
    val animatedOffset = remember { Animatable<Offset, AnimationVector2D>(Offset.Zero, offsetConverter) }

    LaunchedEffect(parallaxOffset.value) {
        animatedOffset.animateTo(
            parallaxOffset.value,
            animationSpec = tween(durationMillis = 300)
        )
    }
    val animatedStart by androidx.compose.animation.animateColorAsState(
        targetValue = theme.startColor,
        animationSpec = tween(durationMillis = 600),
        label = "startColor"
    )
    val animatedEnd by androidx.compose.animation.animateColorAsState(
        targetValue = theme.endColor,
        animationSpec = tween(durationMillis = 600),
        label = "endColor"
    )
    val gradientColors = listOf(animatedStart, animatedEnd)

    val infiniteTransition = rememberInfiniteTransition(label = "waves")
    val wave1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(30000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "wave1"
    )
    val wave2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(45000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "wave2"
    )

    androidx.compose.foundation.Canvas(
        modifier = modifier.graphicsLayer {
            translationX = animatedOffset.value.x
            translationY = animatedOffset.value.y
        }
    ) {
        drawRect(
            brush = Brush.radialGradient(
                colors = gradientColors,
                center = center,
                radius = size.maxDimension
            )
        )

        fun Path.addWave(progress: Float, amplitude: Float, vertical: Float) {
            val wavelength = size.width
            val step = size.width / 20f
            moveTo(-wavelength + progress * wavelength, vertical)
            var x = -wavelength
            while (x <= size.width + wavelength) {
                val y = amplitude * sin(2 * PI * (x / wavelength) + progress * 2 * PI).toFloat() + vertical
                lineTo(x + progress * wavelength, y)
                x += step
            }
            lineTo(size.width + wavelength, size.height)
            lineTo(-wavelength, size.height)
            close()
        }

        val wave1 = Path().apply { addWave(wave1Offset, size.height * 0.05f, size.height * 0.65f) }
        val wave2 = Path().apply { addWave(wave2Offset, size.height * 0.07f, size.height * 0.8f) }

        drawPath(wave1, color = Color.White.copy(alpha = 0.08f), style = Fill)
        drawPath(wave2, color = Color.White.copy(alpha = 0.04f), style = Fill)
    }
}
