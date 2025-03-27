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
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Report
import androidx.compose.ui.draw.blur
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.view.ui.theme.White

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
fun ExpandableText(
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var isTruncated by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            onTextLayout = { textLayoutResult ->
                if (!expanded && textLayoutResult.hasVisualOverflow) {
                    isTruncated = true
                }
            }
        )
        if (isTruncated) {
            val toggleText = if (expanded) "See Less" else "See More"
            Text(
                text = toggleText,
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clickable(enabled = enabled) {
                        if (enabled) {
                            expanded = !expanded
                        }
                    }
                    .padding(top = 2.dp)
            )
        }
    }
}

// PostCard Composable
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
    val commentCounts by viewModel.commentCounts.collectAsState()
    val commentCount = commentCounts[post.id] ?: post.comment_count
    val repostCounts by viewModel.repostCounts.collectAsState()
    val repostCount = repostCounts[post.id] ?: post.repost_count
    val postImagesMap by viewModel.postImages.collectAsState()
    val imageUrls = postImagesMap[post.id] ?: post.images ?: emptyList()
    val userReposts by viewModel.userReposts.collectAsState() // Assuming this exists in NewsFeedViewModel
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var otherText by remember { mutableStateOf("") }
    val user by userPreferences.user.collectAsState(initial = null)
    val loggedInUserId = user?.id

    var showHiddenPost by remember(post.id, post.status) { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showSelfRepostDialog by remember { mutableStateOf(false) }
    var showAlreadyRepostedDialog by remember { mutableStateOf(false) } // New state for already reposted dialog

    // Check if the logged-in user has already reposted this post
    val hasReposted = userReposts.any { it.userId == loggedInUserId && it.postId == post.id }

    LaunchedEffect(post.id, post.status) {
        viewModel.getPostImages(post.id)
        Log.d("PostCard", "Rendering post ID: ${post.id}, Status: ${post.status}, Report: ${post.report}, ShowHidden: $showHiddenPost")
    }

    if (post.status?.lowercase() == "archived") {
        Log.w("PostCard", "Archived post ID: ${post.id} reached UI")
        return
    }

    Card(
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                            .clickable(
                                enabled = !(post.status?.lowercase() == "hidden" && !showHiddenPost),
                                onClick = {
                                    val destination = if (loggedInUserId == post.user_id) {
                                        NavRoutes.ProfileView.createRoute(loggedInUserId!!)
                                    } else {
                                        NavRoutes.ProfileView.createRoute(post.user_id)
                                    }
                                    navController.navigate(destination)
                                }
                            )
                    ) {
                        if (post.profile_picture != null) {
                            AsyncImage(
                                model = post.profile_picture,
                                contentDescription = "User Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                                            Modifier.blur(10.dp)
                                        } else {
                                            Modifier
                                        }
                                    )
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "User Profile",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .then(
                                        if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                                            Modifier.blur(10.dp)
                                        } else {
                                            Modifier
                                        }
                                    ),
                                tint = Color.White
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.username ?: "Anonymous",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(
                                    enabled = !(post.status?.lowercase() == "hidden" && !showHiddenPost),
                                    onClick = {
                                        val destination = if (loggedInUserId == post.user_id) {
                                            NavRoutes.ProfileView.createRoute(loggedInUserId!!)
                                        } else {
                                            NavRoutes.ProfileView.createRoute(post.user_id)
                                        }
                                        navController.navigate(destination)
                                    }
                                )
                                .then(
                                    if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                                        Modifier.blur(10.dp)
                                    } else {
                                        Modifier
                                    }
                                )
                        )
                        Text(
                            text = formatTime(post.created_at),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                                Modifier.blur(10.dp)
                            } else {
                                Modifier
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                    // Blurred content
                    ExpandableText(
                        text = post.content ?: "No content available.",
                        enabled = false,
                        modifier = Modifier.blur(10.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (imageUrls.isNotEmpty()) {
                        if (imageUrls.size == 1) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(
                                        enabled = false,
                                        onClick = {}
                                    )
                            ) {
                                AsyncImage(
                                    model = imageUrls.first(),
                                    contentDescription = "Post image",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(1.91f)
                                        .blur(10.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                                    .padding(top = 8.dp)
                            ) {
                                val pagerState = rememberPagerState(pageCount = { imageUrls.size })
                                HorizontalPager(
                                    state = pagerState,
                                    pageSpacing = 0.dp,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable(
                                                enabled = false,
                                                onClick = {}
                                            )
                                    ) {
                                        AsyncImage(
                                            model = imageUrls[page],
                                            contentDescription = "Post image $page",
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .blur(10.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${pagerState.currentPage + 1}/${imageUrls.size}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.blur(10.dp)
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    imageUrls.forEachIndexed { index, _ ->
                                        val isSelected = index == pagerState.currentPage
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else Color.Transparent)
                                                .then(
                                                    if (!isSelected) Modifier.border(1.dp, Color.White, CircleShape)
                                                    else Modifier
                                                )
                                                .blur(10.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReactionIcon(
                            icon = Icons.Default.Favorite,
                            isLiked = isLiked,
                            onClick = {},
                            enabled = false
                        )
                        Text(
                            text = "$likeCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(start = 4.dp, end = 12.dp)
                                .blur(10.dp)
                        )
                        ReactionIcon(
                            icon = Icons.Default.ChatBubble,
                            onClick = {},
                            enabled = false
                        )
                        Text(
                            text = "$commentCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(start = 4.dp, end = 12.dp)
                                .blur(10.dp)
                        )
                        ReactionIcon(
                            icon = Icons.Default.Repeat,
                            onClick = {},
                            enabled = false
                        )
                        Text(
                            text = "$repostCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .blur(10.dp)
                        )
                    }
                } else {
                    // Normal content (not blurred)
                    ExpandableText(
                        text = post.content ?: "No content available.",
                        modifier = Modifier
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(340.dp)
                                    .padding(top = 8.dp)
                            ) {
                                val pagerState = rememberPagerState(pageCount = { imageUrls.size })
                                HorizontalPager(
                                    state = pagerState,
                                    pageSpacing = 0.dp,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable {
                                                onShowFullScreenImage(imageUrls, page)
                                            }
                                    ) {
                                        AsyncImage(
                                            model = imageUrls[page],
                                            contentDescription = "Post image $page",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(8.dp)
                                        .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${pagerState.currentPage + 1}/${imageUrls.size}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    imageUrls.forEachIndexed { index, _ ->
                                        val isSelected = index == pagerState.currentPage
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(if (isSelected) Color.White else Color.Transparent)
                                                .then(
                                                    if (!isSelected) Modifier.border(1.dp, Color.White, CircleShape)
                                                    else Modifier
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReactionIcon(
                            icon = Icons.Default.Favorite,
                            isLiked = isLiked,
                            onClick = { viewModel.toggleLike(post.id, post.user_id) },
                            enabled = true
                        )
                        Text(
                            text = "$likeCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                        )
                        ReactionIcon(
                            icon = Icons.Default.ChatBubble,
                            onClick = onCommentClick,
                            enabled = true
                        )
                        Text(
                            text = "$commentCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                        )
                        ReactionIcon(
                            icon = Icons.Default.Repeat,
                            onClick = {
                                when {
                                    loggedInUserId == post.user_id -> {
                                        showSelfRepostDialog = true // User is the poster
                                    }
                                    hasReposted -> {
                                        showAlreadyRepostedDialog = true // User has already reposted
                                    }
                                    else -> {
                                        onShowRepostScreen(post) // Proceed with repost
                                    }
                                }
                            },
                            enabled = loggedInUserId != post.user_id && !hasReposted // Disable if self-post or already reposted
                        )
                        Text(
                            text = "$repostCount",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                    if (post.status?.lowercase() == "hidden" && showHiddenPost) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showHiddenPost = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                        ) {
                            Text("Hide Post", color = Color.Black)
                        }
                    }
                }
            }

            if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "This post is hidden",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showConfirmationDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            )
                        ) {
                            Text("See Post")
                        }
                    }
                }
            }

            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    imageVector = Icons.Default.Report,
                    contentDescription = "Report post",
                    modifier = Modifier
                        .size(34.dp)
                        .clickable(
                            enabled = !(post.status?.lowercase() == "hidden" && !showHiddenPost),
                            onClick = { showReportDialog = true }
                        )
                        .padding(8.dp),
                    tint = Color.Gray
                )
            }
        }
    }

    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Post") },
            text = {
                Column {
                    Text("Please select a reason:")
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
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }
                    if (selectedReason == "Others") {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            value = otherText,
                            onValueChange = { otherText = it },
                            label = { Text("Specify reason") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (selectedReason.isNotEmpty() && (selectedReason != "Others" || otherText.isNotEmpty())) {
                            onReportClick(post.id, selectedReason, if (selectedReason == "Others") otherText else null)
                            showReportDialog = false
                        } else if (selectedReason == "Others") {
                            Toast.makeText(context, "Please specify the reason", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showConfirmationDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmationDialog = false },
            title = { Text("View Hidden Post") },
            text = { Text("Are you sure you want to view this hidden post?") },
            confirmButton = {
                TextButton(onClick = {
                    showHiddenPost = true
                    showConfirmationDialog = false
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmationDialog = false }) {
                    Text("No")
                }
            }
        )
    }

    if (showSelfRepostDialog) {
        AlertDialog(
            onDismissRequest = { showSelfRepostDialog = false },
            title = { Text("Repost Error") },
            text = { Text("You can't repost your own post.") },
            confirmButton = {
                TextButton(onClick = { showSelfRepostDialog = false }) {
                    Text("OK")
                }
            }
        )
    }

    // New Alert Dialog for already reposted attempt
    if (showAlreadyRepostedDialog) {
        AlertDialog(
            onDismissRequest = { showAlreadyRepostedDialog = false },
            title = { Text("Repost Error") },
            text = { Text("You have already reposted this post.") },
            confirmButton = {
                TextButton(onClick = { showAlreadyRepostedDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun ReactionIcon(icon: ImageVector, isLiked: Boolean = false, onClick: (() -> Unit)? = null, enabled: Boolean = true) {
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