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
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Report
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.PostByIdResponse
import com.example.raceconnect.model.Repost
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.NotificationClickedViewModel
import com.example.raceconnect.viewmodel.NotificationClickedViewModelFactory
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import com.example.raceconnect.view.ui.theme.fontFamily
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    navController: NavController,
    postId: Int,
    repostId: Int? = null,
    userPreferences: UserPreferences,
    viewModel: NotificationClickedViewModel = viewModel(
        factory = NotificationClickedViewModelFactory(
            apiService = RetrofitInstance.api,
            userPreferences = userPreferences
        )
    ),
    onClose: () -> Unit = {}
) {
    val repost by viewModel.repost.collectAsState()
    val originalPost by viewModel.originalPost.collectAsState()
    val repostData by viewModel.repostData.collectAsState()
    val comments by viewModel.comments.collectAsState()
    val isLiked by viewModel.isLiked.collectAsState()
    val likeCount by viewModel.likeCount.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val userId by viewModel.userId.collectAsState()

    val tokenState by userPreferences.token.collectAsState(initial = null)

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

    val topBarTitle = if (repostId != null && originalPost != null) "Repost Detail" else "Post Detail"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle,  fontFamily = fontFamily, color = Color.White, fontSize = 24.sp) },
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
            userId == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "User ID not available. Please log in again.", color = Color.Red)
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
                        Log.d("PostDetailScreen", "repostId: $repostId, originalPost: $originalPost, repost: $repost, repostData: $repostData")
                        if (repostId != null && originalPost != null && repostData != null) {
                            RepostLayout(
                                repostData = repostData!!,
                                originalPost = originalPost!!,
                                isLiked = isLiked,
                                likeCount = likeCount,
                                commentsCount = comments.size,
                                onLikeClick = { viewModel.toggleLike(postId, originalPost!!.userId) },
                                userId = userId!!,
                                onReportClick = { postIdToReport, reason, otherText ->
                                    Log.d("PostDetailScreen", "Reporting post $postIdToReport with reason $reason and other text $otherText by user $userId")
                                    // Implement report functionality here
                                }
                            )
                        } else {
                            PostLayout(
                                post = repost!!,
                                isLiked = isLiked,
                                likeCount = likeCount,
                                commentsCount = comments.size,
                                onLikeClick = { viewModel.toggleLike(postId, repost!!.userId) },
                                userId = userId!!,
                                onReportClick = { postIdToReport, reason, otherText ->
                                    Log.d("PostDetailScreen", "Reporting post $postIdToReport with reason $reason and other text $otherText by user $userId")
                                    // Implement report functionality here
                                }
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
            color = Color.Black,
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

@Composable
fun RepostLayout(
    repostData: Repost,
    originalPost: PostByIdResponse,
    isLiked: Boolean,
    likeCount: Int,
    commentsCount: Int,
    onLikeClick: () -> Unit,
    userId: Int,
    onReportClick: (Int, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var otherText by remember { mutableStateOf("") }

    Card(
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Repost Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    ) {
                        repostData.profilePicture?.let { url ->
                            Image(
                                painter = rememberAsyncImagePainter(model = url),
                                contentDescription = "Repost User Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } ?: Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Repost User Profile",
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Repost Icon",
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${repostData.username} reposted",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = formatTimestamp(repostData.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Repost Quote (if any) with ExpandableText
                repostData.quote?.takeIf { it.isNotEmpty() }?.let { quote ->
                    ExpandableText(
                        text = quote,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Original Post
                Card(
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    PostLayout(
                        post = originalPost,
                        isLiked = isLiked,
                        likeCount = likeCount,
                        commentsCount = commentsCount,
                        onLikeClick = onLikeClick,
                        userId = userId,
                        onReportClick = onReportClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Report Icon for Repost
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    imageVector = Icons.Default.Report,
                    contentDescription = "Report repost",
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { showReportDialog = true }
                        .padding(8.dp),
                    tint = Color.Gray
                )
            }
        }
    }

    // Report Dialog for Repost
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Repost") },
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
                            onReportClick(repostData.id, selectedReason, if (selectedReason == "Others") otherText else null)
                            showReportDialog = false
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
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun PostLayout(
    post: PostByIdResponse,
    isLiked: Boolean,
    likeCount: Int,
    commentsCount: Int,
    onLikeClick: () -> Unit,
    userId: Int,
    onReportClick: (Int, String, String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showHiddenPost by remember(post.id, post.status) { mutableStateOf(false) }
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var selectedReason by remember { mutableStateOf("") }
    var otherText by remember { mutableStateOf("") }

    val imageUrls = post.images.map { it.image_url }

    Card(
        shape = RectangleShape,
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Column {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    ) {
                        post.profilePicture?.let { url ->
                            Image(
                                painter = rememberAsyncImagePainter(model = url),
                                contentDescription = "Profile Picture",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } ?: Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.fillMaxSize(),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = post.username,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = formatTimestamp(post.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content based on hidden status
                if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                    // Blurred content
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black,
                        modifier = Modifier.blur(10.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (imageUrls.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = imageUrls.first()),
                                contentDescription = "Post image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.91f)
                                    .blur(10.dp),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                } else {
                    // Normal content with ExpandableText
                    ExpandableText(
                        text = post.content,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (imageUrls.size == 1) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = imageUrls.first()),
                                contentDescription = "Post image",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1.91f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    } else if (imageUrls.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .padding(top = 8.dp)
                        ) {
                            val pagerState = rememberPagerState()
                            HorizontalPager(
                                count = imageUrls.size,
                                state = pagerState,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(8.dp))
                                ) {
                                    Image(
                                        painter = rememberAsyncImagePainter(model = imageUrls[page]),
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

                // Interaction Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Start,
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
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "Comment",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "$commentsCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp, end = 12.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Repeat,
                        contentDescription = "Repost",
                        tint = Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "${post.repostCount}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Hide Post Button for hidden posts
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

            // Hidden Post Overlay
            if (post.status?.lowercase() == "hidden" && !showHiddenPost) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "This post is hidden",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { showConfirmationDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White)
                        ) {
                            Text("See Post")
                        }
                    }
                }
            }

            // Report Icon
            Box(modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(
                    imageVector = Icons.Default.Report,
                    contentDescription = "Report post",
                    modifier = Modifier
                        .size(34.dp)
                        .clickable { showReportDialog = true }
                        .padding(8.dp),
                    tint = Color.Gray
                )
            }
        }
    }

    // Report Dialog
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

    // Confirmation Dialog for Hidden Post
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
}

private fun formatTimestamp(timestamp: String?): String {
    if (timestamp.isNullOrEmpty()) return "Just now"

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    sdf.timeZone = TimeZone.getTimeZone("UTC")
    val date = try {
        sdf.parse(timestamp) ?: return "Just now"
    } catch (e: Exception) {
        Log.e("PostDetailScreen", "Error parsing date $timestamp: ${e.message}")
        return "Just now"
    }

    val now = Date()
    val diff = now.time - date.time
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "$days days ago"
        hours > 0 -> "$hours hours ago"
        minutes > 0 -> "$minutes minutes ago"
        else -> "Just now"
    }
}