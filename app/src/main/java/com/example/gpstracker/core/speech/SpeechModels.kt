package com.example.gpstracker.core.speech

/**
 * Recognizer states. [Listening.level] is 0..1 after RMS normalization.
 * [Result.transcripts] holds at most five alternatives, best first.
 */
sealed interface SpeechState {
    data object Idle : SpeechState

    data class Listening(
        val partialText: String,
        val level: Float,
    ) : SpeechState

    data object Processing : SpeechState

    data class Result(val transcripts: List<String>) : SpeechState

    data class Error(val reason: SpeechError) : SpeechState
}

enum class SpeechError {
    NoMatch,
    SpeechTimeout,
    Network,
    Busy,
    InsufficientPermissions,
    Client,
    Server,
    Audio,
    ;

    companion object {
        /** Android [android.speech.SpeechRecognizer] error ints. Not the stub constants. */
        const val NETWORK_TIMEOUT = 1
        const val NETWORK = 2
        const val AUDIO = 3
        const val SERVER = 4
        const val CLIENT = 5
        const val SPEECH_TIMEOUT = 6
        const val NO_MATCH = 7
        const val BUSY = 8
        const val INSUFFICIENT_PERMISSIONS = 9
        const val SERVER_DISCONNECTED = 11

        fun fromCode(code: Int): SpeechError = when (code) {
            NETWORK_TIMEOUT, NETWORK -> Network
            AUDIO -> Audio
            SERVER, SERVER_DISCONNECTED -> Server
            CLIENT -> Client
            SPEECH_TIMEOUT -> SpeechTimeout
            NO_MATCH -> NoMatch
            BUSY -> Busy
            INSUFFICIENT_PERMISSIONS -> InsufficientPermissions
            else -> Client
        }
    }
}

/**
 * Maps [android.speech.RecognitionListener.onRmsChanged] into 0..1, then smooths.
 * The recognizer reports roughly -2 dB to 10 dB.
 */
object RmsLevel {
    const val MIN_DB = -2f
    const val MAX_DB = 10f

    fun normalize(db: Float): Float =
        ((db - MIN_DB) / (MAX_DB - MIN_DB)).coerceIn(0f, 1f)

    fun smooth(previous: Float, sample: Float): Float =
        (previous * 0.6f) + (sample * 0.4f)
}
