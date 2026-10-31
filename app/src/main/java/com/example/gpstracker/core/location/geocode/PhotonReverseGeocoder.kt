package com.example.gpstracker.core.location.geocode

import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.data.connectivity.ConnectivityRepository
import com.example.gpstracker.core.geo.GeoOkHttp
import com.example.gpstracker.core.geo.HttpStatusException
import com.example.gpstracker.core.geo.PhotonMapper
import com.example.gpstracker.core.geo.PhotonResponse
import com.example.gpstracker.core.geo.execute
import com.example.gpstracker.core.geo.retrying
import com.example.gpstracker.core.map.MapServicesConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

/**
 * Reverse geocoding through Photon. The public demo host is for development only.
 * Returns null when offline, the service fails, or Photon has no address parts.
 */
@Singleton
class PhotonReverseGeocoder @Inject constructor(
    @GeoOkHttp private val client: OkHttpClient,
    private val json: Json,
    private val connectivityRepository: ConnectivityRepository,
    private val dispatchers: DispatcherProvider,
) : ReverseGeocoder {

    private data class CacheKey(val lat: Long, val lng: Long, val languageTag: String)

    private val mutex = Mutex()
    private val cache = object : LinkedHashMap<CacheKey, GeocodedAddress>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<CacheKey, GeocodedAddress>?): Boolean =
            size > MAX_CACHE_ENTRIES
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): GeocodedAddress? {
        if (!connectivityRepository.isConnected.first()) return null
        val language = Locale.getDefault().toLanguageTag()
        val key = CacheKey(
            lat = (latitude * CACHE_SCALE).roundToLong(),
            lng = (longitude * CACHE_SCALE).roundToLong(),
            languageTag = language,
        )
        mutex.withLock { cache[key] }?.let { return it }
        val url = MapServicesConfig.PHOTON_BASE_URL.toHttpUrl().newBuilder()
            .addPathSegment("reverse")
            .addQueryParameter("lat", latitude.toString())
            .addQueryParameter("lon", longitude.toString())
            .addQueryParameter("lang", Locale.getDefault().language)
            .build()
        val address = withContext(dispatchers.io) {
            try {
                val body = retrying {
                    client.execute(
                        Request.Builder().url(url).header("User-Agent", MapServicesConfig.USER_AGENT).build(),
                    )
                }
                val feature = json.decodeFromString(PhotonResponse.serializer(), body).features.firstOrNull()
                    ?: return@withContext null
                val parts = PhotonMapper.addressParts(feature.properties)
                GeocodedAddress(
                    thoroughfare = parts.thoroughfare,
                    subLocality = parts.subLocality,
                    locality = parts.locality,
                    countryName = parts.countryName,
                ).takeIf { it.placeLabel() != null }
            } catch (e: HttpStatusException) {
                Timber.w("Photon reverse HTTP %d", e.code)
                null
            } catch (t: Throwable) {
                Timber.w(t, "Photon reverse geocoding failed")
                null
            }
        } ?: nominatimReverse(latitude, longitude)
        if (address != null) mutex.withLock { cache[key] = address }
        return address
    }

    private suspend fun nominatimReverse(latitude: Double, longitude: Double): GeocodedAddress? =
        withContext(dispatchers.io) {
            val url = NOMINATIM_REVERSE_URL.toHttpUrl().newBuilder()
                .addQueryParameter("format", "jsonv2")
                .addQueryParameter("lat", latitude.toString())
                .addQueryParameter("lon", longitude.toString())
                .addQueryParameter("zoom", "14")
                .addQueryParameter("addressdetails", "1")
                .addQueryParameter("accept-language", Locale.getDefault().toLanguageTag())
                .build()
            try {
                val body = retrying {
                    client.execute(
                        Request.Builder()
                            .url(url)
                            .header("User-Agent", MapServicesConfig.USER_AGENT)
                            .header("Accept-Language", Locale.getDefault().toLanguageTag())
                            .build(),
                    )
                }
                json.decodeFromString(NominatimReverse.serializer(), body).toAddress()
            } catch (e: HttpStatusException) {
                Timber.w("Nominatim reverse HTTP %d", e.code)
                null
            } catch (t: Throwable) {
                Timber.w(t, "Nominatim reverse geocoding failed")
                null
            }
        }

    private companion object {
        const val CACHE_SCALE = 10_000.0
        const val MAX_CACHE_ENTRIES = 32
        const val NOMINATIM_REVERSE_URL = "https://nominatim.openstreetmap.org/reverse"
    }
}

@Serializable
private data class NominatimReverse(
    val name: String? = null,
    val address: NominatimAddress? = null,
)

@Serializable
private data class NominatimAddress(
    val suburb: String? = null,
    val neighbourhood: String? = null,
    val quarter: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val state: String? = null,
    val country: String? = null,
    val road: String? = null,
)

private fun NominatimReverse.toAddress(): GeocodedAddress? {
    fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
    val parts = address
    val address = GeocodedAddress(
        thoroughfare = parts?.road.clean(),
        subLocality = parts?.suburb.clean()
            ?: parts?.neighbourhood.clean()
            ?: parts?.quarter.clean(),
        locality = parts?.city.clean()
            ?: parts?.town.clean()
            ?: parts?.village.clean()
            ?: parts?.state.clean()
            ?: name.clean(),
        countryName = parts?.country.clean(),
    )
    return address.takeIf { it.placeLabel() != null }
}
