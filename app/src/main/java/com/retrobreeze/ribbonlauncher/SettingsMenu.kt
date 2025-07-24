package com.retrobreeze.ribbonlauncher

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SettingsMenu(
    sortMode: SortMode,
    onSortClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

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
                SortButton(
                    sortMode = sortMode,
                    onClick = {
                        onSortClick()
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun SettingsMenuPreview() {
    SettingsMenu(sortMode = SortMode.AZ, onSortClick = {})
}
