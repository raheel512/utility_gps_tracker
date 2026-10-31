package com.example.gpstracker.core.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [
        RecentSearchEntity::class,
        SavedPlaceEntity::class,
        CollectionEntity::class,
        RouteHistoryEntity::class,
    ],
    version = 5,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun savedPlaceDao(): SavedPlaceDao
    abstract fun collectionDao(): CollectionDao
    abstract fun routeHistoryDao(): RouteHistoryDao
}
