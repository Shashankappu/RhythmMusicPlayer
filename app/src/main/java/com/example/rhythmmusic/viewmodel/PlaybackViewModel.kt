package com.example.rhythmmusic.viewmodel

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.lifecycle.ViewModel
import com.example.rhythmmusic.controller.MediaControllerManager
import com.example.rhythmmusic.logger.TimberLog
import com.example.rhythmmusic.model.QueueItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    val mediaControllerManager: MediaControllerManager
): ViewModel() {

    private val _playbackState = MutableStateFlow<PlaybackStateCompat?>(null)
    val playbackState: StateFlow<PlaybackStateCompat?> = _playbackState.asStateFlow()

    private val _metadata = MutableStateFlow<MediaMetadataCompat?>(null)
    val metadata: StateFlow<MediaMetadataCompat?> = _metadata.asStateFlow()

    private val _playlist = MutableStateFlow<List<QueueItem>>(emptyList())
    val playlist: StateFlow<List<QueueItem>> = _playlist.asStateFlow()

    init {
        mediaControllerManager.init()

        // Observe playback state and metadata updates
        mediaControllerManager.controllerCallbackListener = { state, metadata ->
            _playbackState.value = state
            _metadata.value = metadata
        }

        loadMockPlaylist()
    }


    fun play() = mediaControllerManager.play()
    fun pause() = mediaControllerManager.pause()
    fun skipNext() = mediaControllerManager.skipNext()
    fun skipPrevious() = mediaControllerManager.skipPrevious()

    fun playFromPlaylist(index: Int) {
        val item = _playlist.value[index]
        mediaControllerManager.playFromUri(item)
    }

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
            ),
            QueueItem(
                mediaId = "3",
                title = "Sample Track 3",
                artist = "Mock Artist",
                url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3"
            ),
            QueueItem(
                mediaId = "4",
                title = "Sample Track 4",
                artist = "Mock Artist",
                url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
            ),
            QueueItem(
                mediaId = "5",
                title = "Sample Track 5",
                artist = "Mock Artist",
                url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
            ),
            QueueItem(
                mediaId = "6",
                title = "Sample Track 6",
                artist = "Mock Artist",
                url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"
            ),
            QueueItem(
                mediaId = "7",
                title = "Sample Track 7",
                artist = "Mock Artist",
                url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-7.mp3"
            )
        )
    }
}
