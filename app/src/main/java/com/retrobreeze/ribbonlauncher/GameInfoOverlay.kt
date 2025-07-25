package com.retrobreeze.ribbonlauncher

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.retrobreeze.ribbonlauncher.model.AppCustomization
import com.retrobreeze.ribbonlauncher.model.GameEntry
import kotlin.math.roundToInt

private const val MAX_LABEL_LENGTH = 30

@Composable
fun GameInfoOverlay(
    show: Boolean,
    game: GameEntry?,
    customization: AppCustomization?,
    onDismiss: () -> Unit,
    onLabelChange: (String?) -> Unit,
    onIconChange: (String?) -> Unit,
    onWallpaperChange: (String?) -> Unit
) {
    if (game == null) return

    var labelState by remember { mutableStateOf(TextFieldValue(customization?.label ?: game.displayName)) }
    val context = LocalContext.current

    val iconPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onIconChange(it.toString()) }
    }
    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onWallpaperChange(it.toString()) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var dragOffset by remember { mutableStateOf(0f) }
        val threshold = with(LocalContext.current.resources.displayMetrics) { 80 * density }
        AnimatedVisibility(
            visible = show,
            enter = slideInVertically(animationSpec = tween(), initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(animationSpec = tween(), targetOffsetY = { it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .offset { IntOffset(0, dragOffset.roundToInt()) }
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onVerticalDrag = { _, delta -> dragOffset = (dragOffset + delta).coerceAtLeast(0f) },
                            onDragEnd = {
                                if (dragOffset > threshold) onDismiss()
                                dragOffset = 0f
                            }
                        )
                    },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(text = "${game.displayName}", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.TextField(
                        value = labelState,
                        onValueChange = { value ->
                            var text = value.text.replace("\n", "")
                            if (text.length > MAX_LABEL_LENGTH) text = text.take(MAX_LABEL_LENGTH)
                            labelState = value.copy(text = text)
                            onLabelChange(labelState.text.takeIf { it != game.displayName })
                        },
                        label = { Text("Custom Name") }
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { iconPicker.launch("image/*") }) { Text("Change Icon") }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = { onIconChange(null) }) { Text("Revert") }
                    }
                    customization?.iconUri?.let { uri ->
                        Spacer(Modifier.height(8.dp))
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(onClick = { wallpaperPicker.launch("image/*") }) { Text("Change Wallpaper") }
                        Spacer(Modifier.width(8.dp))
                        OutlinedButton(onClick = { onWallpaperChange(null) }) { Text("Revert") }
                    }
                    customization?.wallpaperUri?.let { uri ->
                        Spacer(Modifier.height(8.dp))
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RectangleShape)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Package: ${game.packageName}")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {
                        val uri = Uri.parse("https://play.google.com/store/apps/details?id=${game.packageName}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }) { Text("Play Store") }
                }
            }
        }
    }
}
