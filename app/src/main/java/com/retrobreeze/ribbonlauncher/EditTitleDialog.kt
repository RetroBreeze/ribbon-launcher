package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.retrobreeze.ribbonlauncher.util.contrastingColor

private const val MAX_GAME_TITLE_LENGTH = 30

@Composable
fun EditTitleDialog(
    show: Boolean,
    current: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    if (!show) return

    var localTitle by remember { mutableStateOf(current) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val surface = MaterialTheme.colorScheme.surface
        val content = surface.contrastingColor()
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .widthIn(max = 400.dp),
            color = surface,
            contentColor = content,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Edit Title", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = localTitle,
                    onValueChange = {
                        var text = it.replace("\n", "")
                        if (text.length > MAX_GAME_TITLE_LENGTH) text = text.take(MAX_GAME_TITLE_LENGTH)
                        localTitle = text
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onConfirm(localTitle) }),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { onConfirm(localTitle) }) { Text("OK") }
                }
            }
        }
    }
}
