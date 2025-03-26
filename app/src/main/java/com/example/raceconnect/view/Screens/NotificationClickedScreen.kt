package com.example.raceconnect.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.raceconnect.model.PostByIdResponse
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.NotificationClickedViewModel
import android.util.Log
import androidx.compose.foundation.BorderStroke
import java.text.SimpleDateFormat
import java.util.*
import com.example.raceconnect.model.Repost

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
    val repostData by viewModel.repostData.collectAsState() // Collect repostData
    val comments by viewModel.comments.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val likeCount by viewModel.likeCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val tokenState by userPreferences.token.collectAsState(initial = null)

    // Log the received postId and repostId to debug navigation
    LaunchedEffect(Unit) {
        Log.d("PostDetailScreen", "Received postId: $postId, repostId: $repostId")
    }

    LaunchedEffect(key1 = postId, key2 = repostId, key3 = tokenState) {
        if (tokenState != null) {
            viewModel.setAuthToken(tokenState!!)
            viewModel.clearPost()
            viewModel.fetchPost(postId, repostId)
            viewModel.fetchComments(postId)
            viewModel.fetchPostLikes(postId)
        } else {
            viewModel._error.value = "Authentication token is missing"
        }
    }

    // Dynamic title based on whether this is a repost or a regular post
    val topBarTitle = if (repostId != null && originalPost != null) "Repost Detail" else "Post Detail"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Red)
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
                        // Log the state to debug why RepostLayout isn't triggered
                        Log.d("PostDetailScreen", "repostId: $repostId, originalPost: $originalPost, repost: $repost, repostData: $repostData")
                        if (repostId != null && originalPost != null && repostData != null) {
                            RepostLayout(
                                repostData = repostData!!, // Pass repostData
                                originalPost = originalPost!!,
                                isLiked = isLiked,
                                likeCount = likeCount,
                                commentsCount = comments.size,
                                onLikeClick = { viewModel.toggleLike(postId, originalPost!!.userId) }
                            )
                        } else {
                            PostLayout(
                                post = repost!!,
                                isLiked = isLiked,
                                likeCount = likeCount,
                                commentsCount = comments.size,
                                onLikeClick = { viewModel.toggleLike(postId, repost!!.userId) }
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
    repostData: Repost, // Use Repost instead of PostByIdResponse for repost user info
    originalPost: PostByIdResponse,
    isLiked: Boolean,
    likeCount: Int,
    commentsCount: Int,
    onLikeClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Repost user info (profile picture, username, "reposted", timestamp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile picture
                repostData.profilePicture?.let { url ->
                    Image(
                        painter = rememberAsyncImagePainter(model = imageRequest(url)),
                        contentDescription = "Repost User Profile",
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
                        contentDescription = "Repost User Profile",
                        modifier = Modifier.fillMaxSize(),
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                // Username, "reposted", and timestamp
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = repostData.username,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "reposted",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "·",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatTimestamp(repostData.createdAt),
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display the repost quote (if it exists)
            repostData.quote?.takeIf { it.isNotEmpty() }?.let { quote ->
                Text(
                    text = quote,
                    color = Color.Black,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Nested card for original post content
            Card(
                shape = RoundedCornerShape(4.dp),
                border = BorderStroke(1.dp, Color.Gray),
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box {
                    Column(modifier = Modifier.padding(8.dp)) {
                        // Original post user info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            originalPost.profilePicture?.let { url ->
                                Image(
                                    painter = rememberAsyncImagePainter(model = imageRequest(url)),
                                    contentDescription = "Original User Profile",
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
                                    contentDescription = "Original User Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    tint = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = originalPost.username,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = formatTimestamp(originalPost.createdAt),
                                    color = Color.Gray,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Original post content (text)
                        if (originalPost.content.isNotEmpty()) {
                            Text(
                                text = originalPost.content,
                                color = Color.Black,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Original post image (if available)
                        originalPost.images.firstOrNull()?.image_url?.let { imageUrl ->
                            Image(
                                painter = rememberAsyncImagePainter(model = imageRequest(imageUrl)),
                                contentDescription = "Repost Image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    // Report button (Info icon) at the top-right corner
                    IconButton(
                        onClick = { /* Handle report action */ },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Report",
                            tint = Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action buttons (Like, Comment, Repost) - Icons only
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onLikeClick) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color.Red else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Comment,
                    contentDescription = "Comment",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Icon(
                    imageVector = Icons.Default.Repeat,
                    contentDescription = "Repost",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun PostLayout(
    post: PostByIdResponse,
    isLiked: Boolean,
    likeCount: Int,
    commentsCount: Int,
    onLikeClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // User info (profile picture, username, timestamp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                post.profilePicture?.let { url ->
                    Image(
                        painter = rememberAsyncImagePainter(model = imageRequest(url)),
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
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = post.username,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                    Text(
                        text = post.createdAt.let { formatTimestamp(it) },
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Post content (text)
            if (post.content.isNotEmpty()) {
                Text(
                    text = post.content,
                    color = Color.Black,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Post image (if available)
            post.images.firstOrNull()?.image_url?.let { imageUrl ->
                Image(
                    painter = rememberAsyncImagePainter(model = imageRequest(imageUrl)),
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Action buttons (Like, Comment, Repost)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "$likeCount",
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repost",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${post.repostCount}",
                        color = Color.Black,
                        fontSize = 14.sp
                    )
                }
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
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
    }
}