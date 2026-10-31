package com.example.gpstracker.core.speech.di

import com.example.gpstracker.core.speech.AndroidSpeechRecognizerClient
import com.example.gpstracker.core.speech.SpeechRecognizerClient
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SpeechModule {
    @Binds
    @Singleton
    abstract fun bindSpeechRecognizerClient(
        impl: AndroidSpeechRecognizerClient,
    ): SpeechRecognizerClient
}
