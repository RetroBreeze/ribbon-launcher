package com.retrobreeze.ribbonlauncher.util

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.PI

/**
 * Observe device motion and emit a small Offset suitable for parallax effects.
 * Falls back to the accelerometer + magnetometer when the rotation vector sensor
 * is unavailable so motion works on more devices and emulators.
 */
@Composable
fun rememberParallaxOffset(maxOffsetDp: Float = 16f): State<Offset> {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }
    val offsetState = remember { mutableStateOf(Offset.Zero) }
    val orientation = context.resources.configuration.orientation
    val maxOffsetPx = with(density) { maxOffsetDp.dp.toPx() }

    DisposableEffect(sensorManager, orientation) {
        var accelValues: FloatArray? = null
        var magnetValues: FloatArray? = null

        fun computeOffset(matrix: FloatArray) {
            val remapped = FloatArray(9)
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                SensorManager.remapCoordinateSystem(
                    matrix,
                    SensorManager.AXIS_Y,
                    SensorManager.AXIS_MINUS_X,
                    remapped
                )
            } else {
                remapped.indices.forEach { remapped[it] = matrix[it] }
            }
            val orientations = FloatArray(3)
            SensorManager.getOrientation(remapped, orientations)
            val pitch = orientations[1]
            val roll = orientations[2]
            val x = (-roll / (PI / 2)).toFloat().coerceIn(-1f, 1f) * maxOffsetPx
            val y = (-pitch / (PI / 2)).toFloat().coerceIn(-1f, 1f) * maxOffsetPx
            offsetState.value = Offset(x, y)
        }

        val listener = object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_GAME_ROTATION_VECTOR,
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        val matrix = FloatArray(9)
                        SensorManager.getRotationMatrixFromVector(matrix, event.values)
                        computeOffset(matrix)
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        accelValues = event.values.clone()
                        if (magnetValues != null) {
                            val matrix = FloatArray(9)
                            if (SensorManager.getRotationMatrix(matrix, null, accelValues, magnetValues)) {
                                computeOffset(matrix)
                            }
                        }
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        magnetValues = event.values.clone()
                    }
                }
            }
        }

        val sensors = buildList {
            sensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
                ?.let { add(it) }
                ?: sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)?.let { add(it) }
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let { add(it) }
            sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let { add(it) }
        }

        sensors.forEach { sensor ->
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        }

        onDispose { sensorManager.unregisterListener(listener) }
    }

    return offsetState
}
