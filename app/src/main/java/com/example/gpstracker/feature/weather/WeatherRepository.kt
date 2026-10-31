package com.example.gpstracker.feature.weather

import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.data.connectivity.ConnectivityRepository
import com.example.gpstracker.core.geo.GeoOkHttp
import com.example.gpstracker.core.geo.HttpStatusException
import com.example.gpstracker.core.geo.execute
import com.example.gpstracker.core.geo.retrying
import com.example.gpstracker.core.map.MapServicesConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

data class WeatherSnapshot(
    val temperatureCelsius: Double,
    val humidityPercent: Double,
    val cloudPercent: Double,
    val pressureHpa: Double,
    val windMps: Double,
    val weatherCode: Int,
    val observedAt: String,
    val sunrise: String,
    val sunset: String,
)

@Singleton
class WeatherRepository @Inject constructor(
    @GeoOkHttp private val client: OkHttpClient,
    private val json: Json,
    private val connectivityRepository: ConnectivityRepository,
    private val dispatchers: DispatcherProvider,
) {
    suspend fun forecast(latitude: Double, longitude: Double): WeatherSnapshot? {
        if (!connectivityRepository.isConnected.first()) return null
        val url = MapServicesConfig.OPEN_METEO_FORECAST_URL.toHttpUrl().newBuilder()
            .addQueryParameter("latitude", latitude.toString())
            .addQueryParameter("longitude", longitude.toString())
            .addQueryParameter(
                "current",
                "temperature_2m,relative_humidity_2m,cloud_cover,pressure_msl,wind_speed_10m,weather_code",
            )
            .addQueryParameter("daily", "sunrise,sunset")
            .addQueryParameter("timezone", "auto")
            .addQueryParameter("forecast_days", "1")
            .addQueryParameter("wind_speed_unit", "ms")
            .build()
        return withContext(dispatchers.io) {
            try {
                val body = retrying {
                    client.execute(
                        Request.Builder()
                            .url(url)
                            .header("User-Agent", MapServicesConfig.USER_AGENT)
                            .build(),
                    )
                }
                json.decodeFromString(OpenMeteoForecast.serializer(), body).toSnapshot()
            } catch (e: HttpStatusException) {
                Timber.w("Open-Meteo HTTP %d", e.code)
                null
            } catch (t: Throwable) {
                Timber.w(t, "Open-Meteo forecast failed")
                null
            }
        }
    }
}

@Serializable
private data class OpenMeteoForecast(
    val current: OpenMeteoCurrent? = null,
    val daily: OpenMeteoDaily? = null,
)

@Serializable
private data class OpenMeteoCurrent(
    val time: String? = null,
    @SerialName("temperature_2m") val temperature: Double? = null,
    @SerialName("relative_humidity_2m") val humidity: Double? = null,
    @SerialName("cloud_cover") val cloudCover: Double? = null,
    @SerialName("pressure_msl") val pressure: Double? = null,
    @SerialName("wind_speed_10m") val windSpeed: Double? = null,
    @SerialName("weather_code") val weatherCode: Int? = null,
)

@Serializable
private data class OpenMeteoDaily(
    val sunrise: List<String> = emptyList(),
    val sunset: List<String> = emptyList(),
)

private fun OpenMeteoForecast.toSnapshot(): WeatherSnapshot? {
    val now = current ?: return null
    return WeatherSnapshot(
        temperatureCelsius = now.temperature ?: return null,
        humidityPercent = now.humidity ?: return null,
        cloudPercent = now.cloudCover ?: return null,
        pressureHpa = now.pressure ?: return null,
        windMps = now.windSpeed ?: return null,
        weatherCode = now.weatherCode ?: return null,
        observedAt = now.time ?: return null,
        sunrise = daily?.sunrise?.firstOrNull() ?: return null,
        sunset = daily.sunset.firstOrNull() ?: return null,
    )
}
