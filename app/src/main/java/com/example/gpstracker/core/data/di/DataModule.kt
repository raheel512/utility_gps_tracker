package com.example.gpstracker.core.data.di

import com.example.gpstracker.core.data.SettingsRepository
import com.example.gpstracker.core.data.connectivity.ConnectivityManagerNetworkMonitor
import com.example.gpstracker.core.data.connectivity.ConnectivityRepository
import com.example.gpstracker.core.data.datastore.SettingsDataStore
import com.example.gpstracker.core.data.history.RoomRouteHistoryRepository
import com.example.gpstracker.core.data.history.RouteHistoryRepository
import com.example.gpstracker.core.data.saved.RoomSavedPlacesRepository
import com.example.gpstracker.core.data.saved.SavedPlacesRepository
import com.example.gpstracker.core.places.RecentSearchRepository
import com.example.gpstracker.core.places.RoomRecentSearchRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsDataStore,
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindConnectivityRepository(
        impl: ConnectivityManagerNetworkMonitor,
    ): ConnectivityRepository

    @Binds
    @Singleton
    abstract fun bindRecentSearchRepository(
        impl: RoomRecentSearchRepository,
    ): RecentSearchRepository

    @Binds
    @Singleton
    abstract fun bindSavedPlacesRepository(
        impl: RoomSavedPlacesRepository,
    ): SavedPlacesRepository

    @Binds
    @Singleton
    abstract fun bindRouteHistoryRepository(
        impl: RoomRouteHistoryRepository,
    ): RouteHistoryRepository
}
