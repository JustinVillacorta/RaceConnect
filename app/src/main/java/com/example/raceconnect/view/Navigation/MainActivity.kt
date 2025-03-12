package com.example.raceconnect.view.navigation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.navigation.AppNavigation
import com.example.raceconnect.view.ui.theme.RaceConnectTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RaceConnectTheme(darkTheme = false) { // Force light theme
                Surface(modifier = Modifier.fillMaxSize()) {
                    SplashScreen(userPreferences = UserPreferences(applicationContext))
                }
            }
        }
    }
}

@OptIn(UnstableApi::class)
@Composable
fun SplashScreen(userPreferences: UserPreferences) {
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(true) }

    // Initialize ExoPlayer with error handling
    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            try {
                val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${R.raw.splashscreen}")
                Log.d("SplashScreen", "Loading media item: $mediaItem")
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        Log.d("SplashScreen", "Playback state changed to: $playbackState")
                        if (playbackState == Player.STATE_ENDED) {
                            isLoading = false
                        }
                    }
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        Log.e("SplashScreen", "Player error: ${error.message}", error)
                        isLoading = false // Fallback to transition on error
                    }
                })
            } catch (e: Exception) {
                Log.e("SplashScreen", "Error initializing player: ${e.message}", e)
                isLoading = false // Fallback to transition on exception
            }
        }
    }

    // Fallback delay to ensure transition after 5 seconds
    LaunchedEffect(Unit) {
        delay(4000) // 5 seconds max duration
        Log.d("SplashScreen", "Timeout reached, transitioning to AppNavigation")
        isLoading = false
    }

    // Show video player while loading, then transition to AppNavigation
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    PlayerView(context).apply {
                        this.player = player
                        layoutParams = android.view.ViewGroup.LayoutParams(
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                            android.view.ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        // Disable media controls
                        useController = false
                        // Set resize mode to fill the screen while maintaining aspect ratio
                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        // Alternative: Use RESIZE_MODE_FILL to stretch (may distort) or RESIZE_MODE_ZOOM to crop edges
                    }
                }
            )
        }
    } else {
        RaceConnectTheme(darkTheme = false) { // Ensure light theme for AppNavigation
            AppNavigation(userPreferences)
        }
        LaunchedEffect(Unit) {
            Log.d("SplashScreen", "Releasing player")
            player.release()
        }
    }
}