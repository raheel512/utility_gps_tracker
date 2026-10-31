package com.example.gpstracker.core.routes

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

/**
 * Opens a nearby category in the Google Maps app. This is an external handoff,
 * not a Maps Platform request.
 */
object GoogleMapsSearch {
    const val PACKAGE_NAME: String = "com.google.android.apps.maps"

    fun nearbyUri(categoryQuery: String): String {
        val encoded = URLEncoder.encode(
            "${categoryQuery.trim()} near me",
            StandardCharsets.UTF_8.name(),
        )
        return "https://www.google.com/maps/search/?api=1&query=$encoded"
    }
}

fun Context.openNearbyInGoogleMaps(categoryQuery: String): Boolean {
    val uri = GoogleMapsSearch.nearbyUri(categoryQuery).toUri()
    val maps = Intent(Intent.ACTION_VIEW, uri).apply {
        setPackage(GoogleMapsSearch.PACKAGE_NAME)
    }
    return try {
        startActivity(maps)
        true
    } catch (_: ActivityNotFoundException) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
            true
        } catch (_: ActivityNotFoundException) {
            false
        }
    }
}
