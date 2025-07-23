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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.retrobreeze.ribbonlauncher.model.GameEntry

@Composable
fun EditAppsDialog(
    show: Boolean,
    allApps: List<GameEntry>,
    games: List<GameEntry>,
    selectedPackages: Set<String>,
    onConfirm: (Set<String>) -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    var localSelection by remember { mutableStateOf(selectedPackages.toMutableSet()) }
    LaunchedEffect(show) {
        if (show) {
            localSelection = selectedPackages.toMutableSet()
        }
    }

    val gamePackages = remember(games) { games.map { it.packageName } }
    val allGamesSelected = gamePackages.all { localSelection.contains(it) }

    fun toggleSelectGames(checked: Boolean) {
        localSelection = if (checked) {
            (localSelection + gamePackages).toMutableSet()
        } else {
            localSelection.toMutableSet().apply { removeAll(gamePackages) }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = localSelection.isEmpty(),
                            onCheckedChange = { checked -> if (checked) localSelection.clear() }
                        )
                        Text("none")
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = localSelection.size == allApps.size,
                            onCheckedChange = { checked ->
                                localSelection = if (checked) {
                                    allApps.map { it.packageName }.toMutableSet()
                                } else {
                                    mutableSetOf()
                                }
                            }
                        )
                        Text("All")
                    }
                    Spacer(Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = allGamesSelected,
                            onCheckedChange = { toggleSelectGames(it) }
                        )
                        Text("Games")
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { onConfirm(localSelection.toSet()); onDismiss() }) {
                        Text("OK")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                LazyColumn(modifier = Modifier
                    .heightIn(max = 400.dp)
                    .fillMaxWidth()
                ) {
                    items(allApps) { app ->
                        val checked = app.packageName in localSelection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = checked,
                                onCheckedChange = { isChecked ->
                                    localSelection = localSelection.toMutableSet().apply {
                                        if (isChecked) add(app.packageName) else remove(app.packageName)
                                    }
                                }
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

            }
        }
    }
}
