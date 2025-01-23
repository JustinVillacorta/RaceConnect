package com.example.raceconnect.ui

import android.media.browse.MediaBrowser
import android.net.Uri
import android.os.Looper.prepare
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.raceconnect.Model.Reels

import com.example.raceconnect.R

@Composable
fun ReelsScreen() {
    val sampleReels = listOf(
        Reels(
            user = "Takeru Sanada",
            profilePic = R.drawable.baseline_account_circle_24,
            videoUrl = "https://youtu.be/iJcLBh_2qM4?si=MmYZCcLXF8TL_RuZ",
            likes = 129,
            comments = 59,
            shares = 10_000
        ),

        Reels(
            user = "Takeru Sanada",
            profilePic = R.drawable.baseline_account_circle_24,
            videoUrl = "https://youtu.be/iJcLBh_2qM4?si=MmYZCcLXF8TL_RuZ",
            likes = 129,
            comments = 59,
            shares = 10_000
        ),

        Reels(
            user = "Takeru Sanada",
            profilePic = R.drawable.baseline_account_circle_24,
            videoUrl = "https://youtu.be/iJcLBh_2qM4?si=MmYZCcLXF8TL_RuZ",
            likes = 129,
            comments = 59,
            shares = 10_000
        ),

        Reels(
            user = "Takeru Sanada",
            profilePic = R.drawable.baseline_account_circle_24,
            videoUrl = "https://youtu.be/iJcLBh_2qM4?si=MmYZCcLXF8TL_RuZ",
            likes = 129,
            comments = 59,
            shares = 10_000
        ),


        Reels(
            user = "Takeru Sanada",
            profilePic = R.drawable.baseline_account_circle_24,
            videoUrl = "https://youtu.be/iJcLBh_2qM4?si=MmYZCcLXF8TL_RuZ",
            likes = 129,
            comments = 59,
            shares = 10_000
        ),
        Reels(
            user = "John Doe",
            profilePic = R.drawable.baseline_account_circle_24,
            videoUrl = "https://sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4",
            likes = 200,
            comments = 75,
            shares = 5_000
        )


    )

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(sampleReels) { reel ->
            ReelItem(reel = reel)
        }
    }
}


@Composable
fun ReelItem(reel: Reels) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.Builder()
                .setUri(Uri.parse(reel.videoUrl))
                .build()
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // Video Player
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    useController = false // Hide default controls
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay UI
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar (Optional)
            Text(
                text = "RaceConnect",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                modifier = Modifier.padding(8.dp)
            )

            // Bottom Content
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // User Info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = reel.profilePic),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = reel.user,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Easy steps on how to film your car",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp
                        )
                    }
                }

                // Action Buttons
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { /* Handle Like */ }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Like", tint = Color.Red)
                    }
                    Text(text = "${reel.likes}")

                    // Use a valid icon for Comment
                    IconButton(onClick = { /* Handle Comment */ }) {
                        Icon(painter = painterResource(id = R.drawable.baseline_mode_comment_24), contentDescription = "Comment", tint = Color.Gray)
                    }
                    Text(text = "${reel.comments}")

                    IconButton(onClick = { /* Handle Share */ }) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Gray)
                    }
                    Text(text = "${reel.shares}")
                }
            }
        }
    }
}
