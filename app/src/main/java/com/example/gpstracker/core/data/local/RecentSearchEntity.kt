package com.example.gpstracker.core.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val placeId: String,
    val primaryText: String,
    val secondaryText: String?,
    val latitude: Double?,
    val longitude: Double?,
    val updatedAtMillis: Long,
)
