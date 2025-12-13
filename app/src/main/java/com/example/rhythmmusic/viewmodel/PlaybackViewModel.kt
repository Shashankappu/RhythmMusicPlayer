package com.example.rhythmmusic.viewmodel

import android.net.Uri
import android.os.Bundle
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.ViewModel
import com.example.rhythmmusic.controller.MediaControllerManager
import com.example.rhythmmusic.logger.TimberLog
import com.example.rhythmmusic.model.PlaybackState
import com.example.rhythmmusic.model.QueueItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.core.net.toUri

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    val mediaControllerManager: MediaControllerManager
): ViewModel() {

    private val _playbackState = MutableStateFlow<PlaybackStateCompat?>(null)
    val playbackState : StateFlow<PlaybackStateCompat?> = _playbackState.asStateFlow()

    private val _metadata = MutableStateFlow<MediaMetadataCompat?>(null)
    val metadata : StateFlow<MediaMetadataCompat?> = _metadata.asStateFlow()

    private val _playlist = MutableStateFlow<List<QueueItem>>(emptyList())
    val playlist: StateFlow<List<QueueItem>> = _playlist.asStateFlow()

    init {
        mediaControllerManager.init()
        observeController()
        loadMockPlaylist()
    }

    private fun observeController(){
        mediaControllerManager.mediaController?.registerCallback(object : MediaControllerCompat.Callback(){
            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                _playbackState.value = state
                TimberLog.d("ViewModel observed state : ${state?.state}")
            }

            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                _metadata.value = metadata
                TimberLog.d("ViewModel observed metadata : ${metadata?.description?.title}")


            }
        })
    }

    fun play(){
        mediaControllerManager.play()
    }
    fun pause(){
        mediaControllerManager.pause()
    }

    fun skipNext(){
        mediaControllerManager.skipNext()
    }

    fun skipPrevious(){
        mediaControllerManager.skipPrevious()
    }

    fun playSource(source: String) = mediaControllerManager.playSource(source)


    fun loadMockPlaylist() {
        _playlist.value = listOf(
            QueueItem(
                mediaId = "1",
                title = "Sample Track 1",
                artist = "Mock Artist",
                url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
            ),
            QueueItem(
                mediaId = "2",
                title = "Sample Track 2",
                artist = "Mock Artist",
                url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3"
            )
        )
    }

    fun playFromPlaylist(index: Int) {
        val item = _playlist.value.getOrNull(index) ?: return

        mediaControllerManager.mediaController?.transportControls?.playFromUri(
            item.url.toUri(),
            Bundle().apply {
                putString("MEDIA_ID", item.mediaId)
            }
        )
    }
}