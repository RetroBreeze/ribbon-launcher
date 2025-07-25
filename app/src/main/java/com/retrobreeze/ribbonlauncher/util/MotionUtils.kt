package com.retrobreeze.ribbonlauncher.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.PI

@Composable
fun rememberParallaxOffset(maxOffsetDp: Float = 16f): State<Offset> {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sensorManager = remember { context.getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    val offsetState = remember { mutableStateOf(Offset.Zero) }

    val maxOffsetPx = with(density) { maxOffsetDp.dp.toPx() }

    DisposableEffect(sensorManager) {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensor != null) {
            val listener = object : SensorEventListener {
                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                override fun onSensorChanged(event: SensorEvent) {
                    val rotationMatrix = FloatArray(9)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val orientations = FloatArray(3)
                    SensorManager.getOrientation(rotationMatrix, orientations)
                    val pitch = orientations[1]
                    val roll = orientations[2]

                    val x = (-roll / (PI / 2)).toFloat().coerceIn(-1f, 1f) * maxOffsetPx
                    val y = (-pitch / (PI / 2)).toFloat().coerceIn(-1f, 1f) * maxOffsetPx
                    offsetState.value = Offset(x, y)
                }
            }
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
            onDispose { sensorManager.unregisterListener(listener) }
        } else {
            onDispose { }
        }
    }
    return offsetState
}
