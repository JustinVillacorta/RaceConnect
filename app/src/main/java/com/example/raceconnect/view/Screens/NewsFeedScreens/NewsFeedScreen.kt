@file:Suppress("DEPRECATION")
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.view.Screens.NewsFeedScreens.*
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.google.accompanist.swiperefresh.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    navController: NavController,
    userPreferences: UserPreferences // ✅ Remove Application, keep UserPreferences
) {
    val viewModel: NewsFeedViewModel =
        viewModel(factory = NewsFeedViewModelFactory(userPreferences)) // ✅ Pass UserPreferences only

    val posts = viewModel.posts.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val postLikes by viewModel.postLikes.collectAsState()
    val likeCounts by viewModel.likeCounts.collectAsState()
    val newPostTrigger by viewModel.newPostTrigger.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCreatePostScreen by remember { mutableStateOf(false) }
    var selectedPostId by remember { mutableStateOf<Int?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // ✅ Listen for new post events and refresh
    LaunchedEffect(newPostTrigger) {
        if (newPostTrigger) {
            viewModel.refreshPosts()
            viewModel.resetNewPostTrigger()
        }
    }

    // ✅ Show Comments Modal Bottom Sheet
    if (showBottomSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false }
        ) {
            CommentScreen(postId = selectedPostId ?: -1, navController = navController)
        }
    }

    // ✅ Show Create Post Modal Bottom Sheet
    if (showCreatePostScreen) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showCreatePostScreen = false }
        ) {
            CreatePostScreen(
                viewModel = viewModel,
                onClose = { showCreatePostScreen = false }
            )
        }
    }

    // ✅ Main UI
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { viewModel.refreshPosts() }
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
                    onAddPostClick = { showCreatePostScreen = true })
            }

            items(posts.itemCount) { index ->
                val post = posts[posts.itemCount - 1 - index]
                post?.let {
                    LaunchedEffect(it.id) {
                        viewModel.fetchPostLikes(it.id)
                    }

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
                            if (isLiked) {
                                viewModel.toggleLike(it.id, it.user_id)
                            } else {
                                viewModel.unlikePost(it.id)
                            }
                        }
                    )
                }
            }

            posts.apply {
                when (loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                        }
                    }

                    is LoadState.Error -> {
                        item {
                            Text(text = "Error loading posts", color = Color.Red)
                        }
                    }

                    is LoadState.NotLoading -> {}
                }
            }
        }
    }
}






