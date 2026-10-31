package com.example.gpstracker.core.sensors

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SensorsModule {
    @Binds
    @Singleton
    abstract fun bindHeadingProvider(impl: SensorHeadingProvider): HeadingProvider

    @Binds
    @Singleton
    abstract fun bindPressureProvider(impl: SensorPressureProvider): PressureProvider

    @Binds
    @Singleton
    abstract fun bindGnssStatusRepository(
        impl: LocationManagerGnssStatusRepository,
    ): GnssStatusRepository
}
