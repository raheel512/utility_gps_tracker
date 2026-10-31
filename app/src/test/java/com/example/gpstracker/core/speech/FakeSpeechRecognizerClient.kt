package com.example.gpstracker.core.speech

import kotlinx.coroutines.flow.MutableSharedFlow

class FakeSpeechRecognizerClient : SpeechRecognizerClient {
    private val _states = MutableSharedFlow<SpeechState>(extraBufferCapacity = 32)
    override val states = _states

    var available = true
    var startCount = 0
    var cancelCount = 0
    var destroyCount = 0
    var lastLanguage: String? = null

    override fun isRecognitionAvailable(): Boolean = available

    override fun start(languageTag: String) {
        startCount++
        lastLanguage = languageTag
    }

    override fun cancel() {
        cancelCount++
    }

    override fun destroy() {
        destroyCount++
    }

    fun emit(state: SpeechState) {
        _states.tryEmit(state)
    }
}
