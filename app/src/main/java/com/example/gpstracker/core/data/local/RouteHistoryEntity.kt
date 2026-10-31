package com.example.gpstracker.core.data.local

import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "route_history",
    indices = [Index(value = ["startedAtMillis"])],
)
data class RouteHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val originLabel: String,
    val destLabel: String,
    val destPlaceId: String?,
    val destLatitude: Double,
    val destLongitude: Double,
    val viaLabel: String?,
    val viaPlaceId: String?,
    val viaLatitude: Double?,
    val viaLongitude: Double?,
    val plannedDurationSeconds: Long,
    val plannedDistanceMeters: Int,
    val encodedPolyline: String,
    val startedAtMillis: Long,
    val hasRouteStats: Boolean = true,
)
