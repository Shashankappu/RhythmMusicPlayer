package com.example.rhythmmusic.service

import android.net.Uri
import androidx.media3.common.MediaItem
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media3.exoplayer.ExoPlayer
import com.example.rhythmmusic.logger.TimberLog
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class RhythmMusicPlaybackService : MediaBrowserServiceCompat() {
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var exoPlayer : ExoPlayer

    private val mediaSessionCallback =
        object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                super.onPlay()
                TimberLog.d("MediaSession onPlay")
            }

            override fun onPause() {
                super.onPause()
                TimberLog.d("MediaSession onPause")
            }

            override fun onSkipToNext() {
                super.onSkipToNext()
                TimberLog.d("MediaSession onSkipToNext")
            }
            override fun onSkipToPrevious() {
                super.onSkipToPrevious()
                TimberLog.d("MediaSession onSkipToPrevious")
            }

            override fun onStop() {
                super.onStop()
                TimberLog.d("MediaSession onStop")
            }

            override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
                Timber.d("onPlayFromUri called: $uri")

                if (uri == null) return

                val mediaItem = MediaItem.Builder()
                    .setUri(uri)
                    .setMediaId(extras?.getString("MEDIA_ID") ?: "")
                    .build()

                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.play()
            }
        }

    override fun onCreate() {
        super.onCreate()

        TimberLog.d("PlaybackService created")

        exoPlayer = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()

        mediaSession = MediaSessionCompat(this,"RhythmMusicPlaybackService").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )

            setCallback(mediaSessionCallback)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

    }



    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        Timber.i("onGetRoot called")
        return BrowserRoot("ROOT",null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem?>?>
    ) {
        TimberLog.d("onLoadChildren called: parentId= $parentId")
        result.sendResult(mutableListOf())
    }

    override fun onDestroy() {
        TimberLog.d("PlaybackService destroyed")
        exoPlayer.release()
        mediaSession.release()
        super.onDestroy()
    }

}