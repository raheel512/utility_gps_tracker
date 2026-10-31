package com.example.gpstracker.core.map

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.location.Location
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.gpstracker.R
import com.example.gpstracker.core.common.rememberContentReady
import com.example.gpstracker.core.designsystem.theme.ColorTokens
import com.example.gpstracker.core.permissions.isPermissionGranted
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapLibreMapOptions
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillExtrusionLayer
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import timber.log.Timber
import android.Manifest
import kotlinx.coroutines.delay

enum class AppMapType { Normal, Satellite, Hybrid, Terrain }

private const val PIN_IMAGE = "app-pin"
private const val PIN_SOURCE = "app-pins"
private const val PIN_LAYER = "app-pins-layer"
private const val ROUTE_SOURCE = "app-route"
private const val ROUTE_LAYER = "app-route-layer"
private const val CAMERA_MS = 600

/**
 * Street map drawn with MapLibre and the OpenFreeMap Liberty style.
 * Satellite, hybrid, and terrain keep this street style and show [R.string.map_imagery_unavailable].
 * The MapLibre logo and OpenStreetMap attribution stay visible.
 */
@Composable
fun AppMap(
    mapType: AppMapType,
    camera: MapCameraState,
    showUserLocation: Boolean,
    modifier: Modifier = Modifier,
    compassEnabled: Boolean = true,
    buildingsEnabled: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(),
    pins: List<MapPin> = emptyList(),
    route: List<MapPoint> = emptyList(),
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    onPinClick: (String) -> Unit = {},
) {
    if (LocalInspectionMode.current) {
        Box(modifier.background(MaterialTheme.colorScheme.surfaceVariant))
        return
    }
    // The placeholder is what slides in. MapView is created only after that
    // animation, so opening the screen does not wait on the map.
    if (rememberContentReady()) {
        AppMapNow(
            mapType = mapType,
            camera = camera,
            showUserLocation = showUserLocation,
            modifier = modifier,
            compassEnabled = compassEnabled,
            buildingsEnabled = buildingsEnabled,
            contentPadding = contentPadding,
            pins = pins,
            route = route,
            userLatitude = userLatitude,
            userLongitude = userLongitude,
            onPinClick = onPinClick,
        )
    } else {
        MapLoading(modifier)
    }
}

