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
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import com.retrobreeze.ribbonlauncher.model.GameEntry
import com.retrobreeze.ribbonlauncher.ArrowDirection
import com.retrobreeze.ribbonlauncher.CarouselArrow
import com.retrobreeze.ribbonlauncher.AppEditMenu
import kotlinx.coroutines.launch

private const val MAX_GAME_TITLE_LENGTH = 30

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
    iconScale: Float,
    showLabels: Boolean = true,
    showEditButton: Boolean = true,
    settingsExpanded: Boolean = false,
    pinnedCount: Int = 0,
    onLaunch: (GameEntry) -> Unit,
    onEdit: () -> Unit,
    onPinToggle: (GameEntry) -> Unit,
    onCustomIcon: (GameEntry) -> Unit,
    onCustomWallpaper: (GameEntry) -> Unit,
    onTitleChange: (GameEntry, String) -> Unit,
    onReset: (GameEntry) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val baseItemSpacing = 12.dp
    val baseItemSize = 150.dp

    var previousScale by remember { mutableStateOf(iconScale) }
    var spacingTarget by remember { mutableStateOf(baseItemSpacing * iconScale) }

    val itemSize by animateDpAsState(
        targetValue = baseItemSize * iconScale,
        animationSpec = tween(durationMillis = 300),
        label = "itemSize"
    )
    val itemSpacing by animateDpAsState(
        targetValue = spacingTarget,
        animationSpec = tween(durationMillis = 300),
        label = "spacing"
    )

    // Extra padding around the pinned divider
    val dividerPadding by animateDpAsState(
        targetValue = itemSpacing * 2,
        animationSpec = tween(durationMillis = 300),
        label = "dividerPadding"
    )

    var isResizing by remember { mutableStateOf(false) }
    LaunchedEffect(iconScale) {
        if (iconScale != previousScale) {
            isResizing = true
            spacingTarget = baseItemSpacing * previousScale
            previousScale = iconScale
            kotlinx.coroutines.delay(300)
            spacingTarget = baseItemSpacing * iconScale
            kotlinx.coroutines.delay(300)
            isResizing = false
        }
    }
    val selectedScale = 1.25f
    val maxPageWidth = itemSize * selectedScale
    // Keep navigation arrows a consistent size regardless of icon scale
    val arrowHeight = 48.dp
    val arrowWidth = arrowHeight / 2
    // Keep arrows away from the very edge of the screen
    val arrowPadding = 16.dp

    val density = LocalDensity.current
    var currentText by remember {
        mutableStateOf(
            if (showEditButton && pagerState.currentPage == games.size) "Edit" else games.getOrNull(pagerState.currentPage)?.displayName.orEmpty()
        )
    }
    var labelBitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    var alpha by remember { mutableStateOf(if (showLabels) 1f else 0f) }
    var editingTitle by remember { mutableStateOf(false) }
    var localTitle by remember { mutableStateOf(TextFieldValue("")) }
    val focusRequester = remember { FocusRequester() }
    var hadFocus by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        label = "BitmapFade"
    )

    LaunchedEffect(pagerState.currentPage) {
        if (editingTitle) {
            editingTitle = false
        }
        val newText = if (showEditButton && pagerState.currentPage == games.size) {
            "Edit"
        } else {
            games.getOrNull(pagerState.currentPage)?.displayName.orEmpty()
        }
        if (newText != currentText) {
            alpha = 0f
            kotlinx.coroutines.delay(150)
            currentText = newText
            labelBitmap = renderTextToBitmap(currentText, 48.dp, density)
            if (showLabels) alpha = 1f
        } else if (labelBitmap == null) {
            labelBitmap = renderTextToBitmap(text = currentText, heightDp = 48.dp, density = density)
        }
    }

    LaunchedEffect(showLabels) {
        if (showLabels) {
            if (labelBitmap == null) {
                labelBitmap = renderTextToBitmap(currentText, 48.dp, density)
            }
            alpha = 1f
        } else {
            alpha = 0f
        }
    }


    val screenWidthDp = LocalConfiguration.current.screenWidthDp.dp
    val targetPadding = ((screenWidthDp - maxPageWidth) / 2).coerceAtLeast(0.dp)
    val horizontalPadding by animateDpAsState(targetValue = targetPadding, label = "padding")

    val animatables = remember { mutableMapOf<String, Animatable<Float, AnimationVector1D>>() }
    var previousIndices by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    val totalPages = games.size + if (showEditButton) 1 else 0

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
                key = { index -> if (index < games.size) games[index].packageName else "edit_button" }
            ) { page ->
                val isEditPage = showEditButton && page == games.size
                val isSelected = pagerState.currentPage == page
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) selectedScale else 1f,
                    animationSpec = tween(durationMillis = 300),
                    label = "ScaleAnimation"
                )
                val size = itemSize

                val offset = if (!isEditPage) animatables[games[page].packageName]?.value ?: 0f else 0f
                val pinnedOffset = if (pinnedCount > 0 && page >= pinnedCount) dividerPadding else 0.dp

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { translationX = offset + with(density) { pinnedOffset.toPx() } },
                    contentAlignment = Alignment.Center
                ) {
                    if (isEditPage) {
                        val editModifier = Modifier
                            .height(size + (size * 0.25f))
                            .width(size)
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                            }
                            .clip(RoundedCornerShape(size * 0.08f))
                            .background(Color.Gray.copy(alpha = 0.3f))
                            .clickable {
                                if (isSelected) {
                                    onEdit()
                                } else {
                                    coroutineScope.launch { pagerState.animateScrollToPage(page) }
                                }
                            }

                        Box(
                            modifier = editModifier,
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                        }
                    } else {
                    val game = games[page]
                    val gameModifier = Modifier
                        .height(size + (size * 0.25f))
                        .width(size)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .clickable {
                            if (isSelected) {
                                onLaunch(game)
                            } else {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(page)
                                }
                            }
                        }

                        Box(
                            modifier = gameModifier,
                            contentAlignment = Alignment.Center
                        ) {
                            ReflectiveGameIcon(
                                icon = game.icon,
                                contentDesc = game.displayName,
                                iconSize = size,
                                showReflection = !isResizing
                            )
                            // menu moved below label when settings are open
                        }
                    }
                    if (pinnedCount > 0 && page == pinnedCount - 1) {
                        val lineHeight = size * scale * 0.75f
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .offset(x = itemSpacing / 2 + dividerPadding / 2)
                                .height(lineHeight)
                                .width(1.dp)
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }
        val canScrollLeft = pagerState.currentPage > 0
        val canScrollRight = pagerState.currentPage < totalPages - 1

        CarouselArrow(
            direction = ArrowDirection.LEFT,
            enabled = canScrollLeft,
            onClick = {
                val target = (pagerState.currentPage - 4).coerceAtLeast(0)
                coroutineScope.launch { pagerState.animateScrollToPage(target) }
            },
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = arrowPadding),
            width = arrowWidth,
            height = arrowHeight
        )

        CarouselArrow(
            direction = ArrowDirection.RIGHT,
            enabled = canScrollRight,
            onClick = {
                val target = (pagerState.currentPage + 4).coerceAtMost(totalPages - 1)
                coroutineScope.launch { pagerState.animateScrollToPage(target) }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = arrowPadding),
            width = arrowWidth,
            height = arrowHeight
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (editingTitle) {
                BasicTextField(
                    value = localTitle,
                    onValueChange = { value ->
                        var text = value.text.replace("\n", "")
                        if (text.length > MAX_GAME_TITLE_LENGTH) text = text.take(MAX_GAME_TITLE_LENGTH)
                        localTitle = value.copy(text = text)
                    },
                    textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        focusManager.clearFocus()
                        editingTitle = false
                        onTitleChange(games[pagerState.currentPage], localTitle.text)
                    }),
                    modifier = Modifier
                        .height(48.dp)
                        .focusRequester(focusRequester)
                        .onFocusChanged { state ->
                            if (state.isFocused) {
                                hadFocus = true
                                localTitle = localTitle.copy(selection = TextRange(0, localTitle.text.length))
                            } else if (hadFocus) {
                                hadFocus = false
                                editingTitle = false
                                onTitleChange(games[pagerState.currentPage], localTitle.text)
                            }
                        }
                )
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                    keyboardController?.show()
                }
            } else {
                labelBitmap?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(64.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            bitmap = it,
                            contentDescription = currentText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .alpha(animatedAlpha)
                        )
                    }
                }
            }
            val showEditMenu = settingsExpanded && showEditButton && pagerState.currentPage < games.size
            Spacer(Modifier.height(if (showEditMenu) 8.dp else 0.dp))
            AppEditMenu(
                visible = showEditMenu,
                iconSize = 32.dp,
                onPinToggle = { onPinToggle(games[pagerState.currentPage]) },
                onCustomTitle = {
                    val title = games[pagerState.currentPage].displayName
                    localTitle = TextFieldValue(title)
                    editingTitle = true
                },
                onCustomIcon = { onCustomIcon(games[pagerState.currentPage]) },
                onCustomWallpaper = { onCustomWallpaper(games[pagerState.currentPage]) },
                onReset = { onReset(games[pagerState.currentPage]) }
            )
        }
    }
}

@Composable
fun ReflectiveGameIcon(
    icon: Drawable,
    contentDesc: String,
    iconSize: Dp,
    showReflection: Boolean = true
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
                .clip(RoundedCornerShape(iconSize * 0.08f)),
            contentScale = ContentScale.Crop
        )

        if (showReflection) {
            Box(
                modifier = Modifier
                    .height(iconSize * 0.25f)
                    .width(iconSize)
                    .clip(RoundedCornerShape(iconSize * 0.08f))
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
}

