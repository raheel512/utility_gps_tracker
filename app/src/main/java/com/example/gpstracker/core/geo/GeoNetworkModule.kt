package com.example.gpstracker.core.geo

import com.example.gpstracker.core.map.MapServicesConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GeoNetworkModule {
    @Provides
    @Singleton
    @GeoOkHttp
    fun provideGeoOkHttp(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(MapServicesConfig.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
        .readTimeout(MapServicesConfig.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
        .callTimeout(35, TimeUnit.SECONDS)
        .build()
}
