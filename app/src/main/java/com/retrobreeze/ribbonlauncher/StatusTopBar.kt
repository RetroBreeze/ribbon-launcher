package com.retrobreeze.ribbonlauncher

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WifiManager
import android.os.BatteryManager
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Battery0Bar
import androidx.compose.material.icons.filled.Battery1Bar
import androidx.compose.material.icons.filled.Battery2Bar
import androidx.compose.material.icons.filled.Battery3Bar
import androidx.compose.material.icons.filled.Battery4Bar
import androidx.compose.material.icons.filled.Battery5Bar
import androidx.compose.material.icons.filled.Battery6Bar
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryUnknown
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.SignalWifi0Bar
import androidx.compose.material.icons.filled.SignalWifi4Bar
import androidx.compose.material.icons.filled.Wifi1Bar
import androidx.compose.material.icons.filled.Wifi2Bar
import androidx.compose.material.icons.filled.NetworkWifi3Bar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatusTopBar(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val wifiLevel by rememberWifiSignalLevel()
    val bluetoothState by rememberBluetoothState()
    val batteryState by rememberBatteryStatus()
    val currentTime by rememberCurrentTime()

    val isDark = isSystemInDarkTheme()
    val gradient = remember(isDark) {
        val tint = if (isDark) Color.White else Color.Black
        Brush.verticalGradient(
            colors = listOf(Color.Transparent, tint.copy(alpha = if (isDark) 0.12f else 0.18f))
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(32.dp)
            .background(gradient)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = currentTime,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.align(Alignment.Center)
        )
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = wifiIcon(wifiLevel),
                contentDescription = "Wi-Fi",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = bluetoothIcon(bluetoothState),
                contentDescription = "Bluetooth",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = batteryIcon(batteryState.level, batteryState.charging),
                contentDescription = "Battery",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${batteryState.level}%",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
        }
    }
}


data class BluetoothState(val enabled: Boolean, val connected: Boolean)

@Composable
private fun rememberWifiSignalLevel(): State<Int> {
    val context = LocalContext.current
    val wifiManager = remember { context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager }
    val level = remember { mutableStateOf(if (wifiManager.isWifiEnabled) WifiManager.calculateSignalLevel(wifiManager.connectionInfo.rssi, 5) else -1) }

    DisposableEffect(wifiManager) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (!wifiManager.isWifiEnabled) {
                    level.value = -1
                } else {
                    level.value = WifiManager.calculateSignalLevel(wifiManager.connectionInfo.rssi, 5)
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(WifiManager.RSSI_CHANGED_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return level
}

@Composable
private fun rememberBluetoothState(): State<BluetoothState> {
    val context = LocalContext.current
    val manager = remember { context.getSystemService(BluetoothManager::class.java) }
    val state = remember {
        mutableStateOf(
            BluetoothState(
                enabled = manager.adapter?.isEnabled == true,
                connected = false
            )
        )
    }

    DisposableEffect(manager) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val s = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                        if (s != BluetoothAdapter.STATE_ON) {
                            state.value = BluetoothState(false, false)
                        } else {
                            val connected = manager.getConnectedDevices(BluetoothProfile.GATT).isNotEmpty() ||
                                manager.getConnectedDevices(BluetoothProfile.HEADSET).isNotEmpty() ||
                                manager.getConnectedDevices(BluetoothProfile.A2DP).isNotEmpty()
                            state.value = BluetoothState(true, connected)
                        }
                    }
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        state.value = state.value.copy(connected = true)
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val connected = manager.getConnectedDevices(BluetoothProfile.GATT).isNotEmpty() ||
                            manager.getConnectedDevices(BluetoothProfile.HEADSET).isNotEmpty() ||
                            manager.getConnectedDevices(BluetoothProfile.A2DP).isNotEmpty()
                        state.value = state.value.copy(connected = connected)
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    return state
}

data class BatteryState(val level: Int, val charging: Boolean)

@Composable
private fun rememberBatteryStatus(): State<BatteryState> {
    val context = LocalContext.current
    val state = remember { mutableStateOf(BatteryState(100, false)) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val pct = if (level >= 0 && scale > 0) (level * 100f / scale).toInt() else state.value.level
                    val charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
                    state.value = BatteryState(pct, charging)
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose { context.unregisterReceiver(receiver) }
    }
    return state
}

private fun wifiIcon(level: Int) = when {
    level < 0 -> Icons.Filled.SignalWifiOff
    level == 0 -> Icons.Filled.SignalWifi0Bar
    level == 1 -> Icons.Filled.Wifi1Bar
    level == 2 -> Icons.Filled.Wifi2Bar
    level == 3 -> Icons.Filled.NetworkWifi3Bar
    else -> Icons.Filled.SignalWifi4Bar
}

private fun bluetoothIcon(state: BluetoothState) = when {
    !state.enabled -> Icons.Filled.BluetoothDisabled
    state.connected -> Icons.Filled.BluetoothConnected
    else -> Icons.Filled.BluetoothSearching
}

private fun batteryIcon(level: Int, charging: Boolean) = when {
    charging -> Icons.Filled.BatteryChargingFull
    level >= 85 -> Icons.Filled.Battery6Bar
    level >= 70 -> Icons.Filled.Battery5Bar
    level >= 55 -> Icons.Filled.Battery4Bar
    level >= 40 -> Icons.Filled.Battery3Bar
    level >= 25 -> Icons.Filled.Battery2Bar
    level >= 10 -> Icons.Filled.Battery1Bar
    level >= 0 -> Icons.Filled.Battery0Bar
    else -> Icons.Filled.BatteryUnknown
}

@Composable
private fun rememberCurrentTime(): State<String> {
    val timeState = remember { mutableStateOf(currentTimeString()) }

    LaunchedEffect(Unit) {
        while (true) {
            timeState.value = currentTimeString()
            delay(60000)
        }
    }
    return timeState
}

private fun currentTimeString(): String {
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
    return formatter.format(Date())
}

@Preview
@Composable
private fun StatusTopBarPreview() {
    StatusTopBar()
}
