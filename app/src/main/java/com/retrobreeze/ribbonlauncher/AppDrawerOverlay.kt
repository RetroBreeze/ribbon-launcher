package com.retrobreeze.ribbonlauncher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.retrobreeze.ribbonlauncher.model.GameEntry

@Composable
fun AppDrawerOverlay(
    apps: List<GameEntry>,
    showDrawer: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AnimatedVisibility(
        visible = showDrawer,
        enter = slideInHorizontally(animationSpec = tween(), initialOffsetX = { it }),
        exit = slideOutHorizontally(animationSpec = tween(), targetOffsetX = { it })
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(start = 32.dp, top = 32.dp, bottom = 32.dp)
                .clickable(
                    onClick = onDismiss,
                    indication = null,
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                )
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(480.dp)
                    .align(Alignment.CenterEnd)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(apps) { app ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    context.packageManager.getLaunchIntentForPackage(app.packageName)?.let {
                                        context.startActivity(it)
                                    }
                                    onDismiss()
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(app.icon),
                                contentDescription = app.displayName,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = app.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
