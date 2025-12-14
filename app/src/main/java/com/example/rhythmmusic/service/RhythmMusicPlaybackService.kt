package com.example.rhythmmusic.service

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.rhythmmusic.logger.TimberLog
import com.example.rhythmmusic.model.QueueItem
import com.example.rhythmmusic.notification.PlaybackNotificationManager
import com.example.rhythmmusic.notification.PlaybackNotificationManager.Companion.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.map

@AndroidEntryPoint
class RhythmMusicPlaybackService : MediaBrowserServiceCompat() {

    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var exoPlayer: ExoPlayer
    private lateinit var notificationManager: PlaybackNotificationManager

    private val queue = mutableListOf<MediaItem>()
    private var currentIndex = 0


    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {

        override fun onPlay() {
            TimberLog.d("MediaSession onPlay")
            startForeground(
                NOTIFICATION_ID,
                notificationManager.buildNotification()
            )
            exoPlayer.playWhenReady = true
        }

        override fun onPause() {
            TimberLog.d("MediaSession onPause")
            exoPlayer.playWhenReady = false
            stopForeground(false)
        }

        override fun onStop() {
            TimberLog.d("MediaSession onStop")
            exoPlayer.stop()
            stopForeground(true)
        }

        override fun onPlayFromUri(uri: Uri?, extras: Bundle?) {
            uri ?: return
            TimberLog.d("MediaSession onPlayFromUri -> $uri")
            val item = MediaItem.fromUri(uri)
            if (!queue.contains(item)) {
                queue.add(item)
                currentIndex = queue.size - 1
            } else {
                currentIndex = queue.indexOf(item)
            }
            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer.prepare()
            onPlay()
        }

        override fun onCustomAction(action: String, extras: Bundle?) {
            TimberLog.d("MediaSession onCustomAction -> $action")
            if (action == "SET_QUEUE" && extras != null) {
                val items = extras.getParcelableArrayList("QUEUE", Bundle::class.java)?.map { QueueItem.fromBundle(it) } ?: emptyList()
                val startIndex = extras.getInt("INDEX", 0)
                setQueue(items, startIndex)
            }
        }

        override fun onSkipToNext() {
            TimberLog.d("MediaSession onSkipToNext")
            if (currentIndex < queue.lastIndex) {
                currentIndex++
                playCurrent()
            }
        }

        override fun onSkipToPrevious() {
            TimberLog.d("MediaSession onSkipToPrevious")
            if (currentIndex > 0) {
                currentIndex--
                playCurrent()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        TimberLog.d("PlaybackService created")

        exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                androidx.media3.common.AudioAttributes.Builder()
                    .setUsage(androidx.media3.common.C.USAGE_MEDIA)
                    .setContentType(androidx.media3.common.C.AUDIO_CONTENT_TYPE_MUSIC)
                    .build(),
                true
            )
            .setHandleAudioBecomingNoisy(true)
            .build()

        exoPlayer.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateCompat = when (playbackState) {
                    Player.STATE_READY -> if (exoPlayer.playWhenReady) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED
                    Player.STATE_BUFFERING -> PlaybackStateCompat.STATE_BUFFERING
                    Player.STATE_ENDED -> PlaybackStateCompat.STATE_STOPPED
                    else -> PlaybackStateCompat.STATE_NONE
                }

                mediaSession.setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(stateCompat, exoPlayer.currentPosition, 1f)
                        .setActions(
                            PlaybackStateCompat.ACTION_PLAY or
                                    PlaybackStateCompat.ACTION_PAUSE or
                                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                        .build()
                )
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                TimberLog.d("onMediaItemTransition -> mediaItem = $mediaItem")
                mediaItem?.let {
                    val metadataCompat = MediaMetadataCompat.Builder()
                        .putString(
                            MediaMetadataCompat.METADATA_KEY_TITLE,
                            it.mediaMetadata.title?.toString()
                        )
                        .putString(
                            MediaMetadataCompat.METADATA_KEY_ARTIST,
                            it.mediaMetadata.artist?.toString()
                        )
                        .build()

                    mediaSession.setMetadata(metadataCompat)
                }
            }
        })


        mediaSession = MediaSessionCompat(this, "RhythmMusicPlaybackService").apply {
            setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            setCallback(mediaSessionCallback)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken
        notificationManager = PlaybackNotificationManager(applicationContext)
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot = BrowserRoot("ROOT", null)

    override fun onLoadChildren(
        parentId: String,
        result: Result<List<MediaBrowserCompat.MediaItem?>?>
    ) {
        result.sendResult(emptyList())
    }

    override fun onDestroy() {
        exoPlayer.release()
        mediaSession.release()
        super.onDestroy()
    }


    fun setQueue(items: List<QueueItem>, startIndex: Int = 0) {
        queue.clear()

        queue.addAll(
            items.map { item ->
                MediaItem.Builder()
                    .setMediaId(item.mediaId)
                    .setUri(item.url)
                    .setMediaMetadata(
                        androidx.media3.common.MediaMetadata.Builder()
                            .setTitle(item.title)
                            .setArtist(item.artist)
                            .build()
                    )
                    .build()
            }
        )

        currentIndex = startIndex.coerceIn(queue.indices)

        exoPlayer.setMediaItems(queue, currentIndex, 0L)
        exoPlayer.prepare()
    }

    private fun playCurrent() {
        val item = queue.getOrNull(currentIndex) ?: return
        val uri = item.localConfiguration?.uri
        uri?.let {
            mediaSessionCallback.onPlayFromUri(it, Bundle().apply {
                    putString("MEDIA_ID", item.mediaId)
                    putString("TITLE", item.mediaMetadata.title?.toString())
                    putString("ARTIST", item.mediaMetadata.artist?.toString())
            })
        }
    }
}
