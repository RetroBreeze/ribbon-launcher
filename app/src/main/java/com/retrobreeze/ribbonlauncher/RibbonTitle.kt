package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.clickable
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.Alignment

private const val MAX_TITLE_LENGTH = 50

@Composable
fun RibbonTitle(
    title: String,
    onTitleChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf(false) }
    var localTitle by remember { mutableStateOf(TextFieldValue(title)) }
    val focusRequester = remember { FocusRequester() }
    var hadFocus by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(title) {
        if (!editing) {
            localTitle = TextFieldValue(title)
        }
    }

    LaunchedEffect(editing) {
        if (editing) {
            focusRequester.requestFocus()
            keyboardController?.show()
        }
    }

    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        if (editing) {
            BasicTextField(
                value = localTitle,
                onValueChange = { value ->
                    var text = value.text.replace("\n", "")
                    if (text.length > MAX_TITLE_LENGTH) text = text.take(MAX_TITLE_LENGTH)
                    localTitle = value.copy(text = text)
                },
                textStyle = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize * 1.5f,
                    color = Color.White
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    editing = false
                    onTitleChange(localTitle.text)
                }),
                modifier = Modifier
                    .heightIn(min = 32.dp)
                    .focusRequester(focusRequester)
                    .onFocusChanged { state ->
                        if (state.isFocused) {
                            hadFocus = true
                            localTitle = localTitle.copy(selection = TextRange(0, localTitle.text.length))
                        } else if (hadFocus) {
                            hadFocus = false
                            editing = false
                            onTitleChange(localTitle.text)
                        }
                    }
            )
            Spacer(Modifier.width(4.dp))
            IconButton(
                onClick = {
                    focusManager.clearFocus()
                    editing = false
                    onTitleChange(localTitle.text)
                },
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Transparent)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
            }
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize * 1.5f
                ),
                color = Color.White,
                modifier = Modifier.clickable {
                    localTitle = TextFieldValue(title, TextRange(0, title.length))
                    editing = true
                }
            )
            // Tapping the title itself toggles editing; no extra edit button
        }
    }
}

@Preview
@Composable
private fun RibbonTitlePreview() {
    RibbonTitle(title = "Games", onTitleChange = {})
}
