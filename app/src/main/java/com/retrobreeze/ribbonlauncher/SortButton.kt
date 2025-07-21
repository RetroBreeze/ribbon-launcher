package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun SortButton(
    sortMode: SortMode,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent),
        ) {
            Icon(imageVector = Icons.Default.Sort, contentDescription = "Sort")
        }
        Text(
            text = sortMode.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Preview
@Composable
private fun SortButtonPreview() {
    SortButton(sortMode = SortMode.AZ, onClick = {})
}
