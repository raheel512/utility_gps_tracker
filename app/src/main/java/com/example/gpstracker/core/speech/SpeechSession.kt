package com.example.gpstracker.core.speech

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

/**
 * Forwards recognizer states. The first [SpeechError.Busy] cancels and starts once more.
 */
class SpeechSession(
    private val client: SpeechRecognizerClient,
) {
    private val _states = MutableSharedFlow<SpeechState>(extraBufferCapacity = 32)
    val states: Flow<SpeechState> = _states

    private var job: Job? = null
    private var languageTag: String = ""
    private var busyRetried: Boolean = false

    fun start(scope: CoroutineScope, languageTag: String) {
        this.languageTag = languageTag
        busyRetried = false
        job?.cancel()
        job = scope.launch {
            client.states.collect { state ->
                if (state is SpeechState.Error &&
                    state.reason == SpeechError.Busy &&
                    !busyRetried
                ) {
                    busyRetried = true
                    client.cancel()
                    client.start(languageTag)
                } else {
                    _states.emit(state)
                }
            }
        }
        client.start(languageTag)
    }

    fun cancel() {
        client.cancel()
    }

    fun destroy() {
        job?.cancel()
        job = null
        client.destroy()
    }
}