@Composable
private fun AppMapNow(
    mapType: AppMapType,
    camera: MapCameraState,
    showUserLocation: Boolean,
    modifier: Modifier = Modifier,
    compassEnabled: Boolean = true,
    buildingsEnabled: Boolean = false,
    contentPadding: PaddingValues = PaddingValues(),
    pins: List<MapPin> = emptyList(),
    route: List<MapPoint> = emptyList(),
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    onPinClick: (String) -> Unit = {},
) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val topPx = with(density) {
        contentPadding.calculateTopPadding().roundToPx()
    }
    val bottomPx = with(density) {
        contentPadding.calculateBottomPadding().roundToPx()
    }
    val endPx = with(density) {
        contentPadding.calculateRightPadding(layoutDirection).roundToPx()
    }
    val pinClick by rememberUpdatedState(onPinClick)
    val mapView = remember {
        MapLibre.getInstance(context.applicationContext)
        // Texture mode follows Compose slides. The default surface punches
        // through the animation and looks like a flicker.
        val options = MapLibreMapOptions.createFromAttributes(context, null)
            .textureMode(true)
        MapView(context, options)
    }
    var mapRef by remember { mutableStateOf<MapLibreMap?>(null) }
    var styleReady by remember { mutableStateOf(false) }
    var showStyleLoading by remember { mutableStateOf(true) }
    LaunchedEffect(styleReady) {
        if (styleReady) showStyleLoading = false
    }
    LaunchedEffect(Unit) {
        delay(8_000)
        showStyleLoading = false
    }

    Box(modifier) {
        AndroidView(
            factory = {
                mapView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                }
            },
            modifier = Modifier.matchParentSize(),
        )
        if (showStyleLoading && !styleReady) {
            val loadingDescription = stringResource(R.string.cd_content_loading)
            Box(
                Modifier
                    .matchParentSize()
                    .background(ColorTokens.ScreenTintStart)
                    .semantics { contentDescription = loadingDescription },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = ColorTokens.Primary500)
            }
        }
        if (mapType != AppMapType.Normal) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp, start = 12.dp, end = 12.dp)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                shape = MaterialTheme.shapes.small,
            ) {
                Text(
                    text = stringResource(R.string.map_imagery_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                )
            }
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                else -> Unit
            }
        }
        mapView.onCreate(null)
        val state = lifecycleOwner.lifecycle.currentState
        if (state.isAtLeast(Lifecycle.State.STARTED)) mapView.onStart()
        if (state.isAtLeast(Lifecycle.State.RESUMED)) mapView.onResume()
        lifecycleOwner.lifecycle.addObserver(observer)
        mapView.getMapAsync { map ->
            mapRef = map
            map.uiSettings.isLogoEnabled = true
            map.uiSettings.isAttributionEnabled = true
            map.setStyle(Style.Builder().fromUri(MapServicesConfig.STYLE_URL)) { style ->
                installOverlays(style)
                styleReady = true
            }
            map.addOnCameraMoveListener {
                val position = map.cameraPosition
                val target = position.target ?: return@addOnCameraMoveListener
                camera.syncFromMap(
                    latitude = target.latitude,
                    longitude = target.longitude,
                    zoom = position.zoom.toFloat(),
                    tilt = position.tilt.toFloat(),
                    bearing = position.bearing.toFloat(),
                )
            }
            map.addOnMapClickListener { latLng ->
                val screen = map.projection.toScreenLocation(latLng)
                val hit = map.queryRenderedFeatures(screen, PIN_LAYER)
                    .firstOrNull()
                    ?.getStringProperty("id")
                if (hit.isNullOrBlank()) {
                    false
                } else {
                    pinClick(hit)
                    true
                }
            }
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            styleReady = false
            mapRef = null
            mapView.onPause()
            mapView.onStop()
            mapView.onDestroy()
        }
    }

    LaunchedEffect(styleReady, mapRef, compassEnabled, topPx, bottomPx, endPx) {
        val map = mapRef ?: return@LaunchedEffect
        map.uiSettings.isCompassEnabled = compassEnabled
        if (!styleReady) return@LaunchedEffect
        // Padding first: setPadding reapplies stored ornament margins.
        map.setPadding(0, topPx, 0, bottomPx)
        val gap = with(density) { 4.dp.roundToPx() }
        val besideLogo = with(density) { 92.dp.roundToPx() }
        val bottom = bottomPx + gap
        val end = endPx + gap
        map.uiSettings.setLogoMargins(gap, gap, end, bottom)
        map.uiSettings.setAttributionMargins(besideLogo, gap, end, bottom)
    }

    LaunchedEffect(styleReady, camera.revision) {
        if (!styleReady) return@LaunchedEffect
        val map = mapRef ?: return@LaunchedEffect
        val pendingBounds = camera.bounds
        if (pendingBounds != null && pendingBounds.points.size >= 2) {
            val builder = LatLngBounds.Builder()
            pendingBounds.points.forEach { builder.include(LatLng(it.latitude, it.longitude)) }
            runCatching {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(builder.build(), pendingBounds.paddingPx),
                    CAMERA_MS,
                )
            }.onFailure { Timber.w(it, "Map bounds update skipped") }
        } else {
            val position = CameraPosition.Builder()
                .target(LatLng(camera.latitude, camera.longitude))
                .zoom(camera.zoom.toDouble())
                .tilt(camera.tilt.toDouble())
                .bearing(camera.bearing.toDouble())
                .build()
            map.animateCamera(CameraUpdateFactory.newCameraPosition(position), CAMERA_MS)
        }
    }

    LaunchedEffect(styleReady, pins, route) {
        if (!styleReady) return@LaunchedEffect
        val style = mapRef?.style ?: return@LaunchedEffect
        (style.getSourceAs<GeoJsonSource>(PIN_SOURCE))?.setGeoJson(pinCollection(pins))
        (style.getSourceAs<GeoJsonSource>(ROUTE_SOURCE))?.setGeoJson(routeCollection(route))
    }

    LaunchedEffect(styleReady, buildingsEnabled) {
        if (!styleReady) return@LaunchedEffect
        val style = mapRef?.style ?: return@LaunchedEffect
        val visibility = if (buildingsEnabled) Property.VISIBLE else Property.NONE
        style.layers.filterIsInstance<FillExtrusionLayer>().forEach { layer ->
            layer.setProperties(PropertyFactory.visibility(visibility))
        }
    }

    val hasLocationPermission = context.isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION) ||
        context.isPermissionGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
    LaunchedEffect(styleReady, showUserLocation, hasLocationPermission, userLatitude, userLongitude) {
        if (!styleReady) return@LaunchedEffect
        val map = mapRef ?: return@LaunchedEffect
        val style = map.style ?: return@LaunchedEffect
        val component = map.locationComponent
        val enabled = showUserLocation && hasLocationPermission && userLatitude != null && userLongitude != null
        if (!component.isLocationComponentActivated) {
            runCatching {
                component.activateLocationComponent(
                    LocationComponentActivationOptions.builder(context, style)
                        .useDefaultLocationEngine(false)
                        .build(),
                )
            }.onFailure { Timber.w(it, "Location layer unavailable") }
        }
        if (!component.isLocationComponentActivated) return@LaunchedEffect
        component.isLocationComponentEnabled = enabled
        if (enabled) {
            component.forceLocationUpdate(
                Location("app").apply {
                    latitude = userLatitude
                    longitude = userLongitude
                    time = System.currentTimeMillis()
                },
            )
        }
    }
}

