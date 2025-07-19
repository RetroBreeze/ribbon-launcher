package com.retrobreeze.ribbonlauncher

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.style.TextAlign
import android.graphics.drawable.ColorDrawable
import com.retrobreeze.ribbonlauncher.model.GameEntry
import com.retrobreeze.ribbonlauncher.ui.components.GameIconFancy
import com.retrobreeze.ribbonlauncher.ui.components.GameIconSimple
import com.retrobreeze.ribbonlauncher.util.isIconLikelyCircular
import kotlinx.coroutines.launch


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
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        BoxWithConstraints {
            val horizontalPadding = (maxWidth - maxPageWidth) / 2

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
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
                    )
                ) { page ->
            val game = games[page]
            val isSelected = pagerState.currentPage == page
            val size by animateDpAsState(
                targetValue = if (isSelected) itemSize * selectedScale else itemSize,
                label = "SizeAnimation"
            )

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

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Crossfade(
                        targetState = pagerState.currentPage,
                        modifier = Modifier.fillMaxWidth(),
                        label = "GameTitle"
                    ) { pageIndex ->
                        Text(
                            text = games[pageIndex].displayName,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun GameCarouselPreview() {
    val sampleGames = List(3) { index ->
        GameEntry(
            packageName = "com.example.$index",
            displayName = "Game $index",
            icon = ColorDrawable(Color.Gray.toArgb())
        )
    }
    GameCarousel(games = sampleGames, onLaunch = {})
}
