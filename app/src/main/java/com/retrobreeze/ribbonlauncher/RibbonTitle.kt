package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp

@Composable
fun RibbonTitle(
    title: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf(false) }
    var localTitle by remember { mutableStateOf(TextFieldValue(title)) }

    LaunchedEffect(title) {
        if (!editing) {
            localTitle = TextFieldValue(title)
        }
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (editing) {
            BasicTextField(
                value = localTitle,
                onValueChange = { localTitle = it },
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = MaterialTheme.colorScheme.onBackground),
                singleLine = true,
                modifier = Modifier
                    .heightIn(min = 32.dp)
                    .onFocusChanged { if (!it.isFocused) { editing = false; onTitleChange(localTitle.text) } }
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = { editing = false; onTitleChange(localTitle.text) }) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Done")
            }
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = { editing = true }) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
            }
        }
    }
}
