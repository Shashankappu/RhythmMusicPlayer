package com.example.rhythmmusic

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class RhythmMusicApp : Application(){
    override fun onCreate() {
        super.onCreate()
            Timber.plant(Timber.DebugTree())

    }
}