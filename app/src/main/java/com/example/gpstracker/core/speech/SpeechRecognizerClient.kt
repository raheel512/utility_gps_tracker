package com.example.gpstracker.core.speech

import kotlinx.coroutines.flow.Flow

/**
 * Speech-to-text. Implementations must create and destroy the recognizer on the main thread.
 */
interface SpeechRecognizerClient {
    val states: Flow<SpeechState>

    fun isRecognitionAvailable(): Boolean

    fun start(languageTag: String)

    fun cancel()

    fun destroy()
}
