package com.example.gpstracker.feature.camera.capture

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CameraCaptureModule {
    @Binds
    @Singleton
    abstract fun bindLatestPhotoQuery(impl: MediaStorePhotoSaver): LatestPhotoQuery
}
