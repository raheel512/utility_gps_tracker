package com.example.gpstracker.core.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject

class AndroidSpeechRecognizerClient @Inject constructor(
    @ApplicationContext private val context: Context,
) : SpeechRecognizerClient {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val _states = MutableSharedFlow<SpeechState>(extraBufferCapacity = 32)
    override val states: Flow<SpeechState> = _states

    private var recognizer: SpeechRecognizer? = null
    private var partialText: String = ""
    private var level: Float = 0f

    override fun isRecognitionAvailable(): Boolean =
        SpeechRecognizer.isRecognitionAvailable(context)

    override fun start(languageTag: String) {
        mainHandler.post {
            partialText = ""
            level = 0f
            val active = recognizer ?: SpeechRecognizer.createSpeechRecognizer(context).also {
                it.setRecognitionListener(listener)
                recognizer = it
            }
            active.startListening(recognizeIntent(languageTag))
        }
    }

    override fun cancel() {
        mainHandler.post { recognizer?.cancel() }
    }

    override fun destroy() {
        mainHandler.post {
            recognizer?.destroy()
            recognizer = null
            partialText = ""
            level = 0f
            _states.tryEmit(SpeechState.Idle)
        }
    }

    private fun recognizeIntent(languageTag: String): Intent =
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, languageTag)
        }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _states.tryEmit(SpeechState.Listening(partialText, level))
        }

        override fun onBeginningOfSpeech() = Unit

        override fun onRmsChanged(rmsdB: Float) {
            level = RmsLevel.smooth(level, RmsLevel.normalize(rmsdB))
            _states.tryEmit(SpeechState.Listening(partialText, level))
        }

        override fun onBufferReceived(buffer: ByteArray?) = Unit

        override fun onEndOfSpeech() {
            _states.tryEmit(SpeechState.Processing)
        }

        override fun onError(error: Int) {
            _states.tryEmit(SpeechState.Error(SpeechError.fromCode(error)))
        }

        override fun onResults(results: Bundle?) {
            val transcripts = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                .orEmpty()
                .take(MAX_ALTERNATIVES)
            _states.tryEmit(SpeechState.Result(transcripts))
        }

        override fun onPartialResults(partialResults: Bundle?) {
            partialText = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                .orEmpty()
            _states.tryEmit(SpeechState.Listening(partialText, level))
        }

        override fun onEvent(eventType: Int, params: Bundle?) = Unit
    }

    private companion object {
        const val MAX_ALTERNATIVES = 5
    }
}
