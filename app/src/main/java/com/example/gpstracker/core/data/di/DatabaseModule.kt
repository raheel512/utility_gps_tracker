package com.example.gpstracker.core.data.di

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.example.gpstracker.core.data.local.AppDatabase
import com.example.gpstracker.core.data.local.CollectionDao
import com.example.gpstracker.core.data.local.MIGRATION_1_2
import com.example.gpstracker.core.data.local.MIGRATION_2_3
import com.example.gpstracker.core.data.local.MIGRATION_3_4
import com.example.gpstracker.core.data.local.MIGRATION_4_5
import com.example.gpstracker.core.data.local.RecentSearchDao
import com.example.gpstracker.core.data.local.RouteHistoryDao
import com.example.gpstracker.core.data.local.SavedPlaceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        DB_NAME,
    )
        .setDriver(AndroidSQLiteDriver())
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
        .build()

    @Provides
    fun provideRecentSearchDao(db: AppDatabase): RecentSearchDao = db.recentSearchDao()

    @Provides
    fun provideSavedPlaceDao(db: AppDatabase): SavedPlaceDao = db.savedPlaceDao()

    @Provides
    fun provideCollectionDao(db: AppDatabase): CollectionDao = db.collectionDao()

    @Provides
    fun provideRouteHistoryDao(db: AppDatabase): RouteHistoryDao = db.routeHistoryDao()

    const val DB_NAME = "gps_tracker.db"
}
