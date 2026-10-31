package com.example.gpstracker.core.location

import kotlinx.coroutines.flow.Flow

/**
 * Location access contract. Implementations must not expose Android Location types.
 */
interface LocationRepository {
    /**
     * One-shot high-accuracy fix. Returns null on timeout, missing permission,
     * or provider failure.
     */
    suspend fun getCurrentLocation(timeoutMs: Long = DEFAULT_CURRENT_TIMEOUT_MS): UserLocation?

    /**
     * Continuous updates. Stops the fused request when there are no collectors.
     */
    fun locationUpdates(intervalMs: Long = DEFAULT_UPDATE_INTERVAL_MS): Flow<UserLocation>

    companion object {
        const val DEFAULT_CURRENT_TIMEOUT_MS = 15_000L
        const val DEFAULT_UPDATE_INTERVAL_MS = 5_000L
    }
}
