package com.example.gpstracker.core.routes.di

import com.example.gpstracker.core.routes.RoutesMemoryCache
import com.example.gpstracker.core.routes.RoutesRepository
import com.example.gpstracker.core.routes.ValhallaRoutesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RoutesBindModule {
    @Binds
    @Singleton
    abstract fun bindRoutesRepository(impl: ValhallaRoutesRepository): RoutesRepository
}

@Module
@InstallIn(SingletonComponent::class)
object RoutesProvideModule {
    @Provides
    @Singleton
    fun provideRoutesJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    @Provides
    @Singleton
    fun provideRoutesMemoryCache(): RoutesMemoryCache = RoutesMemoryCache()
}
