package com.example.gpstracker.core.speech

import org.junit.Assert.assertEquals
import org.junit.Test

class SpeechErrorAndRmsTest {
    @Test
    fun `maps every recognizer error code`() {
        assertEquals(SpeechError.Network, SpeechError.fromCode(SpeechError.NETWORK_TIMEOUT))
        assertEquals(SpeechError.Network, SpeechError.fromCode(SpeechError.NETWORK))
        assertEquals(SpeechError.Audio, SpeechError.fromCode(SpeechError.AUDIO))
        assertEquals(SpeechError.Server, SpeechError.fromCode(SpeechError.SERVER))
        assertEquals(SpeechError.Server, SpeechError.fromCode(SpeechError.SERVER_DISCONNECTED))
        assertEquals(SpeechError.Client, SpeechError.fromCode(SpeechError.CLIENT))
        assertEquals(SpeechError.SpeechTimeout, SpeechError.fromCode(SpeechError.SPEECH_TIMEOUT))
        assertEquals(SpeechError.NoMatch, SpeechError.fromCode(SpeechError.NO_MATCH))
        assertEquals(SpeechError.Busy, SpeechError.fromCode(SpeechError.BUSY))
        assertEquals(
            SpeechError.InsufficientPermissions,
            SpeechError.fromCode(SpeechError.INSUFFICIENT_PERMISSIONS),
        )
        assertEquals(SpeechError.Client, SpeechError.fromCode(99))
    }

    @Test
    fun `rms stays inside 0 to 1 and smooths`() {
        assertEquals(0f, RmsLevel.normalize(-10f), 0f)
        assertEquals(0f, RmsLevel.normalize(RmsLevel.MIN_DB), 0f)
        assertEquals(1f, RmsLevel.normalize(RmsLevel.MAX_DB), 0f)
        assertEquals(1f, RmsLevel.normalize(40f), 0f)
        assertEquals(0.5f, RmsLevel.normalize(4f), 0.001f)
        val smoothed = RmsLevel.smooth(previous = 0.2f, sample = 1f)
        assertEquals(0.2f * 0.6f + 1f * 0.4f, smoothed, 0.0001f)
        assertEquals(0f, RmsLevel.smooth(0f, 0f), 0f)
        assertEquals(1f, RmsLevel.smooth(1f, 1f), 0.0001f)
    }
}
