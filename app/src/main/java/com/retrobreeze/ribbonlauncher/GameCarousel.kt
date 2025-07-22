package com.retrobreeze.ribbonlauncher

import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.Density
import com.retrobreeze.ribbonlauncher.model.GameEntry
import com.retrobreeze.ribbonlauncher.ArrowDirection
import com.retrobreeze.ribbonlauncher.CarouselArrow
import kotlinx.coroutines.launch

fun renderTextToBitmap(
    text: String,
    heightDp: Dp,
    density: Density
): ImageBitmap {
    val widthPx = 1000
    val heightPx = with(density) { heightDp.roundToPx() }

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG).apply {
        color = AndroidColor.WHITE
        textAlign = AndroidPaint.Align.CENTER
        textSize = heightPx * 0.5f
        typeface = Typeface.DEFAULT_BOLD
    }

    canvas.drawColor(AndroidColor.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)
    val x = widthPx / 2f
    val y = heightPx / 2f - (paint.descent() + paint.ascent()) / 2f
    canvas.drawText(text, x, y, paint)

    return bitmap.asImageBitmap()
}

@androidx.compose.foundation.ExperimentalFoundationApi
@Composable
fun GameCarousel(
    games: List<GameEntry>,
    pagerState: PagerState,
    selectedPackageName: String?,
    onLaunch: (GameEntry) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val itemSpacing = 12.dp
    val itemSize = 150.dp
    val selectedScale = 1.25f
    val maxPageWidth = itemSize * selectedScale
    val arrowHeight = itemSize * 0.5f
    val arrowWidth = arrowHeight / 2

    val density = LocalDensity.current
    var currentText by remember { mutableStateOf(games.getOrNull(pagerState.currentPage)?.displayName.orEmpty()) }
    var labelBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var alpha by remember { mutableStateOf(1f) }

    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        label = "BitmapFade"
    )

    LaunchedEffect(pagerState.currentPage) {
        val newText = games.getOrNull(pagerState.currentPage)?.displayName.orEmpty()
        if (newText != currentText) {
            alpha = 0f
            kotlinx.coroutines.delay(150)
            currentText = newText
            labelBitmap = renderTextToBitmap(currentText, 32.dp, density)
            alpha = 1f
        } else if (labelBitmap == null) {
            labelBitmap = renderTextToBitmap(text = currentText, heightDp = 32.dp, density = density)
        }
    }

    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val horizontalPadding = ((screenWidthDp - maxPageWidth) / 2).coerceAtLeast(0.dp)

    val animatables = remember { mutableMapOf<String, Animatable<Float, AnimationVector1D>>() }
    var previousIndices by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    LaunchedEffect(games) {
        val itemWidthPx = with(density) { (maxPageWidth + itemSpacing).toPx() }
        val oldSelectedIndex = previousIndices[selectedPackageName] ?: pagerState.currentPage
        val newSelectedIndex = games.indexOfFirst { it.packageName == selectedPackageName }.takeIf { it != -1 } ?: pagerState.currentPage
        val indexOffset = newSelectedIndex - oldSelectedIndex

        games.forEachIndexed { index, game ->
            val prev = previousIndices[game.packageName] ?: index
            val anim = animatables.getOrPut(game.packageName) { Animatable(0f) }
            val delta = (prev - index + indexOffset) * itemWidthPx
            val start = if (game.packageName == selectedPackageName) 0f else delta
            anim.snapTo(start)
            if (start != 0f) {
                coroutineScope.launch {
                    anim.animateTo(0f)
                }
            }
        }

        previousIndices = games.mapIndexed { i, g -> g.packageName to i }.toMap()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(maxPageWidth),
                pageSpacing = itemSpacing,
                contentPadding = PaddingValues(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    pagerSnapDistance = PagerSnapDistance.atMost(1)
                ),
                key = { index -> games[index].packageName }
            ) { page ->
                val game = games[page]
                val isSelected = pagerState.currentPage == page
                val size by animateDpAsState(
                    targetValue = if (isSelected) itemSize * selectedScale else itemSize,
                    label = "SizeAnimation"
                )
                val offset = animatables[game.packageName]?.value ?: 0f

                Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationX = offset },
                contentAlignment = Alignment.Center
                ) {
                Box(
                    modifier = Modifier
                        .height(size + (size * 0.25f))
                        .width(size)
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
                    ReflectiveGameIcon(
                        icon = game.icon,
                        contentDesc = game.displayName,
                        iconSize = size
                    )
                }
            }
            }
        }
        val canScrollLeft = pagerState.currentPage > 0
        val canScrollRight = pagerState.currentPage < games.lastIndex

        CarouselArrow(
            direction = ArrowDirection.LEFT,
            enabled = canScrollLeft,
            onClick = {
                val target = (pagerState.currentPage - 4).coerceAtLeast(0)
                coroutineScope.launch { pagerState.animateScrollToPage(target) }
            },
            modifier = Modifier.align(Alignment.CenterStart),
            width = arrowWidth,
            height = arrowHeight
        )

        CarouselArrow(
            direction = ArrowDirection.RIGHT,
            enabled = canScrollRight,
            onClick = {
                val target = (pagerState.currentPage + 4).coerceAtMost(games.lastIndex)
                coroutineScope.launch { pagerState.animateScrollToPage(target) }
            },
            modifier = Modifier.align(Alignment.CenterEnd),
            width = arrowWidth,
            height = arrowHeight
        )

        labelBitmap?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .fillMaxWidth()
                    .height(48.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = it,
                    contentDescription = currentText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                        .alpha(animatedAlpha)
                )
            }
        }
    }
}

@Composable
fun ReflectiveGameIcon(
    icon: Drawable,
    contentDesc: String,
    iconSize: Dp
) {
    val bitmap = icon.toBitmap(width = 256, height = 256)
    val painter = BitmapPainter(bitmap.asImageBitmap())

    Column(
        modifier = Modifier
            .width(iconSize)
            .height(iconSize + (iconSize * 0.25f)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painter,
            contentDescription = contentDesc,
            modifier = Modifier
                .size(iconSize)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .height(iconSize * 0.25f)
                .width(iconSize)
                .clip(RoundedCornerShape(12.dp))
                .drawWithCache {
                    val gradient = Brush.verticalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.5f), Color.Transparent),
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
                    .graphicsLayer { scaleY = -1f },
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomCenter
            )
        }
    }
}

