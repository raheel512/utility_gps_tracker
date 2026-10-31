package com.example.gpstracker.core.speech

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SpeechSessionTest {
    @Test
    fun `forwards listening partial result error and cancel`() = runTest {
        val client = FakeSpeechRecognizerClient()
        val session = SpeechSession(client)
        val seen = mutableListOf<SpeechState>()
        val sessionScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            session.states.collect { seen.add(it) }
        }
        session.start(sessionScope, "en")
        client.emit(SpeechState.Listening("fai", 0.4f))
        client.emit(SpeechState.Result(listOf("faisal mosque")))
        client.emit(SpeechState.Error(SpeechError.NoMatch))
        session.cancel()
        session.destroy()
        assertEquals(1, client.startCount)
        assertEquals("en", client.lastLanguage)
        assertEquals(1, client.cancelCount)
        assertTrue(seen.any { it is SpeechState.Listening && it.partialText == "fai" && it.level == 0.4f })
        assertTrue(seen.any { it is SpeechState.Result && it.transcripts == listOf("faisal mosque") })
        assertTrue(seen.any { it is SpeechState.Error && it.reason == SpeechError.NoMatch })
    }

    @Test
    fun `retries once when recognizer is busy`() = runTest {
        val client = FakeSpeechRecognizerClient()
        val session = SpeechSession(client)
        val seen = mutableListOf<SpeechState>()
        val sessionScope = CoroutineScope(UnconfinedTestDispatcher(testScheduler))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            session.states.collect { seen.add(it) }
        }
        session.start(sessionScope, "en-US")
        client.emit(SpeechState.Error(SpeechError.Busy))
        assertEquals(2, client.startCount)
        assertEquals(1, client.cancelCount)
        assertTrue(seen.none { it is SpeechState.Error && it.reason == SpeechError.Busy })
        client.emit(SpeechState.Error(SpeechError.Busy))
        session.destroy()
        assertEquals(2, client.startCount)
        assertTrue(seen.any { it is SpeechState.Error && it.reason == SpeechError.Busy })
    }
}
