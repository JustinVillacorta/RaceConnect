package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.network.NewsFeedPagingSourceAllPosts
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    navController: NavController,
    userPreferences: UserPreferences,
    onShowCreatePost: () -> Unit,
    onShowFullScreenImage: (String, Int) -> Unit,
    onShowProfileView: () -> Unit,
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

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPostId by remember { mutableStateOf<Int?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Log received items for debugging
    LaunchedEffect(posts.itemCount) {
        Log.d("NewsFeedScreen", "Received ${posts.itemCount} items in posts: ${posts.itemSnapshotList.items.map { "ID=${it.id}, IsRepost=${it.isRepost}, OriginalPostId=${it.original_post_id}, CreatedAt=${it.created_at}, Content=${it.content}" }}")
        Log.d("NewsFeedScreen", "Full item list: ${posts.itemSnapshotList.items}")
    }

    // Handle ban navigation
    LaunchedEffect(errorMessage) {
        if (errorMessage?.contains("banned", ignoreCase = true) == true) {
            navController.navigate("login") {
                popUpTo(navController.graph.startDestinationId)
                launchSingleTop = true
            }
            Log.d("NewsFeedScreen", "User logged out due to ban: $errorMessage")
        }
    }

    // Single refresh trigger
    LaunchedEffect(Unit, newPostTriggerState) {
        if (!viewModel.isInitialRefreshDone || newPostTriggerState) {
            isRefreshing = true
            NewsFeedPagingSourceAllPosts.clearCaches() // Clear caches to fetch fresh data
            Log.d("NewsFeedScreen", "Caches cleared before refresh")
            viewModel.refreshPosts()
            posts.refresh()
            delay(100) // Small debounce to ensure refresh stabilizes
            if (newPostTriggerState) viewModel.resetNewPostTrigger()
            viewModel.isInitialRefreshDone = true
            Log.d("NewsFeedScreen", "Refresh triggered: Initial=${!viewModel.isInitialRefreshDone}, NewPost=$newPostTriggerState")
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false }
        ) {
            CommentSectionScreen(
                postId = selectedPostId ?: -1,
                navController = navController,
                userPreferences = userPreferences,
                onShowProfileView = { onShowProfileView(); showBottomSheet = false }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("RaceConnect", style = MaterialTheme.typography.headlineMedium, color = Color.White) },
                actions = {
                    IconButton(onClick = { /* Handle search */ }) {
                        Icon(painterResource(id = R.drawable.baseline_search_24), "Search", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Red)
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                NewsFeedPagingSourceAllPosts.clearCaches() // Clear caches to fetch fresh data
                Log.d("NewsFeedScreen", "Caches cleared before swipe refresh")
                viewModel.refreshPosts()
                posts.refresh()
                Log.d("NewsFeedScreen", "Swipe-to-refresh triggered")
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AddPostSection(
                        navController,
                        onAddPostClick = onShowCreatePost,
                        onShowProfileView = onShowProfileView
                    )
                }

                items(posts.itemCount) { index ->
                    val post = posts[index]
                    post?.let { postItem ->
                        // Fetch likes only once per post lifecycle
                        LaunchedEffect(postItem.id) {
                            if (postLikes[postItem.id] == null) { // Avoid redundant fetches
                                viewModel.fetchPostLikes(postItem.id)
                                Log.d("NewsFeedScreen", "Fetching likes for post ID: ${postItem.id}")
                            }
                        }

                        val isLiked = postLikes[postItem.id] ?: false
                        val likeCount = likeCounts[postItem.id] ?: postItem.like_count
                        Log.d("NewsFeedScreen", "Rendering Post ID: ${postItem.id}, IsRepost: ${postItem.isRepost}, OriginalPostId: ${postItem.original_post_id}, CreatedAt: ${postItem.created_at}, Content: ${postItem.content}")

                        if (postItem.isRepost == true) {
                            // Find the original post in the list
                            val originalPost = posts.itemSnapshotList.items.find { it.id == postItem.original_post_id }
                            if (originalPost == null) {
                                Log.w("NewsFeedScreen", "Original post ID ${postItem.original_post_id} not found for repost ID ${postItem.id}")
                            }
                            val displayOriginalPost = originalPost ?: NewsFeedDataClassItem(
                                id = postItem.original_post_id ?: -1,
                                user_id = -1,
                                content = "Original post unavailable",
                                created_at = "",
                                isRepost = false,
                                original_post_id = null,
                                like_count = 0,
                                comment_count = 0,
                                repost_count = 0,
                                category = postItem.category,
                                privacy = "Public",
                                type = "text",
                                postType = "normal",
                                title = "Unavailable",
                                username = "Unknown"
                            )
                            Log.d("NewsFeedScreen", "Rendering repost ID: ${postItem.id}, Original Post ID: ${displayOriginalPost.id}, Content: ${displayOriginalPost.content}")
                            RepostCard(
                                repost = postItem.copy(isLiked = isLiked, like_count = likeCount),
                                originalPost = displayOriginalPost,
                                navController = navController,
                                viewModel = viewModel,
                                onCommentClick = { selectedPostId = postItem.id; showBottomSheet = true },
                                onLikeClick = { liked ->
                                    if (liked) viewModel.toggleLike(postItem.id, postItem.user_id) else viewModel.unlikePost(postItem.id)
                                },
                                onShowFullScreenImage = { onShowFullScreenImage(it, postItem.id) },
                                userPreferences = userPreferences,
                                onReportClick = { postId, reason, otherText ->
                                    viewModel.reportPost(postId, reason, otherText, onSuccess = {
                                        Log.d("NewsFeedScreen", "Post reported successfully")
                                    }, onFailure = { error ->
                                        Log.e("NewsFeedScreen", "Failed to report post: $error")
                                    })
                                },
                                onShowRepostScreen = onShowRepostScreen,
                                onUserActionClick = { userId, action, otherText ->
                                    if (action == "Report User") viewModel.reportUser(userId, action, otherText)
                                }
                            )
                        } else {
                            Log.d("NewsFeedScreen", "Rendering post ID: ${postItem.id} (original), Content: ${postItem.content}")
                            PostCard(
                                post = postItem.copy(isLiked = isLiked, like_count = likeCount),
                                navController = navController,
                                viewModel = viewModel,
                                onCommentClick = { selectedPostId = postItem.id; showBottomSheet = true },
                                onLikeClick = { liked ->
                                    if (liked) viewModel.toggleLike(postItem.id, postItem.user_id) else viewModel.unlikePost(postItem.id)
                                },
                                onShowFullScreenImage = { onShowFullScreenImage(it, postItem.id) },
                                userPreferences = userPreferences,
                                onReportClick = { postId, reason, otherText ->
                                    viewModel.reportPost(postId, reason, otherText, onSuccess = {
                                        Log.d("NewsFeedScreen", "Post reported successfully")
                                    }, onFailure = { error ->
                                        Log.e("NewsFeedScreen", "Failed to report post: $error")
                                    })
                                },
                                onShowRepostScreen = onShowRepostScreen,
                                onUserActionClick = { userId, action, otherText ->
                                    if (action == "Report User") viewModel.reportUser(userId, action, otherText)
                                }
                            )
                        }
                    }
                }

                posts.apply {
                    when (loadState.refresh) {
                        is LoadState.Loading -> item {
                            Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) {
                                CircularProgressIndicator()
                            }
                            isRefreshing = false // Reset refreshing state
                            Log.d("NewsFeedScreen", "Refresh state: Loading")
                        }
                        is LoadState.Error -> item {
                            Text(
                                "Error: ${(loadState.refresh as LoadState.Error).error.message}",
                                color = Color.Red,
                                modifier = Modifier.padding(16.dp)
                            )
                            isRefreshing = false
                            Log.e("NewsFeedScreen", "Refresh state: Error - ${(loadState.refresh as LoadState.Error).error.message}")
                        }
                        is LoadState.NotLoading -> {
                            isRefreshing = false
                            if (posts.itemCount == 0) item {
                                Text("No posts available", color = Color.Gray, modifier = Modifier.padding(16.dp))
                                Log.d("NewsFeedScreen", "Refresh state: NotLoading, no posts available")
                            }
                        }
                    }
                    when (loadState.append) {
                        is LoadState.Loading -> item {
                            CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
                            Log.d("NewsFeedScreen", "Append state: Loading")
                        }
                        is LoadState.Error -> item {
                            Text("Error loading more posts", color = Color.Red, modifier = Modifier.padding(16.dp))
                            Log.e("NewsFeedScreen", "Append state: Error - ${(loadState.append as LoadState.Error).error.message}")
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}