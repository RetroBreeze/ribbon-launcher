package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.retrobreeze.ribbonlauncher.ui.background.WallpaperTheme

@Composable
fun WallpaperThemeDialog(
    show: Boolean,
    current: WallpaperTheme,
    onSelect: (WallpaperTheme) -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 400.dp)
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(WallpaperTheme.values()) { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(theme); onDismiss() }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    brush = Brush.verticalGradient(
                                        listOf(theme.startColor, theme.endColor)
                                    )
                                )
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = theme.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (theme == current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
