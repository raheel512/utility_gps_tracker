package com.example.gpstracker.core.places

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Test

class AutocompleteSessionManagerTest {

    @Test
    fun `reset creates a new session and bumps generation`() {
        val places = FakePlacesRepository()
        val manager = DefaultAutocompleteSessionManager(places)
        val first = manager.current()
        assertEquals(0, manager.generation())
        manager.reset()
        val second = manager.current()
        assertEquals(1, manager.generation())
        assertNotSame(first, second)
        assertEquals(2, places.sessionsCreated)
    }
}
