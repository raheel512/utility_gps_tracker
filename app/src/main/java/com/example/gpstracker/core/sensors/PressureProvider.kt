package com.example.gpstracker.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

interface PressureProvider {
    /** Emits hPa while collected, or a single null when the device has no barometer. */
    fun pressureHpa(): Flow<Float?>
}

@Singleton
class SensorPressureProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : PressureProvider {

    override fun pressureHpa(): Flow<Float?> {
        val sensorManager = context.getSystemService<SensorManager>()
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PRESSURE)
        if (sensorManager == null || sensor == null) {
            return flowOf(null)
        }
        return callbackFlow {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    trySend(event.values[0])
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
            awaitClose { sensorManager.unregisterListener(listener) }
        }
    }
}
