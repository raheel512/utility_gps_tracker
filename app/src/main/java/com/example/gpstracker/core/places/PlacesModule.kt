package com.example.gpstracker.core.places

import com.example.gpstracker.core.geo.OsmPlacesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlacesBindModule {
    @Binds
    @Singleton
    abstract fun bindPlacesRepository(impl: OsmPlacesRepository): PlacesRepository
}

@Module
@InstallIn(SingletonComponent::class)
object PlacesProvideModule {
    @Provides
    @Singleton
    fun providePlacesMemoryCache(): PlacesMemoryCache = PlacesMemoryCache()
}

@Module
@InstallIn(ViewModelComponent::class)
object PlacesViewModelModule {
    @Provides
    fun provideAutocompleteSessionManager(
        placesRepository: PlacesRepository,
    ): AutocompleteSessionManager = DefaultAutocompleteSessionManager(placesRepository)
}
