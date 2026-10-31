package com.example.gpstracker.core.geo

import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.data.connectivity.ConnectivityRepository
import com.example.gpstracker.core.map.MapServicesConfig
import com.example.gpstracker.core.places.AutocompleteSession
import com.example.gpstracker.core.places.LatLngPoint
import com.example.gpstracker.core.places.NearbyCategory
import com.example.gpstracker.core.places.PlaceDetails
import com.example.gpstracker.core.places.PlaceSuggestion
import com.example.gpstracker.core.places.PlacesFailureReason
import com.example.gpstracker.core.places.PlacesMemoryCache
import com.example.gpstracker.core.places.PlacesRepository
import com.example.gpstracker.core.places.PlacesResult
import com.example.gpstracker.core.places.PlacesUsageTracker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

private object OsmSession : AutocompleteSession

/**
 * Photon autocomplete and Overpass nearby search.
 * Public hosts in [MapServicesConfig] are for development, not a production quota.
 */
@Singleton
class OsmPlacesRepository @Inject constructor(
    @GeoOkHttp private val client: OkHttpClient,
    private val json: Json,
    private val cache: PlacesMemoryCache,
    private val usageTracker: PlacesUsageTracker,
    private val connectivityRepository: ConnectivityRepository,
    private val dispatchers: DispatcherProvider,
) : PlacesRepository {

    private val detailsById = ConcurrentHashMap<String, PlaceDetails>()

    override fun newAutocompleteSession(): AutocompleteSession = OsmSession

    override suspend fun findAutocompletePredictions(
        query: String,
        session: AutocompleteSession,
        locationBias: LatLngPoint?,
    ): PlacesResult<List<PlaceSuggestion>> = withContext(dispatchers.io) {
        val trimmed = query.trim()
        if (trimmed.length < PlacesRepository.AUTOCOMPLETE_MIN_CHARS) {
            return@withContext PlacesResult.Ok(emptyList())
        }
        val cacheKey = PlacesMemoryCache.autocompleteKey(trimmed, locationBias)
        cache.get<List<PlaceSuggestion>>(cacheKey)?.let { return@withContext PlacesResult.Ok(it) }
        if (!connectivityRepository.isConnected.first()) {
            return@withContext PlacesResult.Failure(PlacesFailureReason.Network)
        }
        val url = MapServicesConfig.PHOTON_BASE_URL.toHttpUrl().newBuilder()
            .addPathSegment("api")
            .addQueryParameter("q", trimmed)
            .addQueryParameter("limit", "5")
            .addQueryParameter("lang", Locale.getDefault().language)
            .apply {
                if (locationBias != null) {
                    addQueryParameter("lat", locationBias.latitude.toString())
                    addQueryParameter("lon", locationBias.longitude.toString())
                }
            }
            .build()
        try {
            usageTracker.recordAutocomplete()
            val body = retrying {
                client.execute(Request.Builder().url(url).header("User-Agent", MapServicesConfig.USER_AGENT).build())
            }
            val parsed = json.decodeFromString(PhotonResponse.serializer(), body)
            PhotonMapper.details(parsed).forEach { detailsById[it.placeId] = it }
            val suggestions = PhotonMapper.suggestions(parsed)
            cache.put(cacheKey, suggestions)
            PlacesResult.Ok(suggestions)
        } catch (e: HttpStatusException) {
            Timber.w("Photon HTTP %d", e.code)
            PlacesResult.Failure(if (e.code == 429) PlacesFailureReason.Network else PlacesFailureReason.Unknown)
        } catch (t: Throwable) {
            Timber.w(t, "Photon autocomplete failed")
            PlacesResult.Failure(PlacesFailureReason.Network, t)
        }
    }

    override suspend fun fetchPlaceDetails(
        placeId: String,
        displayName: String,
        session: AutocompleteSession?,
    ): PlacesResult<PlaceDetails> {
        usageTracker.recordDetails()
        val cached = detailsById[placeId]
        return if (cached == null) {
            PlacesResult.Failure(PlacesFailureReason.Empty)
        } else {
            PlacesResult.Ok(cached.copy(name = displayName.ifBlank { cached.name }))
        }
    }

    override suspend fun searchNearby(
        includedType: String,
        center: LatLngPoint,
        radiusMeters: Double,
        maxResultCount: Int,
    ): PlacesResult<List<com.example.gpstracker.core.places.PlaceSummary>> = withContext(dispatchers.io) {
        val category = NearbyCategory.fromId(includedType)
            ?: return@withContext PlacesResult.Failure(PlacesFailureReason.Unknown)
        val cacheKey = PlacesMemoryCache.nearbyKey(category.id, center)
        cache.get<List<com.example.gpstracker.core.places.PlaceSummary>>(cacheKey)?.let {
            return@withContext PlacesResult.Ok(it)
        }
        if (!connectivityRepository.isConnected.first()) {
            return@withContext PlacesResult.Failure(PlacesFailureReason.Network)
        }
        val query = overpassQuery(
            key = category.osmKey,
            value = category.osmValue,
            extraKey = category.osmExtraKey,
            extraValue = category.osmExtraValue,
            latitude = center.latitude,
            longitude = center.longitude,
            radiusMeters = radiusMeters,
        )
        val request = Request.Builder()
            .url(MapServicesConfig.OVERPASS_URL)
            .header("User-Agent", MapServicesConfig.USER_AGENT)
            .post(okhttp3.FormBody.Builder().add("data", query).build())
            .build()
        try {
            usageTracker.recordNearby()
            val body = retrying { client.execute(request) }
            val parsed = json.decodeFromString(OverpassResponse.serializer(), body)
            val places = OverpassMapper.summaries(parsed, maxResultCount)
            cache.put(cacheKey, places)
            PlacesResult.Ok(places)
        } catch (e: HttpStatusException) {
            Timber.w("Overpass HTTP %d", e.code)
            PlacesResult.Failure(PlacesFailureReason.Network)
        } catch (t: Throwable) {
            Timber.w(t, "Overpass nearby failed")
            PlacesResult.Failure(PlacesFailureReason.Network, t)
        }
    }
}
