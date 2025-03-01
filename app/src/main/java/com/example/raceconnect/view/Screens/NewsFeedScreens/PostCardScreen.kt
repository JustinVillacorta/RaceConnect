package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.util.Log
import android.widget.ImageView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

@Composable
fun PostCard(
    post: NewsFeedDataClassItem,
    navController: NavController,
    onCommentClick: () -> Unit,
    onLikeClick: (Boolean) -> Unit,
    viewModel: NewsFeedViewModel // Pass ViewModel to fetch images

) {
    Log.d("PostCard", "📝 Rendering post with ID: ${post.id}, Content: ${post.content}")

    var isLiked by remember { mutableStateOf(post.isLiked) }
    var likeCount by remember { mutableStateOf(post.like_count) }

    // ✅ Get images only for this post
    val postImagesMap by viewModel.postImages.collectAsState()
    val imageUrls = postImagesMap[post.id] ?: emptyList()

    // ✅ Fetch images for this post
    LaunchedEffect(post.id) {
        Log.d("PostCard", "🔄 Fetching images for post ID: ${post.id}")
        viewModel.getPostImages(post.id)
    }

    Card(
        shape = RoundedCornerShape(0.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Profile Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                        .clickable {
                            navController.navigate("profile")
                            Log.d("PostCard", "📌 Navigating to profile")
                        }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Anonymous",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Just now",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Post Content
            Log.d("PostCard", "📝 Post Content: ${post.content}")
            Text(
                text = post.content ?: "No content available.",
                style = MaterialTheme.typography.bodyMedium
            )

            // **Display Images**
            if (imageUrls.isNotEmpty()) {
                Log.d("PostCard", "🖼️ Displaying Images: $imageUrls")

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(imageUrls) { imageUrl ->
                        Log.d("PostCard", "🖼️ Loading Image: $imageUrl")

                        AndroidView(
                            factory = { context ->
                                ImageView(context).apply {
                                    Picasso.get()
                                        .load(imageUrl)
                                        .into(this, object : Callback {
                                            override fun onSuccess() {
                                                Log.d("PostCard", "✅ Image loaded: $imageUrl")
                                            }

                                            override fun onError(e: Exception?) {
                                                Log.e("PostCard", "❌ Image load failed: $imageUrl", e)
                                            }
                                        })
                                }
                            },
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                    }
                }
            } else {
                Log.d("PostCard", "🚫 No images found for post ID: ${post.id}")
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Reaction Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReactionIcon(
                    icon = Icons.Default.Favorite,
                    isLiked = isLiked,
                    onClick = {
                        isLiked = !isLiked
                        likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                        onLikeClick(isLiked)
                        Log.d("PostCard", if (isLiked) "❤️ Post liked" else "💔 Post unliked")
                    }
                )

                Text(
                    text = likeCount.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp),
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.width(15.dp))
                ReactionIcon(icon = Icons.Default.ChatBubble, onClick = {
                    onCommentClick()
                    Log.d("PostCard", "💬 Comment button clicked")
                })
                Spacer(modifier = Modifier.width(12.dp))
                ReactionIcon(icon = Icons.Default.Repeat)
            }
        }
    }
}



@Composable
fun ReactionIcon(icon: ImageVector, isLiked: Boolean = false, onClick: (() -> Unit)? = null) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(20.dp)
            .clickable { onClick?.invoke() },
        tint = if (isLiked) Color.Red else Color.Gray
    )
}




//@Preview(showBackground = true)
//@Composable
//fun PreviewPostCard() {
//    val samplePost = NewsFeedDataClassItem(
//        id = 1,
//        user_id = 1,
//        title = "Sample Title",
//        content = "This is a sample post for preview.",
//        img_url = "https://via.placeholder.com/150", // Sample image placeholder
//        like_count = 0,
//        comment_count = 0,
//        repost_count = 0,
//        category = "Formula 1",
//        privacy = "Public",
//        type = "text",
//        post_type = "normal",
//        created_at = "",
//        updated_at = ""
//    )
//
//    val navController = rememberNavController()
//
//    PostCard(post = samplePost, navController = navController, onCommentClick = {})
//}