package com.retrobreeze.ribbonlauncher

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun SettingsMenu(
    sortMode: SortMode,
    onSortClick: () -> Unit,
    onIconSizeClick: () -> Unit,
    onSelectedIconSizeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showSortLabel by remember { mutableStateOf(false) }

    LaunchedEffect(sortMode) {
        showSortLabel = true
        delay(1000)
        showSortLabel = false
    }

    val divider: @Composable () -> Unit = {
        Box(
            Modifier
                .height(24.dp)
                .width(1.dp)
                .background(Color.White.copy(alpha = 0.3f))
        )
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            modifier = Modifier.clickable { expanded = !expanded }
        )
        AnimatedVisibility(
            visible = expanded,
            enter = expandHorizontally() + fadeIn(),
            exit = shrinkHorizontally() + fadeOut()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .widthIn(min = 24.dp)
                        .clickable { onSortClick() },
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedContent(
                        targetState = showSortLabel,
                        transitionSpec = { fadeIn() with fadeOut() },
                        label = "sortDisplay"
                    ) { show ->
                        if (show) {
                            androidx.compose.material3.Text(
                                text = sortMode.label,
                                color = Color.White
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ZoomIn,
                    contentDescription = "Icon Size",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onIconSizeClick() }
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.CropFree,
                    contentDescription = "Selected Icon Size",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onSelectedIconSizeClick() }
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.TextFields,
                    contentDescription = "Show Labels",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.Photo,
                    contentDescription = "Wallpaper",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                divider()
                Spacer(Modifier.width(8.dp))
                val locked = false
                Icon(
                    imageVector = if (locked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = "Lock",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                divider()
                Spacer(Modifier.width(8.dp))
                val erased = false
                Icon(
                    imageVector = if (erased) Icons.Default.Delete else Icons.Default.Restore,
                    contentDescription = "Reset",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingsMenuPreview() {
    SettingsMenu(
        sortMode = SortMode.AZ,
        onSortClick = {},
        onIconSizeClick = {},
        onSelectedIconSizeClick = {}
    )
}
