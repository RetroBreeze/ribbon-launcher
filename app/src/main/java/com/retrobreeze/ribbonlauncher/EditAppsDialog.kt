package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.retrobreeze.ribbonlauncher.model.GameEntry

@Composable
fun EditAppsDialog(
    show: Boolean,
    allApps: List<GameEntry>,
    selectedPackages: Set<String>,
    onPackageChecked: (String, Boolean) -> Unit,
    onSelectAll: () -> Unit,
    onSelectNone: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = MaterialTheme.shapes.medium) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onSelectAll) { Text("Select All") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = onSelectNone) { Text("Select None") }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                    items(allApps) { app ->
                        val checked = app.packageName in selectedPackages
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { onPackageChecked(app.packageName, it) }
                            )
                            Spacer(Modifier.width(8.dp))
                            Image(
                                painter = rememberAsyncImagePainter(app.icon),
                                contentDescription = app.displayName,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(text = app.displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Done") }
                }
            }
        }
    }
}

