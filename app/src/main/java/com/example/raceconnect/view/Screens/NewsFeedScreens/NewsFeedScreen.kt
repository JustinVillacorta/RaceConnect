package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.AnnouncementDataClass
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.network.NewsFeedPagingSourceAllPosts
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.view.ui.theme.fontFamily
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    navController: NavController,
    userPreferences: UserPreferences,
    onShowCreatePost: () -> Unit,
    onShowFullScreenImage: (List<String>, Int, Int) -> Unit,
    onShowProfileView: (Int) -> Unit,
    onShowRepostScreen: (NewsFeedDataClassItem) -> Unit
) {
    val authViewModel: AuthenticationViewModel = viewModel()
    val context = LocalContext.current
    val viewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences, context))
    val posts = viewModel.postsFlow.collectAsLazyPagingItems()
    val errorMessage by authViewModel.ErrorMessage.collectAsState()
    var isRefreshing by remember { mutableStateOf(false) }
    val postLikes by viewModel.postLikes.collectAsState()
    val likeCounts by viewModel.likeCounts.collectAsState()
    val newPostTriggerState by viewModel.newPostTrigger.collectAsState()
    val user by userPreferences.user.collectAsState(initial = null)
    val loggedInUserId = user?.id ?: 0
    val userReposts by viewModel.userReposts.collectAsState() // Add this to check reposts
    var showAlreadyRepostedDialog by remember { mutableStateOf(false) } // Dialog state

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPostId by remember { mutableStateOf<Int?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val sheetHeight = screenHeight * 0.85f

    // Fetch user reposts on initialization
    LaunchedEffect(loggedInUserId) {
        if (loggedInUserId != 0) {
            viewModel.fetchUserReposts(loggedInUserId)
        }
    }

    LaunchedEffect(posts.itemCount) {
        Log.d("NewsFeedScreen", "Received ${posts.itemCount} items in posts")
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage?.contains("banned", ignoreCase = true) == true) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
            Log.d("NewsFeedScreen", "User logged out due to ban: $errorMessage")
        }
    }

    LaunchedEffect(Unit, newPostTriggerState) {
        if (!viewModel.isInitialRefreshDone || newPostTriggerState) {
            isRefreshing = true
            NewsFeedPagingSourceAllPosts.clearCaches()
            viewModel.refreshPosts()
            posts.refresh()
            delay(100)
            if (newPostTriggerState) viewModel.resetNewPostTrigger()
            viewModel.isInitialRefreshDone = true
            Log.d("NewsFeedScreen", "Refresh triggered")
        }
    }

    LaunchedEffect(posts.loadState.refresh, posts.loadState.append) {
        showErrorDialog = posts.loadState.refresh is LoadState.Error || posts.loadState.append is LoadState.Error
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false },
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
        ) {
            CommentSectionScreen(
                postId = selectedPostId ?: -1,
                navController = navController,
                userPreferences = userPreferences,
                onShowProfileView = { userId -> onShowProfileView(userId); showBottomSheet = false }
            )
        }
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = {
                Text(
                    text = "Connection Error",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = "No Internet Icon",
                        tint = Color.Red,
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .height(48.dp)
                    )
                    Text(
                        text = "No internet connection, please check",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showErrorDialog = false
                    isRefreshing = true
                    NewsFeedPagingSourceAllPosts.clearCaches()
                    viewModel.refreshPosts()
                    posts.refresh()
                    Log.d("NewsFeedScreen", "Retry clicked - Refresh triggered")
                }) {
                    Text("Retry", color = Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    // Alert Dialog for already reposted post
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RaceConnect", fontFamily = fontFamily, color = Color.White, fontSize = 30.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Red)
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                NewsFeedPagingSourceAllPosts.clearCaches()
                viewModel.refreshPosts()
                posts.refresh()
                Log.d("NewsFeedScreen", "Swipe-to-refresh triggered")
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                item {
                    AddPostSection(
                        navController,
                        onAddPostClick = onShowCreatePost,
                        onShowProfileView = { onShowProfileView(loggedInUserId) }
                    )
                }

                items(posts.itemCount) { index ->
                    val item = posts[index]
                    item?.let { feedItem ->
                        LaunchedEffect(feedItem.id) {
                            if (postLikes[feedItem.id] == null && !feedItem.isAnnouncement) {
                                viewModel.fetchPostLikes(feedItem.id)
                                viewModel.fetchPostComments(feedItem.id)
                                viewModel.fetchPostReposts(feedItem.id)
                            }
                        }

                        val isLiked = postLikes[feedItem.id] ?: false
                        val likeCount = likeCounts[feedItem.id] ?: feedItem.like_count

                        // Check if the post has been reposted by the logged-in user
                        val hasReposted = userReposts.any { it.userId == loggedInUserId && it.postId == feedItem.id }

                        when {
                            feedItem.isAnnouncement -> {
                                AnnouncementCard(
                                    announcement = AnnouncementDataClass(
                                        id = feedItem.id,
                                        title = feedItem.title ?: "Announcement",
                                        content = feedItem.content,
                                        image_url = feedItem.images?.firstOrNull(),
                                        status = "active",
                                        created_at = feedItem.created_at
                                    )
                                )
                            }
                            feedItem.isRepost == true -> {
                                val originalPost = posts.itemSnapshotList.items.find { it.id == feedItem.original_post_id }
                                    ?: NewsFeedDataClassItem(
                                        id = feedItem.original_post_id ?: -1,
                                        user_id = -1,
                                        content = "Original post unavailable",
                                        created_at = "",
                                        isRepost = false,
                                        original_post_id = null,
                                        like_count = 0,
                                        comment_count = 0,
                                        repost_count = 0,
                                        category = feedItem.category,
                                        privacy = "Public",
                                        type = "text",
                                        postType = "normal",
                                        title = "Unavailable",
                                        username = "Unknown"
                                    )
                                RepostCard(
                                    repost = feedItem.copy(isLiked = isLiked, like_count = likeCount),
                                    originalPost = originalPost,
                                    navController = navController,
                                    viewModel = viewModel,
                                    onCommentClick = { selectedPostId = originalPost.id; showBottomSheet = true },
                                    onLikeClick = { liked ->
                                        if (liked) viewModel.toggleLike(feedItem.id, feedItem.user_id) else viewModel.unlikePost(feedItem.id)
                                    },
                                    onShowFullScreenImage = { imageUrls, initialIndex ->
                                        onShowFullScreenImage(imageUrls, initialIndex, feedItem.id)
                                    },
                                    userPreferences = userPreferences,
                                    onReportClick = { postId, reason, otherText ->
                                        viewModel.reportPost(postId, reason, otherText, onSuccess = {}, onFailure = { error ->
                                            Log.e("NewsFeedScreen", "Failed to report post: $error")
                                        })
                                    },
                                    onShowRepostScreen = { post ->
                                        val hasRepostedInner = userReposts.any { it.userId == loggedInUserId && it.postId == post.id }
                                        if (hasRepostedInner) {
                                            showAlreadyRepostedDialog = true
                                        } else {
                                            onShowRepostScreen(post)
                                        }
                                    },
                                    onUserActionClick = { userId, action, otherText ->
                                        if (action == "Report User") viewModel.reportUser(userId, action, otherText)
                                    }
                                )
                            }
                            else -> {
                                PostCard(
                                    post = feedItem.copy(isLiked = isLiked, like_count = likeCount),
                                    navController = navController,
                                    viewModel = viewModel,
                                    onCommentClick = { selectedPostId = feedItem.id; showBottomSheet = true },
                                    onLikeClick = { liked ->
                                        if (liked) viewModel.toggleLike(feedItem.id, feedItem.user_id) else viewModel.unlikePost(feedItem.id)
                                    },
                                    onShowFullScreenImage = { imageUrls, initialIndex ->
                                        onShowFullScreenImage(imageUrls, initialIndex, feedItem.id)
                                    },
                                    userPreferences = userPreferences,
                                    onReportClick = { postId, reason, otherText ->
                                        viewModel.reportPost(postId, reason, otherText, onSuccess = {}, onFailure = { error ->
                                            Log.e("NewsFeedScreen", "Failed to report post: $error")
                                        })
                                    },
                                    onShowRepostScreen = { post ->
                                        if (hasReposted) {
                                            showAlreadyRepostedDialog = true
                                        } else {
                                            onShowRepostScreen(post)
                                        }
                                    },
                                    onUserActionClick = { userId, action, otherText ->
                                        if (action == "Report User") viewModel.reportUser(userId, action, otherText)
                                    }
                                )
                            }
                        }
                    }
                }

                posts.apply {
                    when (loadState.refresh) {
                        is LoadState.Loading -> item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                                CircularProgressIndicator()
                            }
                            isRefreshing = false
                            Log.d("NewsFeedScreen", "Refresh state: Loading")
                        }
                        is LoadState.Error -> item {
                            Text(
                                "No Posts yet\nLooks like you haven't posted anything yet",
                                color = Color.Gray,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                            isRefreshing = false
                            Log.e("NewsFeedScreen", "Refresh state: Error - Showing 'No Posts yet' message")
                        }
                        is LoadState.NotLoading -> {
                            isRefreshing = false
                            if (posts.itemCount == 0) item {
                                Text(
                                    "No Posts yet\nLooks like you haven't posted anything yet",
                                    color = Color.Gray,
                                    modifier = Modifier.padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                                Log.d("NewsFeedScreen", "Refresh state: NotLoading, no items available")
                            }
                        }
                    }
                    when (loadState.append) {
                        is LoadState.Loading -> item {
                            CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
                            Log.d("NewsFeedScreen", "Append state: Loading")
                        }
                        is LoadState.Error -> {
                            Log.e("NewsFeedScreen", "Append state: Error - AlertDialog already shown")
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}