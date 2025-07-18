package com.retrobreeze.ribbonlauncher

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.retrobreeze.ribbonlauncher.model.GameEntry
import com.retrobreeze.ribbonlauncher.ui.components.GameIconFancy
import com.retrobreeze.ribbonlauncher.ui.components.GameIconSimple
import com.retrobreeze.ribbonlauncher.util.isIconLikelyCircular
import kotlin.math.abs

@Composable
fun GameCarousel(
    games: List<GameEntry>,
    onLaunch: (GameEntry) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val itemSpacing = 32.dp
    val itemSize = 150.dp
    val selectedScale = 1.25f

    // Detect center item
    val centerItemIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val viewportCenter = (layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset) / 2
            layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs((item.offset + item.size / 2) - viewportCenter)
            }?.index ?: 0
        }
    }

    LaunchedEffect(listState.isScrollInProgress, centerItemIndex) {
        if (!listState.isScrollInProgress) {
            val layoutInfo = listState.layoutInfo
            val itemInfo = layoutInfo.visibleItemsInfo
                .firstOrNull { it.index == centerItemIndex }
                ?: return@LaunchedEffect
            val viewportCenter = (layoutInfo.viewportEndOffset + layoutInfo.viewportStartOffset) / 2
            val itemCenter = itemInfo.offset + itemInfo.size / 2
            val diff = itemCenter - viewportCenter
            if (diff != 0) {
                listState.animateScrollBy(diff.toFloat())
            }
        }
    }

    LazyRow(
        state = listState,
        horizontalArrangement = Arrangement.spacedBy(itemSpacing),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        contentPadding = PaddingValues(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        itemsIndexed(games) { index, game ->
            val isSelected = index == centerItemIndex
            val size by animateDpAsState(
                targetValue = if (isSelected) itemSize * selectedScale else itemSize,
                label = "SizeAnimation"
            )

            Box(
                modifier = Modifier.size(size),
                contentAlignment = Alignment.Center
            ) {
                game.icon?.let { icon ->
                    if (isIconLikelyCircular(icon)) {
                        GameIconFancy(
                            icon = icon,
                            contentDesc = game.displayName
                        )
                    } else {
                        GameIconSimple(
                            icon = icon,
                            contentDesc = game.displayName
                        )
                    }
                }
            }
        }
    }
}
