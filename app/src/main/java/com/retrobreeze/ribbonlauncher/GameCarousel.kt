package com.retrobreeze.ribbonlauncher

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.retrobreeze.ribbonlauncher.model.GameEntry
import com.retrobreeze.ribbonlauncher.ui.components.GameIconFancy
import com.retrobreeze.ribbonlauncher.ui.components.GameIconSimple
import com.retrobreeze.ribbonlauncher.util.isIconLikelyCircular

@Composable
fun GameCarousel(
    games: List<GameEntry>,
    onLaunch: (GameEntry) -> Unit
) {
    val pagerState = rememberPagerState(initialPage = 0) { games.size }
    val itemSpacing = 32.dp
    val itemSize = 150.dp
    val selectedScale = 1.25f
    val maxPageWidth = itemSize * selectedScale

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
    ) {
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
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(size)
                        .clickable(enabled = isSelected) {
                            onLaunch(game)
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
}
