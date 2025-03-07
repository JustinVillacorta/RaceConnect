// NewsFeedScreen.kt
@file:Suppress("DEPRECATION")
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.view.Screens.NewsFeedScreens.AddPostSection
import com.example.raceconnect.view.Screens.NewsFeedScreens.CommentSectionScreen // Updated import
import com.example.raceconnect.view.Screens.NewsFeedScreens.CreatePostScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.PostCard
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.google.accompanist.swiperefresh.*

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
    val posts = viewModel.posts.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val postLikes by viewModel.postLikes.collectAsState()
    val likeCounts by viewModel.likeCounts.collectAsState()
    val newPostTrigger by viewModel.newPostTrigger.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPostId by remember { mutableStateOf<Int?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(newPostTrigger) {
        if (newPostTrigger) {
            viewModel.refreshPosts()
            viewModel.resetNewPostTrigger()
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
                userPreferences = userPreferences, // Added userPreferences parameter
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
            onRefresh = { viewModel.refreshPosts() },
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
                                if (isLiked) viewModel.toggleLike(it.id, it.user_id)
                                else viewModel.unlikePost(it.id)
                            },
                            onShowFullScreenImage = { imageUrl -> onShowFullScreenImage(imageUrl, it.id) },
                            onShowProfileView = onShowProfileView,
                            onReportClick = { viewModel.reportPost(it.id) },
                            onShowRepostScreen = onShowRepostScreen
                        )
                    }
                }

                posts.apply {
                    when (loadState.append) {
                        is LoadState.Loading -> {
                            item { CircularProgressIndicator(modifier = Modifier.fillMaxWidth().padding(16.dp)) }
                        }
                        is LoadState.Error -> {
                            item { Text(text = "Error loading posts", color = Color.Red) }
                        }
                        is LoadState.NotLoading -> {}
                    }
                }
            }
        }
    }
}