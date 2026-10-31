package com.example.gpstracker.core.places

import com.example.gpstracker.BuildConfig
import timber.log.Timber
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Debug-only counters for Places API calls this process. No UI.
 */
@Singleton
class PlacesUsageTracker @Inject constructor() {
    private val autocomplete = AtomicInteger(0)
    private val nearby = AtomicInteger(0)
    private val details = AtomicInteger(0)
    private val routes = AtomicInteger(0)

    fun recordAutocomplete() {
        val count = autocomplete.incrementAndGet()
        logIfDebug(count, nearby.get(), details.get(), routes.get())
    }

    fun recordNearby() {
        val count = nearby.incrementAndGet()
        logIfDebug(autocomplete.get(), count, details.get(), routes.get())
    }

    fun recordDetails() {
        val count = details.incrementAndGet()
        logIfDebug(autocomplete.get(), nearby.get(), count, routes.get())
    }

    fun recordRoutes() {
        val count = routes.incrementAndGet()
        logIfDebug(autocomplete.get(), nearby.get(), details.get(), count)
    }

    fun snapshot(): UsageSnapshot = UsageSnapshot(
        autocomplete = autocomplete.get(),
        nearby = nearby.get(),
        details = details.get(),
        routes = routes.get(),
    )

    private fun logIfDebug(ac: Int, nb: Int, det: Int, rt: Int) {
        if (BuildConfig.DEBUG) {
            Timber.d(
                "PlacesUsage autocomplete=%d nearby=%d details=%d routes=%d",
                ac,
                nb,
                det,
                rt,
            )
        }
    }

    data class UsageSnapshot(
        val autocomplete: Int,
        val nearby: Int,
        val details: Int,
        val routes: Int,
    )
}
