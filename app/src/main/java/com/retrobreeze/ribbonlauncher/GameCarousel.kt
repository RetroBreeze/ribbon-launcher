package com.retrobreeze.ribbonlauncher

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
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
    val itemSpacing = 16.dp
    val itemSize = 150.dp

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
            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.25f else 1f,
                label = "ScaleAnimation"
            )

            Box(
                modifier = Modifier
                    .size(itemSize) // Reserve consistent layout space
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                    },
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
