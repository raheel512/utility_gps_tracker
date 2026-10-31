package com.example.gpstracker.core.routes

import com.example.gpstracker.core.places.PlacesMemoryCache

class RoutesMemoryCache(
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val ttlMs: Long = DEFAULT_TTL_MS,
    private val nowMs: () -> Long = { System.currentTimeMillis() },
) {
    private val lock = Any()
    private val map = object : LinkedHashMap<String, CacheEntry>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>?): Boolean =
            size > maxEntries
    }

    fun get(key: String): RoutePlan? = synchronized(lock) {
        val entry = map[key] ?: return null
        if (nowMs() - entry.storedAtMs > ttlMs) {
            map.remove(key)
            return null
        }
        entry.plan
    }

    fun put(key: String, plan: RoutePlan) = synchronized(lock) {
        map[key] = CacheEntry(plan, nowMs())
    }

    fun clear() = synchronized(lock) { map.clear() }

    private data class CacheEntry(val plan: RoutePlan, val storedAtMs: Long)

    companion object {
        const val DEFAULT_MAX_ENTRIES = 16
        const val DEFAULT_TTL_MS = 5 * 60 * 1000L

        fun key(request: RouteComputeRequest): String {
            val origin = PlacesMemoryCache.locationKey(
                request.originLatitude,
                request.originLongitude,
            )
            val dest = request.destPlaceId?.takeIf { it.isNotBlank() }
                ?: "${round5(request.destLatitude)},${round5(request.destLongitude)}"
            val via = request.viaPlaceId?.takeIf { it.isNotBlank() }
                ?: if (request.viaLatitude != null && request.viaLongitude != null) {
                    "${round5(request.viaLatitude)},${round5(request.viaLongitude)}"
                } else {
                    "novia"
                }
            return "rt:$origin:$dest:$via:${request.avoidTolls}:${request.avoidFerries}"
        }

        private fun round5(value: Double): Long = Math.round(value * 100_000.0)
    }
}
