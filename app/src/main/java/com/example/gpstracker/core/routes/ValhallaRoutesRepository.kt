package com.example.gpstracker.core.routes

import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.data.connectivity.ConnectivityRepository
import com.example.gpstracker.core.geo.GeoOkHttp
import com.example.gpstracker.core.geo.HttpStatusException
import com.example.gpstracker.core.geo.ValhallaMapper
import com.example.gpstracker.core.geo.ValhallaResponse
import com.example.gpstracker.core.geo.execute
import com.example.gpstracker.core.geo.retrying
import com.example.gpstracker.core.map.MapServicesConfig
import com.example.gpstracker.core.places.LatLngPoint
import com.example.gpstracker.core.places.PlacesUsageTracker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/**
 * Driving routes from Valhalla. The public host is for development, not production traffic.
 * Shapes are stored with the existing precision-5 polyline so history keeps decoding.
 */
@Singleton
class ValhallaRoutesRepository @Inject constructor(
    @GeoOkHttp private val client: OkHttpClient,
    private val json: Json,
    private val cache: RoutesMemoryCache,
    private val usageTracker: PlacesUsageTracker,
    private val connectivityRepository: ConnectivityRepository,
    private val dispatchers: DispatcherProvider,
) : RoutesRepository {

    override suspend fun computeRoute(request: RouteComputeRequest): RoutesResult = withContext(dispatchers.io) {
        if (!connectivityRepository.isConnected.first()) return@withContext RoutesResult.Offline
        val cacheKey = RoutesMemoryCache.key(request)
        cache.get(cacheKey)?.let { return@withContext RoutesResult.Ok(it) }
        val via = if (request.viaLatitude != null && request.viaLongitude != null) {
            LatLngPoint(request.viaLatitude, request.viaLongitude)
        } else {
            null
        }
        val payload = ValhallaMapper.request(
            origin = LatLngPoint(request.originLatitude, request.originLongitude),
            destination = LatLngPoint(request.destLatitude, request.destLongitude),
            via = via,
            avoidTolls = request.avoidTolls,
            avoidFerries = request.avoidFerries,
        )
        val http = Request.Builder()
            .url(MapServicesConfig.VALHALLA_ROUTE_URL)
            .header("User-Agent", MapServicesConfig.USER_AGENT)
            .post(json.encodeToString(payload).toRequestBody(JSON))
            .build()
        usageTracker.recordRoutes()
        try {
            val body = retrying { client.execute(http) }
            val trip = json.decodeFromString(ValhallaResponse.serializer(), body).trip
                ?: return@withContext RoutesResult.Empty
            val shapes = trip.legs.mapNotNull { it.shape?.takeIf(String::isNotEmpty) }
            if (shapes.isEmpty()) return@withContext RoutesResult.Empty
            val points = shapes.flatMapIndexed { index, shape ->
                val decoded = EncodedPolyline.decode(shape, precision = 6)
                if (index == 0) decoded else decoded.drop(1)
            }
            if (points.size < 2) return@withContext RoutesResult.Empty
            val summary = trip.summary
            val distanceMeters = ((summary?.length ?: 0.0) * 1000.0).roundToInt()
            val durationSeconds = (summary?.time ?: 0.0).roundToLong()
            if (distanceMeters <= 0 && durationSeconds <= 0) return@withContext RoutesResult.Empty
            val plan = RoutePlan(
                encodedPolyline = EncodedPolyline.encode(points, precision = 5),
                points = points,
                distanceMeters = distanceMeters,
                durationSeconds = durationSeconds,
                viaLabel = request.viaName?.takeIf { it.isNotBlank() },
            )
            cache.put(cacheKey, plan)
            RoutesResult.Ok(plan)
        } catch (e: HttpStatusException) {
            Timber.w("Valhalla HTTP %d", e.code)
            RoutesResult.HttpError(e.code)
        } catch (t: Throwable) {
            Timber.w(t, "Valhalla route failed")
            RoutesResult.Network
        }
    }

    private companion object {
        val JSON = "application/json".toMediaType()
    }
}
