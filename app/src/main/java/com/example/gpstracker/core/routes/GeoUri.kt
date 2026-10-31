package com.example.gpstracker.core.routes

import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.Locale

/**
 * geo URI for the destination pin. Avoid flags are not part of this URI;
 * they are applied on the Valhalla request that draws the in-app route.
 */
object GeoUri {
    fun pin(latitude: Double, longitude: Double, label: String? = null): String {
        val lat = String.format(Locale.ROOT, "%.6f", latitude)
        val lng = String.format(Locale.ROOT, "%.6f", longitude)
        val trimmed = label?.trim().orEmpty()
        if (trimmed.isEmpty()) return "geo:$lat,$lng"
        val query = URLEncoder.encode("$lat,$lng($trimmed)", StandardCharsets.UTF_8.name())
        return "geo:$lat,$lng?q=$query"
    }
}
