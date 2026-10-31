package com.example.gpstracker.core.common

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * One-shot UI events (navigation, snackbars). Prefer this over putting
 * navigation side-effects in [StateFlow] state.
 */
class EventBus<T> {
    private val channel = Channel<T>(capacity = Channel.BUFFERED)
    val events: Flow<T> = channel.receiveAsFlow()

    suspend fun emit(event: T) {
        channel.send(event)
    }

    fun tryEmit(event: T): Boolean = channel.trySend(event).isSuccess
}
