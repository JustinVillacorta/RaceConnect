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
import com.example.raceconnect.view.Screens.NewsFeedScreens.PostCard

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
    var isRefreshing by remember { mutableStateOf(false) } // Local state for refresh control
    val postLikes by viewModel.postLikes.collectAsState()
    val likeCounts by viewModel.likeCounts.collectAsState()
    val newPostTriggerState by viewModel.newPostTrigger.collectAsState()
    val user by userPreferences.user.collectAsState(initial = null)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPostId by remember { mutableStateOf<Int?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Log user ID for debugging
    LaunchedEffect(user?.id) {
        Log.d("NewsFeedScreen", "Logged-in user ID: ${user?.id}")
    }

    // Trigger initial refresh
    LaunchedEffect(Unit) {
        if (!viewModel.isInitialRefreshDone) {
            isRefreshing = true
            viewModel.refreshPosts()
            posts.refresh()
            viewModel.isInitialRefreshDone = true
        }
    }

    // Handle new post trigger
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
                        style = androidx.compose.material3.MaterialTheme.typography.headlineMedium,
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
                    val post = posts[index] // Latest first (no reversal)
                    post?.let {
                        LaunchedEffect(it.id) {
                            viewModel.fetchPostLikes(it.id)
                            Log.d("NewsFeedScreen", "Post ID: ${it.id}, Created At: ${it.created_at}, Content: ${it.content}")
                        }

                        // Directly call PostCard without nesting a @Composable function
                        PostCard(
                            post = it.copy(
                                isLiked = postLikes[it.id] ?: false,
                                like_count = likeCounts[it.id] ?: it.like_count
                            ),
                            navController = navController,
                            viewModel = viewModel,
                            onCommentClick = {
                                selectedPostId = it.id
                                showBottomSheet = true
                            },
                            onLikeClick = { isLiked ->
                                if (isLiked) viewModel.toggleLike(it.id, it.user_id)
                                else viewModel.unlikePost(it.id)
                            },
                            onShowFullScreenImage = { imageUrl -> onShowFullScreenImage(imageUrl, it.id) },
                            userPreferences = userPreferences,
                            onReportClick = { viewModel.reportPost(it.id) },
                            onShowRepostScreen = onShowRepostScreen
                        )
                    }
                }

                // Handle initial and append load states
                posts.apply {
                    when (loadState.refresh) {
                        is LoadState.Loading -> {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }

                        is LoadState.Error -> {
                            item {
                                Text(
                                    text = "Error loading posts: ${(loadState.refresh as LoadState.Error).error.message}",
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
                            // Stop refreshing when loading is complete
                            /**LaunchedEffect(Unit) {
                                isRefreshing = false
                            }**/
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
                                Text(text = "Error loading more posts", color = Color.Red, modifier = Modifier.padding(16.dp))
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }
}