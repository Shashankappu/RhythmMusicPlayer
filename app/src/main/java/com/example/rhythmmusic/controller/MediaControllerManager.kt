package com.example.rhythmmusic.controller

import android.content.ComponentName
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.example.rhythmmusic.logger.TimberLog
import com.example.rhythmmusic.service.RhythmMusicPlaybackService
import timber.log.Timber

class MediaControllerManager(
    private val context: Context,
    private val serviceComponent: ComponentName
) {
    private var mediaBrowser : MediaBrowserCompat? = null
    var mediaController : MediaControllerCompat? = null
        private set

    private var currentSource: String? = null

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null

    fun init() {
        mediaBrowser = MediaBrowserCompat(
            context,
            ComponentName(context, RhythmMusicPlaybackService::class.java),
            connectionCallback,
            null
        )
        mediaBrowser?.connect()
    }

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            super.onConnected()
            TimberLog.d("MediaBrowser onConnected")
            try {
                mediaBrowser?.sessionToken?.let { token ->
                    mediaController = MediaControllerCompat(context, token)
                    mediaController?.registerCallback(controllerCallback)
                } ?: run {
                    // Token not ready yet — retry shortly
                    Timber.w("SessionToken is null, retrying connection...")
                    Handler(Looper.getMainLooper()).postDelayed({ onConnected() }, 50)
                }
            } catch (e: RemoteException){
                TimberLog.d("Failed to create MediaControllerCompat" +  e.message.toString())
            }
        }

        override fun onConnectionSuspended() {
            super.onConnectionSuspended()
        }

        override fun onConnectionFailed() {
            super.onConnectionFailed()
        }
    }

    private val controllerCallback = object : MediaControllerCompat.Callback() {
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            TimberLog.d("controllerCallback onPlaybackStateChanged")
        }

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            TimberLog.d("controllerCallback onMetadataChanged")
        }
    }

    fun requestAudioFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setOnAudioFocusChangeListener { focusChange -> handleAudioFocusChange(focusChange) }
                .build()
            audioManager.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                { focusChange -> handleAudioFocusChange(focusChange) },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun handleAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> mediaController?.transportControls?.play()
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> mediaController?.transportControls?.pause()
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> mediaController?.setVolumeTo(10, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
        }
    }


    fun connect() {
        TimberLog.i("Connecting MediaBrowser")
        mediaBrowser = MediaBrowserCompat(
            context,
            serviceComponent,
            connectionCallback,
            null
        ).apply {
            connect()
        }
    }

    fun disconnect() {
        TimberLog.i("Disconnecting MediaBrowser")
        mediaController?.unregisterCallback(controllerCallback)
        mediaBrowser?.disconnect()
    }

    fun play() {
        if (requestAudioFocus()) {
            mediaController?.transportControls?.play()
        }
    }
    fun pause() = mediaController?.transportControls?.pause()
    fun skipNext() = mediaController?.transportControls?.skipToNext()
    fun skipPrevious() = mediaController?.transportControls?.skipToPrevious()

    fun playSource(source: String) {
        if (currentSource != source) {
            mediaController?.transportControls?.stop()
            currentSource = source
        }
        play()
    }
}