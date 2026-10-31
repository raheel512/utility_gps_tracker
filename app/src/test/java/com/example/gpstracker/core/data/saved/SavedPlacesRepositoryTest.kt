package com.example.gpstracker.core.data.saved

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SavedPlacesRepositoryTest {

    private val repo = FakeSavedPlacesRepository()

    @Test
    fun `toggleSaved adds then removes by placeId`() = runTest {
        val request = SavePlaceRequest(
            placeId = "abc",
            name = "Cafe",
            address = "Street",
            latitude = 33.7,
            longitude = 73.0,
        )
        val saved = repo.toggleSaved(request)
        assertTrue(saved is ToggleSavedResult.Saved)
        assertEquals(1, repo.all().size)

        val removed = repo.toggleSaved(request)
        assertTrue(removed is ToggleSavedResult.Removed)
        assertEquals(0, repo.all().size)
    }

    @Test
    fun `dedupe by rounded coordinates when placeId missing`() = runTest {
        repo.save(
            SavePlaceRequest(
                placeId = null,
                name = "A",
                address = null,
                latitude = 33.684411,
                longitude = 73.047911,
            ),
        )
        repo.save(
            SavePlaceRequest(
                placeId = null,
                name = "B",
                address = null,
                // Same 5-decimal keys as above (round half-up × 1e5).
                latitude = 33.684414,
                longitude = 73.047914,
            ),
        )
        assertEquals(1, repo.all().size)
        assertEquals("B", repo.all().first().name)
    }

    @Test
    fun `undo restore puts place back`() = runTest {
        val saved = (repo.toggleSaved(
            SavePlaceRequest("id1", "Home", "Addr", 1.0, 2.0),
        ) as ToggleSavedResult.Saved).place
        val removed = repo.remove(saved.id)!!
        assertEquals(0, repo.all().size)
        repo.restore(removed)
        assertEquals(1, repo.all().size)
        assertEquals("Home", repo.all().first().name)
    }
}
