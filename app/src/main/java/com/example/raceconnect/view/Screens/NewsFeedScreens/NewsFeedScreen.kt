@file:Suppress("DEPRECATION")

            import android.annotation.SuppressLint
            import android.app.Application
            import android.util.Log
            import androidx.compose.foundation.background
            import androidx.compose.foundation.layout.*
            import androidx.compose.foundation.lazy.LazyColumn
            import androidx.compose.foundation.lazy.items
            import androidx.compose.foundation.shape.RoundedCornerShape
            import androidx.compose.material.icons.Icons
            import androidx.compose.material.icons.filled.*
            import androidx.compose.material3.*
            import androidx.compose.runtime.*
            import androidx.compose.ui.*
            import androidx.compose.ui.draw.clip
            import androidx.compose.ui.graphics.Color
            import androidx.compose.ui.graphics.vector.ImageVector
            import androidx.compose.ui.platform.LocalContext
            import androidx.compose.ui.text.font.FontWeight
            import androidx.compose.ui.tooling.preview.Preview
            import androidx.compose.ui.unit.dp
            import androidx.lifecycle.viewmodel.compose.viewModel
            import androidx.navigation.NavController
            import androidx.navigation.compose.*
            import androidx.paging.LoadState
            import androidx.paging.compose.collectAsLazyPagingItems
            import androidx.compose.foundation.lazy.items
            import com.example.raceconnect.datastore.UserPreferences
            import com.example.raceconnect.model.NewsFeedDataClassItem
            import com.example.raceconnect.view.Screens.NewsFeedScreens.*
            import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
            import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
            import com.google.accompanist.swiperefresh.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    navController: NavController,
    application: Application,
    userPreferences: UserPreferences
) {
    val viewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(application, userPreferences))
    val posts = viewModel.posts.collectAsLazyPagingItems()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val postLikes by viewModel.postLikes.collectAsState()
    val likeCounts by viewModel.likeCounts.collectAsState()
    val newPostTrigger by viewModel.newPostTrigger.collectAsState() // ✅ Listen for new post events

    // State to manage showing the create post modal
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showCreatePostScreen by remember { mutableStateOf(false) }

    var selectedPostId by remember { mutableStateOf<Int?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // **Trigger refresh when a new post is added**
    LaunchedEffect(newPostTrigger) {
        if (newPostTrigger) {
            viewModel.refreshPosts()
            viewModel.resetNewPostTrigger() // ✅ Reset the trigger
        }
    }

    // Show Comments Modal Bottom Sheet
    if (showBottomSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false }
        ) {
            CommentScreen(postId = selectedPostId ?: -1, navController = navController)
        }
    }

    // Show Create Post Modal Bottom Sheet
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

    // Main News Feed UI
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = { viewModel.refreshPosts() }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Add Post Section (Opens CreatePostScreen)
            item {
                AddPostSection(navController = navController, onAddPostClick = { showCreatePostScreen = true })
            }

            // Render Posts
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
                        onCommentClick = {
                            selectedPostId = it.id
                            showBottomSheet = true
                        },
                        onLikeClick = { isLiked ->
                            if (isLiked) {
                                Log.d("NewsFeedScreen", "❤️ Liking post ID: ${it.id}")
                                viewModel.toggleLike(it.id, it.user_id)
                            } else {
                                Log.d("NewsFeedScreen", "💔 Unliking post ID: ${it.id}")
                                viewModel.unlikePost(it.id)
                            }
                        }
                    )
                }
            }

            // Loading Indicator for Pagination
            posts.apply {
                when (loadState.append) {
                    is LoadState.Loading -> {
                        item {
                            CircularProgressIndicator(
                                modifier = Modifier.fillMaxWidth().padding(16.dp)
                            )
                        }
                    }
                    is LoadState.Error -> {
                        item {
                            Text(text = "Error loading posts", color = Color.Red)
                        }
                    }
                    is LoadState.NotLoading -> {
                        // No additional UI needed
                    }
                }
            }
        }
    }
}





@Composable
            fun ActionButton(icon: ImageVector, text: String) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFF8F8F8))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = text, style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray))
                }
            }

            @SuppressLint("SuspiciousIndentation")
            @Composable
            fun NewsFeedAppNavigation(application: Application, userPreferences: UserPreferences) {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "newsFeed",
                ) {
                    composable("newsFeed") {
                        NewsFeedScreen(navController, application, userPreferences)
                    }
                    composable("comments/{postId}") { backStackEntry ->
                        val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: -1
                        CommentScreen(postId = postId, navController = navController)
                    }
                    composable("profile") {
                        ProfileViewScreen(navController, context = LocalContext.current)
                    }
                    composable("createPost") {
                        CreatePostScreen(viewModel = viewModel(factory = NewsFeedViewModelFactory(application, userPreferences))) {
                            navController.popBackStack()
                        }
                    }
                }
            }

//            @Preview(showBackground = true)
//            @Composable
//            fun PreviewNewsFeedScreen() {
//                val mockPosts = listOf(
//                    NewsFeedDataClassItem(id = 1, user_id = 123, title = "Mock Title", content = "This is a mock post content.", img_url = "https://example.com/image.jpg"),
//                    NewsFeedDataClassItem(id = 2, user_id = 124, title = "Mock Title 2", content = "Another mock post.", img_url = "https://example.com/image.jpg")
//                )
//                val navController = rememberNavController()
//
//                NewsFeedScreenPreview(
//                    posts = mockPosts,
//                    isRefreshing = false,
//                    onRefresh = {},
//                    onCreatePost = {},
//                    navController = navController
//                )
//            }
//
//            @Composable
//            fun NewsFeedScreenPreview(
//                posts: List<NewsFeedDataClassItem>,
//                isRefreshing: Boolean,
//                onRefresh: () -> Unit,
//                onCreatePost: () -> Unit,
//                navController: NavController
//            ) {
//                SwipeRefresh(
//                    state = rememberSwipeRefreshState(isRefreshing),
//                    onRefresh = onRefresh
//                ) {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxSize()
//                            .padding(8.dp),
//                        verticalArrangement = Arrangement.spacedBy(8.dp)
//                    ) {
//                        item {
//                            AddPostSection(navController = navController, onAddPostClick = onCreatePost)
//                        }
//
//                        items(posts, key = { it.id }) { post ->
//                            PostCard(
//                                post = post,
//                                navController = navController,
//                                onCommentClick = {}
//                            )
//                        }
//                    }
//                }
//            }
//
//            @Preview(showBackground = true)
//            @Composable
//            fun PreviewActionButton() {
//                ActionButton(icon = Icons.Default.Image, text = "Photos")
//            }