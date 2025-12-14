package com.example.rhythmmusic.controller

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.RemoteException
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.net.toUri
import com.example.rhythmmusic.logger.TimberLog
import com.example.rhythmmusic.model.QueueItem

class MediaControllerManager(
    private val context: Context,
    private val serviceComponent: ComponentName
) {
    private var mediaBrowser: MediaBrowserCompat? = null
    var mediaController: MediaControllerCompat? = null
        private set

    // Callback for external observers (ViewModel)
    var controllerCallbackListener: ((PlaybackStateCompat?, MediaMetadataCompat?) -> Unit)? = null

    private val connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            try {
                mediaBrowser?.sessionToken?.let { token ->
                    mediaController = MediaControllerCompat(context, token)
                    mediaController?.registerCallback(object : MediaControllerCompat.Callback() {
                        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                            controllerCallbackListener?.invoke(state, mediaController?.metadata)
                        }
                        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                            controllerCallbackListener?.invoke(mediaController?.playbackState, metadata)
                        }
                    })
                    // Notify ViewModel immediately
                    controllerCallbackListener?.invoke(mediaController?.playbackState, mediaController?.metadata)
                } ?: run {
                    Handler(Looper.getMainLooper()).postDelayed({ onConnected() }, 50)
                }
            } catch (e: RemoteException) {
                TimberLog.d("Failed to create MediaControllerCompat: ${e.message}")
            }
        }
    }

    fun init() {
        mediaBrowser = MediaBrowserCompat(context, serviceComponent, connectionCallback, null).apply { connect() }
    }

    fun play() = mediaController?.transportControls?.play()
    fun pause() = mediaController?.transportControls?.pause()
    fun skipNext() = mediaController?.transportControls?.skipToNext()
    fun skipPrevious() = mediaController?.transportControls?.skipToPrevious()
    fun playFromUri(item: QueueItem) {
        mediaController?.transportControls?.playFromUri(item.url.toUri(), Bundle().apply { putString("MEDIA_ID", item.mediaId) })
    }
}
