package com.retrobreeze.ribbonlauncher

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.shape.RoundedCornerShape
import com.retrobreeze.ribbonlauncher.model.GameEntry

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MinimalGameCarousel(games: List<GameEntry>) {
    // val background = Color(0xFF101010) // removed for clean background
    val pagerState = rememberPagerState(initialPage = 0) { games.size }



    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        HorizontalPager(
            state = pagerState,
            pageSpacing = 24.dp
        ) { page ->
            val game = games[page]
            ReflectiveGameIconWithGradient(contentDesc = game.displayName, iconSize = 120.dp, iconColor = Color.Red)
        }
    }
}

@Composable
fun ReflectiveGameIconWithGradient(
    contentDesc: String,
    iconSize: Dp,
    iconColor: Color
) {
    val painter = ColorPainter(iconColor)

    Column(
        modifier = Modifier
            .width(iconSize)
            .height(iconSize + (iconSize * 0.25f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painter,
            contentDescription = contentDesc,
            modifier = Modifier.size(iconSize),
            contentScale = ContentScale.Crop
        )

        // Reflection using mask layer
        Box(
            modifier = Modifier
                .height(iconSize * 0.25f)
                .width(iconSize)
                .clip(RoundedCornerShape(12.dp))
                .drawWithCache {
                    val gradient = Brush.verticalGradient(
                        colors = listOf(
                            Color.White,
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = size.height
                    )
                    onDrawWithContent {
                        with(drawContext.canvas) {
                            saveLayer(bounds = size.toRect(), paint = Paint())
                            drawContent()
                            drawRect(gradient, blendMode = BlendMode.DstIn)
                            restore()
                        }
                    }
                }
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { scaleY = -1f }, // move scaleY here
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Preview
@Composable
fun MinimalGameCarouselPreview() {
    val sampleGames = listOf(
        GameEntry("com.example.a", "Alpha", ColorDrawable(Color.Red.toArgb())),
        GameEntry("com.example.b", "Bravo", ColorDrawable(Color.Green.toArgb())),
        GameEntry("com.example.c", "Charlie", ColorDrawable(Color.Blue.toArgb()))
    )
    MinimalGameCarousel(games = sampleGames)
}
