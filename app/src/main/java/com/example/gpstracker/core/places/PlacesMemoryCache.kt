package com.example.gpstracker.core.places

/**
 * In-memory LRU cache for Places responses. Keys should include query/category and
 * location rounded to ~100 m. Default TTL is 10 minutes.
 */
class PlacesMemoryCache(
    private val maxEntries: Int = DEFAULT_MAX_ENTRIES,
    private val ttlMs: Long = DEFAULT_TTL_MS,
    private val nowMs: () -> Long = { System.currentTimeMillis() },
) {
    private val lock = Any()
    private val map = object : LinkedHashMap<String, CacheEntry>(16, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, CacheEntry>?): Boolean =
            size > maxEntries
    }

    fun <T> get(key: String): T? = synchronized(lock) {
        val entry = map[key] ?: return null
        if (nowMs() - entry.storedAtMs > ttlMs) {
            map.remove(key)
            return null
        }
        @Suppress("UNCHECKED_CAST")
        entry.value as T
    }

    fun put(key: String, value: Any) = synchronized(lock) {
        map[key] = CacheEntry(value, nowMs())
    }

    fun clear() = synchronized(lock) { map.clear() }

    fun size(): Int = synchronized(lock) { map.size }

    private data class CacheEntry(val value: Any, val storedAtMs: Long)

    companion object {
        const val DEFAULT_MAX_ENTRIES = 64
        const val DEFAULT_TTL_MS = 10 * 60 * 1000L

        /** ~100 m grid: 0.001° latitude ≈ 111 m. */
        fun locationKey(latitude: Double, longitude: Double): String {
            val lat = Math.round(latitude * 1_000.0)
            val lng = Math.round(longitude * 1_000.0)
            return "$lat,$lng"
        }

        fun autocompleteKey(query: String, bias: LatLngPoint?): String {
            val loc = bias?.let { locationKey(it.latitude, it.longitude) } ?: "nobias"
            return "ac:${query.trim().lowercase()}:$loc"
        }

        fun nearbyKey(type: String, center: LatLngPoint): String =
            "nb:$type:${locationKey(center.latitude, center.longitude)}"
    }
}
