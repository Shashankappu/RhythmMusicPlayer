package com.example.rhythmmusic.di

import android.content.ComponentName
import android.content.Context
import com.example.rhythmmusic.controller.MediaControllerManager
import com.example.rhythmmusic.service.RhythmMusicPlaybackService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaControllerModule {

    @Provides
    @Singleton
    fun provideComponentName(@ApplicationContext context: Context): ComponentName {
        return ComponentName(context, RhythmMusicPlaybackService::class.java)
    }

    @Provides
    @Singleton
    fun provideMediaControllerManager(
        @ApplicationContext context: Context,
        serviceComponent: ComponentName
    ): MediaControllerManager {
        return MediaControllerManager(context, serviceComponent)
    }
}