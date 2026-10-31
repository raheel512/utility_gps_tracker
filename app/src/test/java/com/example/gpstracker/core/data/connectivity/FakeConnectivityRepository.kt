package com.example.gpstracker.core.data.connectivity

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Test double for [ConnectivityRepository] with a settable connectivity state.
 */
class FakeConnectivityRepository(
    initial: Boolean = true,
) : ConnectivityRepository {
    private val state = MutableStateFlow(initial)
    override val isConnected: Flow<Boolean> = state

    fun setConnected(connected: Boolean) {
        state.value = connected
    }
}
