package com.retrobreeze.ribbonlauncher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.retrobreeze.ribbonlauncher.ui.theme.RibbonLauncherTheme
import com.retrobreeze.ribbonlauncher.util.contrastingColor

@Composable
fun SettingsPage(
    show: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!show) return

    val surfaceColor = MaterialTheme.colorScheme.surface
    val contentColor = surfaceColor.contrastingColor()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = surfaceColor,
        contentColor = contentColor
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            Spacer(Modifier.width(88.dp))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(16.dp))
                SettingsOption("Upgrade to premium")
                SettingsOption("Theme: light, dark, OLED")
                SettingsToggleOption("Graphic mode: fancy / performance")
                SettingsToggleOption("Reverse A/B buttons")
                SettingsOption("Set as home")
                SettingsOption("Export settings")
                SettingsOption("Import settings")
                SettingsOption("Reset all settings")
                Spacer(modifier = Modifier.weight(1f))
                AboutSection()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingsOption(label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SettingsToggleOption(label: String) {
    var checked by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .clickable { checked = !checked },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = { checked = it })
    }
}

@Composable
private fun AboutSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "About",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text("Ribbon Launcher v1.0", style = MaterialTheme.typography.bodyMedium)
        Text("\u00A9 2025 RetroBreeze", style = MaterialTheme.typography.bodySmall)
    }
}

@Preview
@Composable
fun SettingsPagePreview() {
    RibbonLauncherTheme {
        SettingsPage(show = true, onDismiss = {})
    }
}
