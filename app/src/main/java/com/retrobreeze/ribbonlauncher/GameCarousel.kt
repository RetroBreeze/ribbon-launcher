package com.retrobreeze.ribbonlauncher

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import com.retrobreeze.ribbonlauncher.model.GameEntry
import com.retrobreeze.ribbonlauncher.ui.components.GameIconFancy
import com.retrobreeze.ribbonlauncher.ui.components.GameIconSimple
import com.retrobreeze.ribbonlauncher.util.isIconLikelyCircular
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun GameCarousel(
    games: List<GameEntry>,
    onLaunch: (GameEntry) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0) { games.size }
    val coroutineScope = rememberCoroutineScope()
    val itemSpacing = 32.dp
    val itemSize = 150.dp
    val selectedScale = 1.25f
    val maxPageWidth = itemSize * selectedScale

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010))
            .padding(horizontal = 32.dp)
    ) {
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            BoxWithConstraints {
                val horizontalPadding = (maxWidth - maxPageWidth) / 2

                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fixed(maxPageWidth),
                    pageSpacing = itemSpacing,
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    flingBehavior = PagerDefaults.flingBehavior(
                        state = pagerState,
                        pagerSnapDistance = PagerSnapDistance.atMost(1)
                    )
                ) { page ->
                    val game = games[page]
                    val isSelected = pagerState.currentPage == page
                    val size by animateDpAsState(
                        targetValue = if (isSelected) itemSize * selectedScale else itemSize,
                        label = "SizeAnimation"
                    )

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(size)
                                .clickable {
                                    if (isSelected) {
                                        onLaunch(game)
                                    } else {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(page)
                                        }
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            game.icon?.let { icon ->
                                if (isIconLikelyCircular(icon)) {
                                    GameIconFancy(icon = icon, contentDesc = game.displayName)
                                } else {
                                    GameIconSimple(icon = icon, contentDesc = game.displayName)
                                }
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .height(48.dp),
            contentAlignment = Alignment.Center
        ) {
            FadingStaticLabel(
                text = games.getOrNull(pagerState.currentPage)?.displayName.orEmpty(),
                height = 32.dp
            )
        }
    }
}

@Composable
fun FadingStaticLabel(
    text: String,
    height: Dp = 32.dp,
    fadeDuration: Int = 150
) {
    var currentText by remember { mutableStateOf(text) }
    var bitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var alpha by remember { mutableStateOf(1f) }

    val density = LocalDensity.current
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(durationMillis = fadeDuration),
        label = "BitmapFade"
    )

    LaunchedEffect(text) {
        if (text != currentText) {
            alpha = 0f
            delay(fadeDuration.toLong())
            currentText = text
            bitmap = renderTextToBitmap(currentText, height, density)
            alpha = 1f
        } else if (bitmap == null) {
            bitmap = renderTextToBitmap(text, height, density)
        }
    }

    bitmap?.let {
        Image(
            bitmap = it,
            contentDescription = currentText,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .alpha(animatedAlpha)

        )
    }
}

fun renderTextToBitmap(
    text: String,
    heightDp: Dp,
    density: Density
): androidx.compose.ui.graphics.ImageBitmap {
    val widthPx = 1000
    val heightPx = with(density) { heightDp.roundToPx() }

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        textSize = heightPx * 0.5f
        typeface = Typeface.DEFAULT_BOLD
    }

    canvas.drawColor(android.graphics.Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
    val x = widthPx / 2f
    val y = heightPx / 2f - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(text, x, y, paint)

    return bitmap.asImageBitmap()
}

@Preview
@Composable
fun GameCarouselDebugPreview() {
    val sampleGames = listOf(
        GameEntry("com.example.one", "Play", ColorDrawable(Color.Gray.toArgb())),
        GameEntry("com.example.two", "Gyro", ColorDrawable(Color.Gray.toArgb())),
        GameEntry("com.example.three", "Yeti", ColorDrawable(Color.Gray.toArgb()))
    )
    GameCarousel(games = sampleGames, onLaunch = {})
}
