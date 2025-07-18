package com.retrobreeze.ribbonlauncher

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.BatteryUnknown
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.SignalWifiOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatusTopBar(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val isConnected by rememberNetworkConnection()
    val isBluetoothOn by rememberBluetoothState()
    val batteryLevel by rememberBatteryLevel()
    val currentTime by rememberCurrentTime()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f))
        Text(
            text = currentTime,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(verticalAlignment = Alignment.CenterVertically) {
            val networkIcon = if (isConnected) Icons.Filled.Wifi else Icons.Filled.SignalWifiOff
            Icon(imageVector = networkIcon, contentDescription = "Network")
            Spacer(modifier = Modifier.width(12.dp))
            val btIcon = if (isBluetoothOn) Icons.Filled.Bluetooth else Icons.Filled.BluetoothDisabled
            Icon(imageVector = btIcon, contentDescription = "Bluetooth")
            Spacer(modifier = Modifier.width(12.dp))
            val batteryIcon = if (batteryLevel > 10) Icons.Filled.BatteryFull else Icons.Filled.BatteryUnknown
            Icon(imageVector = batteryIcon, contentDescription = "Battery")
        }
    }
}

@Composable
private fun rememberNetworkConnection(): State<Boolean> {
    val context = LocalContext.current
    val connectivityManager = remember {
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }
    val isConnected = remember { mutableStateOf(false) }

    DisposableEffect(connectivityManager) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                isConnected.value = true
            }

            override fun onLost(network: Network) {
                val active = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                isConnected.value = active?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            }
        }
        val request = NetworkRequest.Builder().build()
        connectivityManager.registerNetworkCallback(request, callback)
        val active = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        isConnected.value = active?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
    return isConnected
}

@Composable
private fun rememberBluetoothState(): State<Boolean> {
    val context = LocalContext.current
    val bluetoothAdapter = remember { BluetoothAdapter.getDefaultAdapter() }
    val isEnabled = remember { mutableStateOf(bluetoothAdapter?.isEnabled == true) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    isEnabled.value = state == BluetoothAdapter.STATE_ON
                }
            }
        }
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    return isEnabled
}

@Composable
private fun rememberBatteryLevel(): State<Int> {
    val context = LocalContext.current
    val battery = remember { mutableStateOf(100) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                    val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        battery.value = (level * 100f / scale).toInt()
                    }
                }
            }
        }
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(receiver, filter)
        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
    return battery
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
