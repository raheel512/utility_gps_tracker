package com.example.gpstracker.core.data.connectivity

import kotlinx.coroutines.flow.Flow

/**
 * Exposes network connectivity as a stream.
 *
 * Home "Connected" chip = network available (this repository).
 */
interface ConnectivityRepository {
    /** Emits `true` while at least one network with internet is available. */
    val isConnected: Flow<Boolean>
}
