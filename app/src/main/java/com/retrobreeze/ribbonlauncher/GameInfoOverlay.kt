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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
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

    var titleText by remember { mutableStateOf(customization?.label ?: game.displayName) }
    var editingTitle by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val iconPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onIconChange(it.toString()) }
    }
    val wallpaperPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { onWallpaperChange(it.toString()) }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            var dragOffset by remember { mutableStateOf(0f) }
            val threshold = with(LocalContext.current.resources.displayMetrics) { 80 * density }
            AnimatedVisibility(
                visible = show,
                enter = slideInVertically(animationSpec = tween(), initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(animationSpec = tween(), targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(horizontal = 24.dp, vertical = 24.dp)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (editingTitle) {
                            androidx.compose.material3.TextField(
                                value = titleText,
                                onValueChange = { value ->
                                    var text = value
                                    if (text.length > MAX_LABEL_LENGTH) text = text.take(MAX_LABEL_LENGTH)
                                    text = text.replace("\n", "")
                                    titleText = text
                                    onLabelChange(text.takeIf { it != game.displayName })
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleLarge.copy(textAlign = TextAlign.Center),
                                trailingIcon = {
                                    IconButton(onClick = { editingTitle = false }) {
                                        Icon(Icons.Default.Check, contentDescription = "Done")
                                    }
                                }
                            )
                        } else {
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { editingTitle = true },
                                textAlign = TextAlign.Center
                            )
                            IconButton(onClick = { editingTitle = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = game.packageName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Icon",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Box(modifier = Modifier.size(120.dp)) {
                                Image(
                                    painter = rememberAsyncImagePainter(customization?.iconUri ?: game.icon),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Button(onClick = { iconPicker.launch("image/*") }) {
                                    Text("Change")
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { onIconChange(null) }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Wallpaper",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                            ) {
                                if (customization?.wallpaperUri != null) {
                                    Image(
                                        painter = rememberAsyncImagePainter(customization.wallpaperUri!!),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RectangleShape)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Photo,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(120.dp)
                                            .align(Alignment.Center)
                                    )
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Button(onClick = { wallpaperPicker.launch("image/*") }) {
                                    Text("Change")
                                }
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { onWallpaperChange(null) }) {
                                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    IconButton(
                        onClick = {
                            val uri = Uri.parse("https://play.google.com/store/apps/details?id=${game.packageName}")
                            context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_google_play),
                            contentDescription = "Google Play",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

}
