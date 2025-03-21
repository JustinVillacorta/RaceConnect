package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import java.text.SimpleDateFormat
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference.NewsFeedPreferenceViewModel
import java.util.*

// Utility function to format time relative to now
fun formatTime(createdAt: String?): String {
    if (createdAt.isNullOrEmpty()) return "Just now"

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val date = try {
        sdf.parse(createdAt)
    } catch (e: Exception) {
        Log.e("PostCard", "Error parsing date $createdAt: ${e.message}")
        return createdAt ?: "Just now"
    } ?: return "Just now"

    val now = Calendar.getInstance()
    val created = Calendar.getInstance().apply { time = date }
    val diffInMillis = now.timeInMillis - created.timeInMillis
    val diffInSeconds = diffInMillis / 1000
    val diffInMinutes = diffInSeconds / 60
    val diffInHours = diffInMinutes / 60
    val diffInDays = diffInHours / 24

    return when {
        diffInMinutes < 1 -> "Just now"
        diffInMinutes < 60 -> "${diffInMinutes} minute${if (diffInMinutes > 1) "s" else ""} ago"
        diffInHours < 24 -> "${diffInHours} hour${if (diffInHours > 1) "s" else ""} ago"
        diffInDays < 7 -> "${diffInDays} day${if (diffInDays > 1) "s" else ""} ago"
        else -> sdf.format(date) // Fall back to full date for older posts
    }
}

