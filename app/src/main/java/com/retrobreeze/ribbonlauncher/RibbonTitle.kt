package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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

    if (editing) {
        BasicTextField(
            value = localTitle,
            onValueChange = { value ->
                if ("\n" in value.text) {
                    val trimmed = value.text.replace("\n", "")
                    localTitle = value.copy(text = trimmed)
                    focusManager.clearFocus()
                    editing = false
                    onTitleChange(trimmed)
                } else {
                    localTitle = value
                }
            },
            textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {
                focusManager.clearFocus()
                editing = false
                onTitleChange(localTitle.text)
            }),
            modifier = modifier
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
    } else {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
            modifier = modifier.clickable {
                localTitle = TextFieldValue(title, TextRange(0, title.length))
                editing = true
            }
        )
    }
}
