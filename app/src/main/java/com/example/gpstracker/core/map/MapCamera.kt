package com.example.gpstracker.core.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.gpstracker.core.location.UserLocation
import com.example.gpstracker.core.places.LatLngPoint

const val DEFAULT_LOCATION_ZOOM = 17f
const val TILTED_3D_DEGREES = 45f

data class MapPoint(
    val latitude: Double,
    val longitude: Double,
)

data class MapPin(
    val id: String,
    val latitude: Double,
    val longitude: Double,
    val label: String? = null,
)

data class MapBoundsRequest(
    val points: List<MapPoint>,
    val paddingPx: Int,
)

fun UserLocation.toMapPoint(): MapPoint = MapPoint(latitude, longitude)

fun LatLngPoint.toMapPoint(): MapPoint = MapPoint(latitude, longitude)

/**
 * Camera commands for [AppMap]. Gesture updates write the fields without bumping [revision],
 * so the map is not asked to animate to its own position.
 */
@Stable
class MapCameraState(
    latitude: Double,
    longitude: Double,
    zoom: Float,
) {
    var latitude by mutableStateOf(latitude)
    var longitude by mutableStateOf(longitude)
    var zoom by mutableFloatStateOf(zoom)
    var tilt by mutableFloatStateOf(0f)
    var bearing by mutableFloatStateOf(0f)
    var revision by mutableIntStateOf(0)
        private set
    var bounds: MapBoundsRequest? = null
        private set

    fun moveTo(
        latitude: Double,
        longitude: Double,
        zoom: Float = this.zoom,
        tilt: Float = this.tilt,
        bearing: Float = this.bearing,
    ) {
        bounds = null
        this.latitude = latitude
        this.longitude = longitude
        this.zoom = zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
        this.tilt = tilt
        this.bearing = bearing
        revision++
    }

    fun zoomBy(amount: Float) {
        moveTo(latitude, longitude, zoom + amount)
    }

    fun orient(tilt: Float, bearing: Float = this.bearing) {
        moveTo(latitude, longitude, zoom, tilt, bearing)
    }

    fun moveToBounds(points: List<MapPoint>, paddingPx: Int) {
        if (points.isEmpty()) return
        bounds = MapBoundsRequest(points, paddingPx)
        revision++
    }

    fun syncFromMap(latitude: Double, longitude: Double, zoom: Float, tilt: Float, bearing: Float) {
        this.latitude = latitude
        this.longitude = longitude
        this.zoom = zoom
        this.tilt = tilt
        this.bearing = bearing
    }

    private companion object {
        const val MIN_ZOOM = 2f
        const val MAX_ZOOM = 20f
    }
}

@Composable
fun rememberMapCameraState(
    latitude: Double = 0.0,
    longitude: Double = 0.0,
    zoom: Float = DEFAULT_LOCATION_ZOOM,
): MapCameraState = remember(latitude, longitude, zoom) {
    MapCameraState(latitude, longitude, zoom)
}

/** Jumps the camera to the first fix once per screen instance; later fixes don't move it. */
@Composable
fun CenterOnFirstFix(camera: MapCameraState, target: MapPoint?) {
    var centered by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(target) {
        if (target != null && !centered) {
            camera.moveTo(target.latitude, target.longitude, DEFAULT_LOCATION_ZOOM)
            centered = true
        }
    }
}