@Composable
fun PostCard(
    post: NewsFeedDataClassItem,
    navController: NavController,
    onCommentClick: () -> Unit,
    onLikeClick: (Boolean) -> Unit,
    viewModel: NewsFeedViewModel,
    onShowFullScreenImage: (List<String>, Int) -> Unit,
    userPreferences: UserPreferences,
    onReportClick: (Int, String, String?) -> Unit,
    onShowRepostScreen: (NewsFeedDataClassItem) -> Unit,
    onUserActionClick: (Int, String, String?) -> Unit,
    context: Context = LocalContext.current,
    modifier: Modifier = Modifier
) {
    val postLikes by viewModel.postLikes.collectAsState()
    val isLiked = postLikes[post.id] ?: post.isLiked
    val likeCounts by viewModel.likeCounts.collectAsState()
    val likeCount = likeCounts[post.id] ?: post.like_count
    val commentCounts by viewModel.commentCounts.collectAsState() // Add this
    val commentCount = commentCounts[post.id] ?: post.comment_count // Add this
    val repostCounts by viewModel.repostCounts.collectAsState() // Add this
    val repostCount = repostCounts[post.id] ?: post.repost_count // Add this
    val postImagesMap by viewModel.postImages.collectAsState()
    val imageUrls = postImagesMap[post.id] ?: post.images ?: emptyList()
    var showReportDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var otherText by remember { mutableStateOf("") }
    val user by userPreferences.user.collectAsState(initial = null)
    val loggedInUserId = user?.id

    LaunchedEffect(post.id) {
        viewModel.getPostImages(post.id)
        // Optionally fetch counts here if not already fetched
        // viewModel.fetchPostData(post.id)
    }

    Card(
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                // Profile and Header (unchanged)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable {
                                val destination = if (loggedInUserId == post.user_id) {
                                    NavRoutes.ProfileView.createRoute(loggedInUserId!!)
                                } else {
                                    NavRoutes.ProfileView.createRoute(post.user_id)
                                }
                                navController.navigate(destination)
                            }
                    ) {
                        if (post.profile_picture != null) {
                            AsyncImage(
                                model = post.profile_picture,
                                contentDescription = "User Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Profile",
                                modifier = Modifier.fillMaxSize(),
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.username ?: "Anonymous",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatTime(post.created_at),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = post.content ?: "No content available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Image Carousel with LazyRow (unchanged)
                if (imageUrls.isNotEmpty()) {
                    if (imageUrls.size == 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onShowFullScreenImage(imageUrls, 0)
                                }
                        ) {
                            AsyncImage(
                                model = imageUrls.first(),
                                contentDescription = "Post image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.91f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else {
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp)
                                .padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            itemsIndexed(imageUrls) { index, url ->
                                Box(
                                    modifier = Modifier
                                        .width(300.dp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onShowFullScreenImage(imageUrls, index)
                                        }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Post image $index",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reactions with Comment and Repost Counters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like
                    ReactionIcon(
                        icon = Icons.Default.Favorite,
                        isLiked = isLiked,
                        onClick = { viewModel.toggleLike(post.id, post.user_id) }
                    )
                    Text(
                        text = "$likeCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                    )

                    // Comment
                    ReactionIcon(
                        icon = Icons.Default.ChatBubble,
                        onClick = onCommentClick
                    )
                    Text(
                        text = "$commentCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                    )

                    // Repost
                    ReactionIcon(
                        icon = Icons.Default.Repeat,
                        onClick = { onShowRepostScreen(post) }
                    )
                    Text(
                        text = "$repostCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // More Options (unchanged)
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { menuExpanded = true }
                        .padding(8.dp),
                    tint = Color.Gray
                )
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Report Post") },
                        onClick = {
                            menuExpanded = false
                            showReportDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("User Actions") },
                        onClick = {
                            menuExpanded = false
                            showUserDialog = true
                        }
                    )
                }
            }
        }
    }

    // Report Dialog and User Action Dialog (unchanged)
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(text = "Report Post") },
            text = {
                Column {
                    Text(text = "Please select a reason for reporting this post:")
                    Spacer(modifier = Modifier.height(16.dp))

                    val reportOptions = listOf("Not related", "Nudity", "Inappropriate", "Others")
                    reportOptions.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedReason = reason },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == reason,
                                onClick = { selectedReason = reason }
                            )
                            Text(
                                text = reason,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (selectedReason == "Others") {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = otherText,
                            onValueChange = { otherText = it },
                            label = { Text("Please specify") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedReason.isNotEmpty() && (selectedReason != "Others" || otherText.isNotEmpty()))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            if (selectedReason.isNotEmpty()) {
                                if (selectedReason == "Others" && otherText.isEmpty()) {
                                    Toast.makeText(context, "Please specify the reason", Toast.LENGTH_SHORT).show()
                                } else {
                                    onReportClick(post.id, selectedReason, if (selectedReason == "Others") otherText else null)
                                    showReportDialog = false
                                }
                            }
                        }
                        .padding(8.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable { showReportDialog = false }
                        .padding(8.dp)
                )
            }
        )
    }

    if (showUserDialog) {
        AlertDialog(
            onDismissRequest = { showUserDialog = false },
            title = { Text("User Actions") },
            text = {
                Column {
                    Text("Please select an action for this user:")
                    Spacer(modifier = Modifier.height(16.dp))
                    val userOptions = listOf("Report User", "Others")
                    userOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedReason = option },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedReason == option,
                                onClick = { selectedReason = option }
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    if (selectedReason == "Others") {
                        Spacer(modifier = Modifier.height(16.dp))
                        TextField(
                            value = otherText,
                            onValueChange = { otherText = it },
                            label = { Text("Please specify the action") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (selectedReason.isNotEmpty() && (selectedReason != "Others" || otherText.isNotEmpty()))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            if (selectedReason.isNotEmpty()) {
                                if (selectedReason == "Others" && otherText.isEmpty()) {
                                    Toast.makeText(context, "Please specify the action", Toast.LENGTH_SHORT).show()
                                } else {
                                    onUserActionClick(post.user_id, selectedReason, if (selectedReason == "Others") otherText else null)
                                    showUserDialog = false
                                }
                            }
                        }
                        .padding(8.dp)
                )
            },
            dismissButton = {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clickable { showUserDialog = false }
                        .padding(8.dp)
                )
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenImageViewer(
    imageUrls: List<String>,
    initialIndex: Int,
    postId: Int,
    onDismiss: () -> Unit,
    onLikeClick: (Boolean) -> Unit,
    onCommentClick: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isLiked by remember { mutableStateOf(false) }
    var currentIndex by remember { mutableStateOf(initialIndex) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Black
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            scale = if (scale <= 1f) 2.5f else 1f
                            offset = Offset.Zero
                        }
                    )
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 5f)
                        offset += pan
                        val maxOffsetX = (size.width * (scale - 1)) / 2
                        val maxOffsetY = (size.height * (scale - 1)) / 2
                        offset = Offset(
                            offset.x.coerceIn(-maxOffsetX, maxOffsetX),
                            offset.y.coerceIn(-maxOffsetY, maxOffsetY)
                        )
                    }
                    detectDragGestures(
                        onDrag = { _, dragAmount ->
                            if (scale == 1f) offset += dragAmount
                        },
                        onDragEnd = {
                            if (scale == 1f) {
                                if (offset.y > 200f) {
                                    onDismiss()
                                } else if (offset.x > 200f && currentIndex > 0) {
                                    currentIndex-- // Swipe right to previous image
                                } else if (offset.x < -200f && currentIndex < imageUrls.size - 1) {
                                    currentIndex++ // Swipe left to next image
                                }
                                offset = Offset.Zero
                            }
                        }
                    )
                }
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrls[currentIndex])
                    .crossfade(true)
                    .build(),
                contentDescription = "Full-screen image $currentIndex",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    ),
                contentScale = ContentScale.Fit
            )

            // Close Button
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(6.dp)
                    .clickable { onDismiss() },
                tint = Color.White
            )

            // Navigation Arrows (optional, for clarity)
            if (imageUrls.size > 1) {
                if (currentIndex > 0) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Previous",
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(16.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(8.dp)
                            .clickable {
                                currentIndex--
                                offset = Offset.Zero
                            },
                        tint = Color.White
                    )
                }
                if (currentIndex < imageUrls.size - 1) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = "Next",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(16.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .padding(8.dp)
                            .clickable {
                                currentIndex++
                                offset = Offset.Zero
                            },
                        tint = Color.White
                    )
                }
            }

            // Like/Comment Actions
            if (postId != 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .background(Color.Black.copy(alpha = 0.8f), shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    isLiked = !isLiked
                                    onLikeClick(isLiked)
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Like",
                                tint = if (isLiked) Color.Red else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Like",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable { onCommentClick() }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "Comments",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Comments",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
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
            .size(24.dp)
            .clickable { onClick?.invoke() },
        tint = if (isLiked) Color.Red else Color.Gray
    )
}


