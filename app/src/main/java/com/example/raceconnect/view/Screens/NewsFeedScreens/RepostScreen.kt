package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.view.ui.theme.fontFamily
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference.NewsFeedPreferenceViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepostScreen(
    post: NewsFeedDataClassItem,
    navController: NavController,
    viewModel: NewsFeedViewModel,
    onClose: () -> Unit,
    userPreferences: UserPreferences
) {
    val context = LocalContext.current
    var repostComment by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Repost",fontFamily = fontFamily, color = Color.White, fontSize = 24.sp) },
                navigationIcon = {
                    IconButton(onClick = { onClose() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Red
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // TextField with Outline
                BasicTextField(
                    value = repostComment,
                    onValueChange = { repostComment = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .padding(16.dp),
                    textStyle = TextStyle(
                        color = Color.Black,
                        fontSize = MaterialTheme.typography.bodyMedium.fontSize
                    ),
                    decorationBox = { innerTextField ->
                        Box {
                            if (repostComment.isEmpty()) {
                                Text(
                                    text = "Add a comment to your repost (optional)",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable PostCard
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    PostCard(
                        post = post,
                        navController = navController,
                        onCommentClick = { /* Disabled */ },
                        onLikeClick = { _ -> /* Disabled */ },
                        viewModel = viewModel,
                        onShowFullScreenImage = { _, _ -> /* Disabled: No-op with correct signature */ },
                        userPreferences = userPreferences,
                        onReportClick = { postId, reason, otherText ->
                            viewModel.reportPost(postId, reason, otherText, onSuccess = {
                                Log.d("RepostScreen", "Post reported successfully")
                            }, onFailure = { error ->
                                Log.e("RepostScreen", "Failed to report post: $error")
                            })
                        },
                        onShowRepostScreen = { /* Disabled */ },
                        onUserActionClick = { userId, action, otherText ->
                            when (action) {
                                "Report User" -> viewModel.reportUser(userId, action, otherText)
                                else -> Log.d("RepostScreen", "Unhandled action: $action")
                            }
                        }
                    )
                }
            }

            // Repost Button
            Button(
                onClick = {
                    viewModel.repostPost(post.id, repostComment)
                    Toast.makeText(context, "Reposted successfully!", Toast.LENGTH_SHORT).show()
                    onClose()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Repost", color = Color.White, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun RepostCard(
    repost: NewsFeedDataClassItem,
    originalPost: NewsFeedDataClassItem?,
    navController: NavController,
    onCommentClick: () -> Unit,
    onLikeClick: (Boolean) -> Unit,
    viewModel: NewsFeedViewModel,
    onShowFullScreenImage: (List<String>, Int) -> Unit,
    userPreferences: UserPreferences,
    onReportClick: (Int, String, String?) -> Unit,
    onShowRepostScreen: (NewsFeedDataClassItem) -> Unit,
    onUserActionClick: (Int, String, String?) -> Unit
) {
    val user by userPreferences.user.collectAsState(initial = null)
    val context = LocalContext.current
    var showReportDialog by remember { mutableStateOf(false) }
    var showUserDialog by remember { mutableStateOf(false) }
    var showAlreadyRepostedDialog by remember { mutableStateOf(false) } // State for already reposted dialog

    // State to hold the dynamically fetched original post
    var fetchedOriginalPost by remember { mutableStateOf<NewsFeedDataClassItem?>(originalPost) }
    val originalPosts by viewModel.originalPosts.collectAsState()
    val userReposts by viewModel.userReposts.collectAsState() // Assuming this exists in NewsFeedViewModel
    val loggedInUserId = user?.id

    // Fetch the original post if not provided and original_post_id is available
    LaunchedEffect(repost.original_post_id) {
        if (fetchedOriginalPost == null && repost.original_post_id != null) {
            viewModel.fetchOriginalPost(repost.original_post_id)
        }
    }

    // Update fetchedOriginalPost when the original post is fetched
    LaunchedEffect(originalPosts, repost.original_post_id) {
        if (repost.original_post_id != null) {
            fetchedOriginalPost = originalPosts[repost.original_post_id] ?: originalPost
        }
    }

    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 2.dp)
    ) {
        Box(modifier = Modifier.padding(8.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Repost Header (User who reposted)
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
                                val destination = if (user?.id == repost.user_id) {
                                    NavRoutes.ProfileView.createRoute(repost.user_id)
                                } else {
                                    NavRoutes.ProfileView.createRoute(repost.user_id)
                                }
                                navController.navigate(destination)
                            }
                    ) {
                        if (repost.profile_picture != null) {
                            AsyncImage(
                                model = repost.profile_picture,
                                contentDescription = "Reposter Profile",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                error = painterResource(id = android.R.drawable.ic_menu_gallery),
                                placeholder = painterResource(id = android.R.drawable.ic_menu_gallery)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Reposter Profile",
                                modifier = Modifier.fillMaxSize(),
                                tint = Color.White
                            )
                        }
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
                                text = "${repost.username ?: "Anonymous"} reposted",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                        Text(
                            text = formatTime(repost.created_at),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Repost Comment (if any) with ExpandableText
                if (!repost.content.isNullOrEmpty()) {
                    ExpandableText(
                        text = repost.content,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Original Post
                fetchedOriginalPost?.let { original ->
                    // Check if the user has already reposted the original post
                    val hasRepostedOriginal = userReposts.any { it.userId == loggedInUserId && it.postId == original.id }

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    ) {
                        PostCard(
                            post = original,
                            navController = navController,
                            onCommentClick = onCommentClick,
                            onLikeClick = onLikeClick,
                            viewModel = viewModel,
                            onShowFullScreenImage = onShowFullScreenImage,
                            userPreferences = userPreferences,
                            onReportClick = onReportClick,
                            onShowRepostScreen = { post ->
                                if (hasRepostedOriginal) {
                                    showAlreadyRepostedDialog = true // Show dialog if already reposted
                                } else {
                                    onShowRepostScreen(post) // Proceed with repost
                                }
                            },
                            onUserActionClick = onUserActionClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }
                } ?: run {
                    Text(
                        text = "Original post unavailable",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    // Report Dialog (fixed onDismissRequest)
    if (showReportDialog) {
        var selectedOption by remember { mutableStateOf("") }
        var otherText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showReportDialog = false }, // Fixed from showUserDialog
            title = { Text(text = "User Actions") },
            text = {
                Column {
                    Text(text = "Please select an action for this user:")
                    Spacer(modifier = Modifier.height(16.dp))

                    val userOptions = listOf("Report User", "Others")
                    userOptions.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { selectedOption = option },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedOption == option,
                                onClick = { selectedOption = option }
                            )
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (selectedOption == "Others") {
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
                    color = if (selectedOption.isNotEmpty() &&
                        (selectedOption != "Others" || otherText.isNotEmpty()))
                        MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier
                        .clickable {
                            if (selectedOption.isNotEmpty()) {
                                if (selectedOption == "Others" && otherText.isEmpty()) {
                                    Toast.makeText(context,
                                        "Please specify the action",
                                        Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context,
                                        "User action performed: $selectedOption",
                                        Toast.LENGTH_SHORT).show()
                                    onUserActionClick(repost.user_id, selectedOption,
                                        if (selectedOption == "Others") otherText else null)
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
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    // Alert Dialog for already reposted original post
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


@Preview(showBackground = true)
@Composable
fun RepostScreenSimplePreview() {
    val context = LocalContext.current
    val mockPost = NewsFeedDataClassItem(
        id = 1,
        user_id = 1,
        username = "PreviewUser",
        profile_picture = null,
        content = "This is a preview post",
        images = emptyList(),
        created_at = "2025-03-19T10:00:00Z",
        like_count = 10,
        comment_count = 5,
        repost_count = 2,
        isLiked = false,
        title = "title"
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

    RepostScreen(
        post = mockPost,
        navController = mockNavController,
        viewModel = mockViewModel,
        onClose = { /* No-op for preview */ },
        userPreferences = mockUserPreferences
    )
}

@Preview(showBackground = true)
@Composable
fun RepostCardSimplePreview() {
    val context = LocalContext.current
    val mockRepost = NewsFeedDataClassItem(
        id = 1,
        user_id = 1,
        username = "Reposter",
        profile_picture = null,
        content = "This is a repost comment",
        images = emptyList(),
        created_at = "2025-03-19T12:00:00Z",
        like_count = 5,
        comment_count = 2,
        repost_count = 1,
        isLiked = false,
        title = "1"
    )
    val mockOriginalPost = NewsFeedDataClassItem(
        id = 2,
        user_id = 2,
        username = "OriginalPoster",
        profile_picture = null,
        content = "Original post content",
        images = listOf("https://example.com/image.jpg"),
        created_at = "2025-03-19T10:00:00Z",
        like_count = 15,
        comment_count = 8,
        repost_count = 3,
        isLiked = true,
        title = "t "
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

    RepostCard(
        repost = mockRepost,
        originalPost = mockOriginalPost,
        navController = mockNavController,
        onCommentClick = { /* No-op for preview */ },
        onLikeClick = { _ -> /* No-op for preview */ },
        viewModel = mockViewModel,
        onShowFullScreenImage = { _, _ -> /* No-op for preview */ },
        userPreferences = mockUserPreferences,
        onReportClick = { _, _, _ -> /* No-op for preview */ },
        onShowRepostScreen = { _ -> /* No-op for preview */ },
        onUserActionClick = { _, _, _ -> /* No-op for preview */ }
    )
}