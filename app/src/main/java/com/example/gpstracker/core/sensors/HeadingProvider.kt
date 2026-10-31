package com.example.gpstracker.core.sensors

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.core.content.getSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

enum class HeadingAccuracy { High, Medium, Low, Unreliable, Unknown }

data class DeviceHeading(
    /** True-north degrees in [0, 360). */
    val degreesTrue: Float,
    val cardinal: String,
    val accuracy: HeadingAccuracy,
)

interface HeadingProvider {
    /**
     * Emits heading while collected. Stops the rotation-vector sensor when there are no collectors.
     * [location] is used for magnetic declination (true north); null falls back to magnetic north.
     */
    fun headings(location: Location?): Flow<DeviceHeading>
}

@Singleton
class SensorHeadingProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : HeadingProvider {

    override fun headings(location: Location?): Flow<DeviceHeading> = callbackFlow {
        val sensorManager = context.getSystemService<SensorManager>()
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensorManager == null || sensor == null) {
            close()
            return@callbackFlow
        }

        val rotationMatrix = FloatArray(9)
        val orientation = FloatArray(3)
        var accuracy = HeadingAccuracy.Unknown

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                val magneticAzimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                val normalized = ((magneticAzimuth % 360f) + 360f) % 360f
                val declination = location?.let { loc ->
                    GeomagneticField(
                        loc.latitude.toFloat(),
                        loc.longitude.toFloat(),
                        loc.altitude.toFloat(),
                        System.currentTimeMillis(),
                    ).declination
                } ?: 0f
                val trueHeading = ((normalized + declination) % 360f + 360f) % 360f
                trySend(
                    DeviceHeading(
                        degreesTrue = trueHeading,
                        cardinal = cardinalLabel(trueHeading),
                        accuracy = accuracy,
                    ),
                )
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracyValue: Int) {
                accuracy = when (accuracyValue) {
                    SensorManager.SENSOR_STATUS_ACCURACY_HIGH -> HeadingAccuracy.High
                    SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM -> HeadingAccuracy.Medium
                    SensorManager.SENSOR_STATUS_ACCURACY_LOW -> HeadingAccuracy.Low
                    SensorManager.SENSOR_STATUS_UNRELIABLE -> HeadingAccuracy.Unreliable
                    else -> HeadingAccuracy.Unknown
                }
            }
        }

        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sensorManager.unregisterListener(listener) }
    }
}

/** 16-point compass rose, e.g. 342° → NNW. */
fun cardinalLabel(degreesTrue: Float): String {
    val points = arrayOf(
        "N", "NNE", "NE", "ENE", "E", "ESE", "SE", "SSE",
        "S", "SSW", "SW", "WSW", "W", "WNW", "NW", "NNW",
    )
    val index = ((degreesTrue / 22.5f).roundToInt()) % 16
    return points[index]
}
