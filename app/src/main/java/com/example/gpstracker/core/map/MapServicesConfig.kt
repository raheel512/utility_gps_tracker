package com.example.gpstracker.core.map

/**
 * Tile and geoservice endpoints. Public hosts are for development and light testing.
 * Point these at a self-hosted Photon, Overpass, Valhalla, or MapLibre style later.
 * None of these services takes a Google Maps API key.
 */
object MapServicesConfig {
    const val STYLE_URL: String = "https://tiles.openfreemap.org/styles/liberty"
    const val PHOTON_BASE_URL: String = "https://photon.komoot.io"
    const val OVERPASS_URL: String = "https://overpass-api.de/api/interpreter"
    // valhalla.openstreetmap.de is the demo page and rejects POST /route with 405.
    // The routing API is on valhalla1.
    const val VALHALLA_ROUTE_URL: String = "https://valhalla1.openstreetmap.de/route"
    const val OPEN_METEO_FORECAST_URL: String = "https://api.open-meteo.com/v1/forecast"

    const val CONNECT_TIMEOUT_SEC: Long = 15
    const val READ_TIMEOUT_SEC: Long = 25
    const val USER_AGENT: String = "GpsTracker/1.0 (Android)"
}
