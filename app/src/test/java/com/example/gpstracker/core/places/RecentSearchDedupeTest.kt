package com.example.gpstracker.core.places

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RecentSearchDedupeTest {

    @Test
    fun `dedupes by place id and keeps latest 10`() = runTest {
        val repo = FakeRecentSearchRepository(maxRecent = 10)
        repeat(12) { index ->
            repo.saveSuggestion(
                PlaceSuggestion(
                    placeId = "id-$index",
                    primaryText = "Place $index",
                    secondaryText = "Addr",
                ),
                latitude = null,
                longitude = null,
            )
        }
        assertEquals(10, repo.all().size)
        assertEquals("id-11", repo.all().first().placeId)

        repo.saveSuggestion(
            PlaceSuggestion(placeId = "id-11", primaryText = "Updated", secondaryText = ""),
            latitude = 1.0,
            longitude = 2.0,
        )
        assertEquals(10, repo.all().size)
        assertEquals(1, repo.all().count { it.placeId == "id-11" })
        assertEquals("Updated", repo.all().first { it.placeId == "id-11" }.primaryText)
    }
}
