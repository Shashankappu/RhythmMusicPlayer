package com.example.rhythmmusic

import PlayerScreen
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.rhythmmusic.controller.MediaControllerManager
import com.example.rhythmmusic.logger.TimberLog
import com.example.rhythmmusic.ui.theme.RhythmMusicTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var mediaControllerManager: MediaControllerManager

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TimberLog.d("starting service")
//        mediaControllerManager.init()
        enableEdgeToEdge()

        TimberLog.d("started service")
        setContent {
            RhythmMusicTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    PlayerScreen()
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RhythmMusicTheme {
        Greeting("Android")
    }
}