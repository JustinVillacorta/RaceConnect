package com.example.raceconnect.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.viewmodel.NotificationClickedViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    postId: Int,
    repostId: Int? = null,
    userPreferences: UserPreferences,
    viewModel: NotificationClickedViewModel = viewModel(),
    onClose: () -> Unit = {}
) {
    val repost by viewModel.repost.collectAsState()
    val originalPost by viewModel.originalPost.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val likeCount by viewModel.likeCount.collectAsState()
    val isRepostLiked by viewModel.isRepostLiked.collectAsState()
    val repostLikeCount by viewModel.repostLikeCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val tokenState by userPreferences.token.collectAsState(initial = null)

    // Use a stable key to prevent duplicate fetches
    LaunchedEffect(key1 = postId, key2 = repostId, key3 = tokenState) {
        Log.d("PostDetailScreen", "LaunchedEffect triggered with postId: $postId, repostId: $repostId, tokenState: $tokenState")
        if (tokenState != null) {
            viewModel.setAuthToken(tokenState!!)
            viewModel.clearPost()
            viewModel.fetchPost(postId, repostId)
            viewModel.fetchComments(postId)
            viewModel.fetchPostLikes(postId)
            if (repostId != null) {
                viewModel.fetchRepostLikes(repostId)
            }
        } else {
            viewModel._error.value = "Authentication token is missing"
            Log.w("PostDetailScreen", "Token missing")
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Post Detail", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Red)
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                    Log.d("PostDetailScreen", "Displaying loading state")
                }
            }
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = error ?: "Unknown error", color = Color.Red)
                    Log.e("PostDetailScreen", "Error: $error")
                }
            }
            repost == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Post not found", color = Color.Red)
                    Log.w("PostDetailScreen", "Repost is null")
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        // Render as a repost if repostId is provided or if repost.isRepost is true
                        if (repostId != null || (repost?.isRepost == true && repost?.original_post_id != null && originalPost != null)) {
                            RepostLayout(
                                repost = repost!!,
                                originalPost = if (repostId != null) repost!! else originalPost!!,
                                isLiked = isLiked,
                                likeCount = likeCount,
                                commentsCount = comments.size,
                                isRepostLiked = isRepostLiked,
                                repostLikeCount = repostLikeCount,
                                onLikeClick = {
                                    if (repostId != null) {
                                        viewModel.toggleLike(postId)
                                    } else {
                                        viewModel.toggleLike(repost!!.original_post_id!!)
                                    }
                                },
                                onRepostLikeClick = {
                                    if (repostId != null) {
                                        viewModel.toggleRepostLike(repostId)
                                    } else {
                                        viewModel.toggleRepostLike(repost!!.id)
                                    }
                                }
                            )
                        } else {
                            PostLayout(
                                post = repost!!,
                                isLiked = isLiked,
                                likeCount = likeCount,
                                commentsCount = comments.size,
                                onLikeClick = { viewModel.toggleLike(repost!!.id) }
                            )
                        }

                        Text(
                            text = "Comments",
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }

                    items(comments) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.Gray)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountCircle,
                                    contentDescription = "Commenter Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = comment.username ?: "Anonymous",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = comment.comment ?: "No content",
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = comment.createdAt?.let { formatTimestamp(it) } ?: "Just now",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RepostLayout(
    repost: NewsFeedDataClassItem,
    originalPost: NewsFeedDataClassItem,
    isLiked: Boolean,
    likeCount: Int,
    commentsCount: Int,
    isRepostLiked: Boolean,
    repostLikeCount: Int,
    onLikeClick: () -> Unit,
    onRepostLikeClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Repost Header with Quote
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repost.profile_picture?.let { url ->
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = imageRequest(url),
                            onError = { Log.e("RepostLayout", "Failed to load profile picture: ${it.result.throwable?.message}") }
                        ),
                        contentDescription = "Repost Profile",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Repost Profile",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = repost.quote ?: "${repost.username ?: "Anonymous"} reposted",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = repost.created_at?.let { formatTimestamp(it) } ?: "Mar 9, 2025 at 08:35 AM",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Reaction Bar for Repost
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onRepostLikeClick
                ) {
                    Icon(
                        imageVector = if (isRepostLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Repost Like",
                        tint = if (isRepostLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$repostLikeCount",
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = "Comment",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "0", // Repost comments not implemented
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repost",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "0", // Nested reposts not implemented
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }

            // Encased Original Post
            Card(
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        originalPost.profile_picture?.let { url ->
                            Image(
                                painter = rememberAsyncImagePainter(
                                    model = imageRequest(url),
                                    onError = { Log.e("RepostLayout", "Failed to load original profile picture: ${it.result.throwable?.message}") }
                                ),
                                contentDescription = "Original User Profile",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.Gray)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Original User Profile",
                                modifier = Modifier.fillMaxSize(),
                                tint = Color.Black
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = originalPost.username ?: "Anonymous",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                fontSize = 16.sp
                            )
                            Text(
                                text = originalPost.created_at?.let { formatTimestamp(it) } ?: "2025-03-09 08:35:13",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = originalPost.content ?: "This is the original post content.",
                        color = Color.Black,
                        fontSize = 14.sp
                    )

                    originalPost.images?.firstOrNull()?.let { imageUrl ->
                        val painter = rememberAsyncImagePainter(
                            model = imageRequest(imageUrl),
                            onLoading = { Log.d("PostDetailScreen", "Loading image...") },
                            onSuccess = { Log.d("PostDetailScreen", "Image loaded successfully") },
                            onError = { error ->
                                Log.e("PostDetailScreen", "Error loading image: ${error.result.throwable?.message}")
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Image(
                            painter = painter,
                            contentDescription = "Repost Image",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reaction Bar for Original Post
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onLikeClick
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) Color.Red else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$likeCount",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Comment,
                            contentDescription = "Comment",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$commentsCount",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Icon(
                            imageVector = Icons.Default.Repeat,
                            contentDescription = "Repost",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${originalPost.repost_count}",
                            color = Color.Black,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostLayout(
    post: NewsFeedDataClassItem,
    isLiked: Boolean,
    likeCount: Int,
    commentsCount: Int,
    onLikeClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                post.profile_picture?.let { url ->
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = imageRequest(url),
                            onError = { Log.e("PostLayout", "Failed to load profile picture: ${it.result.throwable?.message}") }
                        ),
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = post.username ?: "Anonymous",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = post.created_at?.let { formatTimestamp(it) } ?: "Mar 9, 2025 at 08:35 AM",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = post.content ?: "This is a sample post content.",
                color = Color.Black,
                fontSize = 14.sp
            )

            post.images?.firstOrNull()?.let { imageUrl ->
                val painter = rememberAsyncImagePainter(
                    model = imageRequest(imageUrl),
                    onLoading = { Log.d("PostDetailScreen", "Loading image...") },
                    onSuccess = { Log.d("PostDetailScreen", "Image loaded successfully") },
                    onError = { error ->
                        Log.e("PostDetailScreen", "Error loading image: ${error.result.throwable?.message}")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = painter,
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onLikeClick
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$likeCount",
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = "Comment",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$commentsCount",
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repost",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${post.repost_count}",
                    color = Color.Black,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun imageRequest(url: String): ImageRequest {
    val context = LocalContext.current
    return ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        .build()
}

private fun formatTimestamp(dateStr: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = sdf.parse(dateStr) ?: return dateStr
    val diff = Date().time - date.time
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        else -> SimpleDateFormat("MMM d, yyyy 'at' hh:mm a", Locale.getDefault()).format(date)
    }
}