package com.example.gpstracker.core.data.local

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(
    tableName = "saved_places",
    foreignKeys = [
        ForeignKey(
            entity = CollectionEntity::class,
            parentColumns = ["id"],
            childColumns = ["collectionId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [
        Index(value = ["placeId"], unique = false),
        Index(value = ["collectionId"]),
        Index(value = ["latKey", "lngKey"]),
    ],
)
data class SavedPlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val placeId: String?,
    val name: String,
    val address: String?,
    val latitude: Double,
    val longitude: Double,
    /** Latitude rounded to 5 decimals × 1e5 for dedupe when placeId is null. */
    val latKey: Long,
    /** Longitude rounded to 5 decimals × 1e5 for dedupe when placeId is null. */
    val lngKey: Long,
    val collectionId: Long?,
    val createdAtMillis: Long,
    val iconKind: String = IconKind.PLACE,
) {
    object IconKind {
        const val HOME = "HOME"
        const val WORK = "WORK"
        const val PLACE = "PLACE"
    }
}

fun coordinateKey(value: Double): Long = Math.round(value * 100_000.0)