@Preview(showBackground = true)
@Composable
fun PostCardSimplePreview() {
    val context = LocalContext.current
    val mockPost = NewsFeedDataClassItem(
        id = 1,
        user_id = 1,
        username = "TestUser",
        profile_picture = null,
        content = "This is a test post",
        images = listOf("https://example.com/image1.jpg", "https://example.com/image2.jpg"),
        created_at = "2025-03-19T10:00:00Z",
        like_count = 20,
        comment_count = 5,
        repost_count = 3,
        isLiked = false,
        title = "t"
    )
    val mockNavController = remember { object : NavController(context) {} }
    val mockUserPreferences = UserPreferences(context)
    val mockPreferenceViewModel = NewsFeedPreferenceViewModel(
        userPreferences = mockUserPreferences
    )
    val mockViewModel = NewsFeedViewModel(
        userPreferences = mockUserPreferences,
        preferenceViewModel = mockPreferenceViewModel,
        context = context
    )

    PostCard(
        post = mockPost,
        navController = mockNavController,
        onCommentClick = { /* No-op for preview */ },
        onLikeClick = { _ -> /* No-op for preview */ },
        viewModel = mockViewModel,
        onShowFullScreenImage = { _, _ -> /* No-op for preview */ },
        userPreferences = mockUserPreferences,
        onReportClick = { _, _, _ -> /* No-op for preview */ },
        onShowRepostScreen = { _ -> /* No-op for preview */ },
        onUserActionClick = { _, _, _ -> /* No-op for preview */ },
        context = context,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}