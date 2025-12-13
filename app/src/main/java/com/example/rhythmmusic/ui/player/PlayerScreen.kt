import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rhythmmusic.model.QueueItem
import com.example.rhythmmusic.viewmodel.PlaybackViewModel

@Composable
fun PlayerScreen(viewModel: PlaybackViewModel = hiltViewModel()) {

    // Collect state from ViewModel
    val playbackState by viewModel.playbackState.collectAsState(initial = null)
    val metadata by viewModel.metadata.collectAsState(initial = null)
    val playlist by viewModel.playlist.collectAsState()

    Playlist(
        playlist = playlist,
        onItemClick = viewModel::playFromPlaylist
    )
    // UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Track title
        Text(
            text = metadata?.description?.title?.toString() ?: "No Track",
            style = MaterialTheme.typography.bodyMedium
        )

        // Artist
        Text(
            text = metadata?.description?.subtitle?.toString() ?: "",
            style = MaterialTheme.typography.bodySmall
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Playback controls
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.skipPrevious() }) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous")
            }

            if (playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                IconButton(onClick = { viewModel.pause() }) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Pause")
                }
            } else {
                IconButton(onClick = { viewModel.play() }) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                }
            }

            IconButton(onClick = { viewModel.skipNext() }) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next")
            }
        }
    }
}

@Composable
fun Playlist(
    playlist: List<QueueItem>,
    onItemClick: (Int) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {
        itemsIndexed(playlist) { index, item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(index) }
                    .padding(16.dp)
            ) {
                Column {
                    Text(item.title)
                    Text(item.artist, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
