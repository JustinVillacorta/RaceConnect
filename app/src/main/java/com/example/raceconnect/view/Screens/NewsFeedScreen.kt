import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.raceconnect.model.NewsFeedDataClassItem
import androidx.compose.runtime.collectAsState

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.model.Comment
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    navController: NavController,
    viewModel: NewsFeedViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showCreatePostScreen by remember { mutableStateOf(false) }

    // Bottom sheet state for comment section
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedPostId by remember { mutableStateOf<Int?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // Show comment section when bottom sheet is triggered
    if (showBottomSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = { showBottomSheet = false }
        ) {
            CommentScreen(postId = selectedPostId ?: -1)
        }
    }

    if (showCreatePostScreen) {
        CreatePostScreen(
            viewModel = viewModel,
            onClose = { showCreatePostScreen = false }
        )
    } else {
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
                item {
                    AddPostSection { showCreatePostScreen = true }
                }

                items(posts, key = { it.id }) { post ->
                    PostCard(
                        post = post,
                        navController = navController,
                        onCommentClick = {
                            selectedPostId = post.id
                            showBottomSheet = true
                        }
                    )
                }
            }
        }
    }
}






@Composable
fun AddPostSection(onAddPostClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onAddPostClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture (Placeholder Image)
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(40.dp)
                .padding(4.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Placeholder styled as the design
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF8F8F8))
                .border(
                    width = 1.dp,
                    color = Color.LightGray,
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable(onClick = onAddPostClick),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = "What's new today?",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                modifier = Modifier.padding(start = 16.dp)
            )
        }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(viewModel: NewsFeedViewModel, onClose: () -> Unit) {
    var postText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Create post", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            if (postText.isNotEmpty()) {
                                viewModel.addPost(postText) // Call ViewModel to handle post creation
                                onClose()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD50000)) // Red button
                    ) {
                        Text("Publish", color = Color.White)
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // User Profile Section
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    // Profile Picture Placeholder
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "testing", // Static name for now
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Text Field for Post
                OutlinedTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    placeholder = { Text("What's new today?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )

                // Action Buttons (Photos and Reels)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionButton(icon = Icons.Default.Image, text = "Photos")
                    ActionButton(icon = Icons.Default.Videocam, text = "Reels")
                }
            }
        }
    )
}

@Composable
fun ActionButton(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8F8F8)) // Light background
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


@Composable
fun NewsFeedAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "newsFeed") {
        composable("newsFeed") {
            NewsFeedScreen(navController)
        }
        composable("comments/{postId}") { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: -1
            CommentScreen(postId = postId)
        }
    }
}




@Composable
fun PostCard(post: NewsFeedDataClassItem, navController: NavController, onCommentClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Profile Section
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Anonymous",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Just now",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Post Content
            Text(
                text = post.content ?: "No content available.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reaction Bar (including comment icon to trigger bottom sheet)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ReactionIcon(icon = Icons.Default.Favorite)
                Spacer(modifier = Modifier.width(12.dp))
                ReactionIcon(icon = Icons.Default.ChatBubble, onClick = onCommentClick) // Opens bottom sheet
                Spacer(modifier = Modifier.width(12.dp))
                ReactionIcon(icon = Icons.Default.Repeat)
            }
        }
    }
}


@Composable
fun ReactionIcon(icon: ImageVector, onClick: (() -> Unit)? = null) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier
            .size(20.dp)
            .clickable { onClick?.invoke() },
        tint = Color.Gray
    )
}



@Composable
fun CommentScreen(postId: Int) {
    var commentText by remember { mutableStateOf("") }
    val comments = remember {
        mutableStateListOf(
            Comment("John Cena", "Where?", "25m", 4, Icons.Default.Favorite),
            Comment("Jennifer Lawrence", "You're not funny.", "14m", 10, Icons.Default.Favorite),
            Comment("Trish Alexa", "Wow! Congrats!", "1h", 1, Icons.Default.ThumbUp),
            Comment("Jake Jordan Gyllenhaal", "Yesss! You deserve it!", "1h", 1, Icons.Default.ThumbUp)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Comments List
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(comments) { comment ->
                CommentItem(comment)
            }
        }

        // Add Comment Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = commentText,
                onValueChange = { commentText = it },
                placeholder = { Text("Add a comment...") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = {
                if (commentText.isNotEmpty()) {
                    comments.add(Comment("You", commentText, "Just now", 0, Icons.Default.ThumbUp))
                    commentText = ""
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}


@Composable
fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture Placeholder
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.username,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = comment.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = comment.icon,
                contentDescription = "Reaction Icon",
                modifier = Modifier.size(20.dp),
                tint = Color.Gray
            )
            Text(
                text = comment.likes.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}









@Preview(showBackground = true)
@Composable
fun PreviewNewsFeedScreen() {
    val mockPosts = listOf(
        NewsFeedDataClassItem(id = 1, content = "Hello from preview!"),
        NewsFeedDataClassItem(id = 2, content = "Another preview post!")
    )
    val navController = rememberNavController()

    NewsFeedScreenPreview(
        posts = mockPosts,
        isRefreshing = false,
        onRefresh = {},
        onCreatePost = {},
        navController = navController
    )
}

@Composable
fun NewsFeedScreenPreview(
    posts: List<NewsFeedDataClassItem>,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onCreatePost: () -> Unit,
    navController: NavController
) {
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = onRefresh
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                AddPostSection(onAddPostClick = onCreatePost)
            }

            items(posts, key = { it.id }) { post ->
                PostCard(
                    post = post,
                    navController = navController,
                    onCommentClick = {} // Default empty lambda to fix the error
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun PreviewAddPostSection() {
    AddPostSection(onAddPostClick = {})
}







@Preview(showBackground = true)
@Composable
fun PreviewPostCard() {
    val navController = rememberNavController()
    val mockPost = NewsFeedDataClassItem(id = 1, content = "This is a mock post content.")

    PostCard(
        post = mockPost,
        navController = navController,
        onCommentClick = {}
    )
}


@Preview(showBackground = true)
@Composable
fun PreviewCommentScreen() {
    CommentScreen(postId = 1)
}



@Preview(showBackground = true)
@Composable
fun PreviewCommentItem() {
    val mockComment = Comment(
        username = "Mock User",
        text = "This is a mock comment.",
        timestamp = "5m ago",
        likes = 3,
        icon = Icons.Default.Favorite
    )
    CommentItem(comment = mockComment)
}



@Preview(showBackground = true)
@Composable
fun PreviewActionButton() {
    ActionButton(icon = Icons.Default.Image, text = "Photos")
}

