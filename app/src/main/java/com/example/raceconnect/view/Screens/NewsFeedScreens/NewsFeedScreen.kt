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
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

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
    val viewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences))
    val posts = viewModel.postsFlow.collectAsLazyPagingItems()
    var isRefreshing by remember { mutableStateOf(false) }
    val postLikes by viewModel.postLikes.collectAsState()
    val likeCounts by viewModel.likeCounts.collectAsState()
    val newPostTriggerState by viewModel.newPostTrigger.collectAsState()
    val user by userPreferences.user.collectAsState(initial = null)
    val reposts by viewModel.reposts.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPostId by remember { mutableStateOf<Int?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Initial refresh and user ID logging
    LaunchedEffect(Unit) {
        if (!viewModel.isInitialRefreshDone) {
            isRefreshing = true
            viewModel.refreshPosts()
            posts.refresh()
            viewModel.isInitialRefreshDone = true
            Log.d("NewsFeedScreen", "Initial refresh triggered")
        }
    }

    LaunchedEffect(user?.id) {
        Log.d("NewsFeedScreen", "Logged-in user ID: ${user?.id}")
    }

    LaunchedEffect(newPostTriggerState) {
        if (newPostTriggerState) {
            isRefreshing = true
            viewModel.refreshPosts()
            posts.refresh()
            viewModel.resetNewPostTrigger()
            Log.d("NewsFeedScreen", "Refreshed due to new post trigger")
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
                onShowProfileView = {
                    onShowProfileView()
                    showBottomSheet = false
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "RaceConnect",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { /* Handle search click */ }) {
                        Icon(
                            painter = painterResource(id = com.example.raceconnect.R.drawable.baseline_search_24),
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Red,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing),
            onRefresh = {
                isRefreshing = true
                viewModel.refreshPosts()
                posts.refresh()
                Log.d("NewsFeedScreen", "Swipe-to-refresh triggered")
            },
            modifier = Modifier.padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    AddPostSection(
                        navController = navController,
                        onAddPostClick = onShowCreatePost,
                        onShowProfileView = onShowProfileView
                    )
                }

                items(posts.itemCount) { index ->
                    val post = posts[index]
                    post?.let { postItem ->
                        // Fetch likes and reposts only once per post ID
                        LaunchedEffect(postItem.id) {
                            viewModel.fetchPostLikes(postItem.id)
                            viewModel.fetchReposts(postItem.id)
                        }

                        // Log post details for debugging
                        Log.d("NewsFeedScreen", "Rendering Post ID: ${postItem.id}, IsRepost: ${postItem.isRepost}, OriginalPostId: ${postItem.original_post_id}")

                        // Render original post
                        PostCard(
                            post = postItem.copy(
                                isLiked = postLikes[postItem.id] ?: false,
                                like_count = likeCounts[postItem.id] ?: postItem.like_count
                            ),
                            navController = navController,
                            viewModel = viewModel,
                            onCommentClick = {
                                selectedPostId = postItem.id
                                showBottomSheet = true
                                Log.d("NewsFeedScreen", "Comment clicked for post ${postItem.id}")
                            },
                            onLikeClick = { isLiked ->
                                if (isLiked) viewModel.toggleLike(postItem.id, postItem.user_id)
                                else viewModel.unlikePost(postItem.id)
                                Log.d("NewsFeedScreen", "Like toggled for post ${postItem.id} to $isLiked")
                            },
                            onShowFullScreenImage = { imageUrl ->
                                onShowFullScreenImage(imageUrl, postItem.id)
                            },
                            userPreferences = userPreferences,
                            onReportClick = {
                                viewModel.reportPost(postItem.id)
                                Log.d("NewsFeedScreen", "Report clicked for post ${postItem.id}")
                            },
                            onShowRepostScreen = onShowRepostScreen
                        )

                        // Render reposts efficiently
                        val repostList = reposts[postItem.id] ?: emptyList()
                        repostList.forEach { repost ->
                            val repostItem = remember(repost.id) {
                                NewsFeedDataClassItem(
                                    id = repost.id,
                                    user_id = repost.userId,
                                    content = repost.quote ?: "",
                                    created_at = repost.createdAt,
                                    isRepost = true,
                                    original_post_id = postItem.id,
                                    like_count = 0, // Fetch if needed
                                    comment_count = 0,
                                    repost_count = 0,
                                    category = postItem.category,
                                    privacy = postItem.privacy,
                                    type = postItem.type,
                                    postType = postItem.postType,
                                    title = postItem.title
                                )
                            }
                            RepostCard(
                                repost = repostItem.copy(
                                    isLiked = postLikes[repostItem.id] ?: false,
                                    like_count = likeCounts[repostItem.id] ?: repostItem.like_count
                                ),
                                originalPost = postItem,
                                navController = navController,
                                viewModel = viewModel,
                                onCommentClick = {
                                    selectedPostId = repostItem.id
                                    showBottomSheet = true
                                    Log.d("NewsFeedScreen", "Comment clicked for repost ${repostItem.id}")
                                },
                                onLikeClick = { isLiked ->
                                    if (isLiked) viewModel.toggleLike(repostItem.id, repostItem.user_id)
                                    else viewModel.unlikePost(repostItem.id)
                                    Log.d("NewsFeedScreen", "Like toggled for repost ${repostItem.id} to $isLiked")
                                },
                                onShowFullScreenImage = { imageUrl ->
                                    onShowFullScreenImage(imageUrl, repostItem.id)
                                },
                                userPreferences = userPreferences,
                                onReportClick = {
                                    viewModel.reportPost(repostItem.id)
                                    Log.d("NewsFeedScreen", "Report clicked for repost ${repostItem.id}")
                                },
                                onShowRepostScreen = onShowRepostScreen,
                                reposts = reposts
                            )
                        }
                    }
                }

                // Loading and error states
                posts.apply {
                    when (loadState.refresh) {
                        is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is LoadState.Error -> {
                            item {
                                Text(
                                    text = "Error: ${(loadState.refresh as LoadState.Error).error.message}",
                                    color = Color.Red,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        is LoadState.NotLoading -> {
                            if (posts.itemCount == 0) {
                                item {
                                    Text(
                                        text = "No posts available",
                                        color = Color.Gray,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    when (loadState.append) {
                        is LoadState.Loading -> {
                            item {
                                CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp))
                            }
                        }
                        is LoadState.Error -> {
                            item {
                                Text(
                                    text = "Error loading more posts",
                                    color = Color.Red,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}