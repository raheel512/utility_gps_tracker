package com.example.gpstracker.core.places

import com.example.gpstracker.core.common.DispatcherProvider
import com.example.gpstracker.core.data.local.RecentSearchDao
import com.example.gpstracker.core.data.local.RecentSearchEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class RecentSearch(
    val placeId: String,
    val primaryText: String,
    val secondaryText: String?,
    val latitude: Double?,
    val longitude: Double?,
    val updatedAtMillis: Long,
)

interface RecentSearchRepository {
    fun observeRecent(limit: Int = RecentSearchDao.MAX_RECENT): Flow<List<RecentSearch>>

    suspend fun saveFromDetails(details: PlaceDetails, secondaryText: String?)

    suspend fun saveSuggestion(
        suggestion: PlaceSuggestion,
        latitude: Double?,
        longitude: Double?,
    )
}

@Singleton
class RoomRecentSearchRepository @Inject constructor(
    private val dao: RecentSearchDao,
    private val dispatchers: DispatcherProvider,
) : RecentSearchRepository {

    override fun observeRecent(limit: Int): Flow<List<RecentSearch>> =
        dao.observeRecent(limit)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatchers.io)

    override suspend fun saveFromDetails(
        details: PlaceDetails,
        secondaryText: String?,
    ) = withContext(dispatchers.io) {
        upsertAndTrim(
            RecentSearchEntity(
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
    ) = withContext(dispatchers.io) {
        upsertAndTrim(
            RecentSearchEntity(
                placeId = suggestion.placeId,
                primaryText = suggestion.primaryText,
                secondaryText = suggestion.secondaryText.ifBlank { null },
                latitude = latitude,
                longitude = longitude,
                updatedAtMillis = System.currentTimeMillis(),
            ),
        )
    }

    private suspend fun upsertAndTrim(entity: RecentSearchEntity) {
        dao.upsert(entity)
        dao.trimTo(RecentSearchDao.MAX_RECENT)
    }

    private fun RecentSearchEntity.toDomain() = RecentSearch(
        placeId = placeId,
        primaryText = primaryText,
        secondaryText = secondaryText,
        latitude = latitude,
        longitude = longitude,
        updatedAtMillis = updatedAtMillis,
    )
}
