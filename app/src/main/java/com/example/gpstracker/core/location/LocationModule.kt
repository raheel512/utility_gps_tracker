package com.example.gpstracker.core.location

import android.content.Context
import com.example.gpstracker.core.location.geocode.PhotonReverseGeocoder
import com.example.gpstracker.core.location.geocode.ReverseGeocoder
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LocationBindingsModule {
    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: FusedLocationRepository,
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindDeviceLocationSettings(
        impl: DeviceLocationSettingsImpl,
    ): DeviceLocationSettings

    @Binds
    @Singleton
    abstract fun bindReverseGeocoder(
        impl: PhotonReverseGeocoder,
    ): ReverseGeocoder
}

@Module
@InstallIn(SingletonComponent::class)
object LocationProvidesModule {
    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context,
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
}
