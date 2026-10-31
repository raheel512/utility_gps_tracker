package com.example.gpstracker.core.places

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeRecentSearchRepository(
    private val maxRecent: Int = 10,
) : RecentSearchRepository {
    private val items = MutableStateFlow<List<RecentSearch>>(emptyList())

    override fun observeRecent(limit: Int): Flow<List<RecentSearch>> =
        items.map { it.take(limit) }

    override suspend fun saveFromDetails(details: PlaceDetails, secondaryText: String?) {
        upsert(
            RecentSearch(
                placeId = details.placeId,
                primaryText = details.name,
                secondaryText = secondaryText ?: details.formattedAddress,
                latitude = details.latitude,
                longitude = details.longitude,
                updatedAtMillis = System.currentTimeMillis(),
            ),
        )
    }

    override suspend fun saveSuggestion(
        suggestion: PlaceSuggestion,
        latitude: Double?,
        longitude: Double?,
    ) {
        upsert(
            RecentSearch(
                placeId = suggestion.placeId,
                primaryText = suggestion.primaryText,
                secondaryText = suggestion.secondaryText.ifBlank { null },
                latitude = latitude,
                longitude = longitude,
                updatedAtMillis = System.currentTimeMillis(),
            ),
        )
    }

    fun all(): List<RecentSearch> = items.value

    private fun upsert(item: RecentSearch) {
        val without = items.value.filterNot { it.placeId == item.placeId }
        items.value = (listOf(item) + without).sortedByDescending { it.updatedAtMillis }.take(maxRecent)
    }
}