@Composable
private fun MapLoading(modifier: Modifier) {
    val loadingDescription = stringResource(R.string.cd_content_loading)
    Box(
        modifier
            .background(ColorTokens.ScreenTintStart)
            .semantics { contentDescription = loadingDescription },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = ColorTokens.Primary500)
    }
}

private fun installOverlays(style: Style) {
    style.addImage(PIN_IMAGE, pinBitmap())
    style.addSource(GeoJsonSource(PIN_SOURCE, FeatureCollection.fromFeatures(emptyList())))
    style.addSource(GeoJsonSource(ROUTE_SOURCE, FeatureCollection.fromFeatures(emptyList())))
    style.addLayer(
        LineLayer(ROUTE_LAYER, ROUTE_SOURCE).withProperties(
            PropertyFactory.lineColor(ColorTokens.Primary500.toArgb()),
            PropertyFactory.lineWidth(5f),
            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
        ),
    )
    style.addLayer(
        SymbolLayer(PIN_LAYER, PIN_SOURCE).withProperties(
            PropertyFactory.iconImage(PIN_IMAGE),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconIgnorePlacement(true),
            PropertyFactory.textField(Expression.get("label")),
            PropertyFactory.textSize(12f),
            PropertyFactory.textOffset(arrayOf(0f, -1.8f)),
            PropertyFactory.textAllowOverlap(true),
            PropertyFactory.textColor(Color.BLACK),
            PropertyFactory.textHaloColor(Color.WHITE),
            PropertyFactory.textHaloWidth(1.4f),
        ),
    )
}

private fun pinCollection(pins: List<MapPin>): FeatureCollection {
    val features = pins.map { pin ->
        Feature.fromGeometry(Point.fromLngLat(pin.longitude, pin.latitude)).apply {
            addStringProperty("id", pin.id)
            addStringProperty("label", pin.label.orEmpty())
        }
    }
    return FeatureCollection.fromFeatures(features)
}

private fun routeCollection(route: List<MapPoint>): FeatureCollection {
    if (route.size < 2) return FeatureCollection.fromFeatures(emptyList())
    val line = LineString.fromLngLats(route.map { Point.fromLngLat(it.longitude, it.latitude) })
    return FeatureCollection.fromFeature(Feature.fromGeometry(line))
}

private fun pinBitmap(): Bitmap {
    val size = 72
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = ColorTokens.Primary500.toArgb() }
    canvas.drawCircle(size / 2f, size / 2f, size / 2.4f, fill)
    fill.color = Color.WHITE
    canvas.drawCircle(size / 2f, size / 2f, size / 6f, fill)
    return bitmap
}
