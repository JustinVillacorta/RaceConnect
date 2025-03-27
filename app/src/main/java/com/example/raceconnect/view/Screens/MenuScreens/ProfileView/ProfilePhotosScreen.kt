package com.example.raceconnect.view.Screens.MenuScreens.ProfileView

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun PhotosSection(
    postImages: Map<Int, List<String>>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        postImages.forEach { (postId, images) ->
            images.forEach { imageUrl ->
                Log.d("PhotosSection", "Loading photo for postId: $postId, URL: $imageUrl")
                item {
                    val painter = rememberAsyncImagePainter(
                        model = imageUrl,
                        onLoading = { Log.d("PhotosSection", "Loading photo image...") },
                        onSuccess = { Log.d("PhotosSection", "Photo image loaded successfully") },
                        onError = { error ->
                            Log.e("PhotosSection", "Error loading photo image: ${error.result.throwable.message}")
                        }
                    )
                    Image(
                        painter = painter,
                        contentDescription = "User Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}