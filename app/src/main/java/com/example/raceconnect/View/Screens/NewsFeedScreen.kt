import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.raceconnect.Model.Post
import com.example.raceconnect.R


@Composable
fun NewsFeedScreen() {
    val posts = remember {
        mutableStateListOf(
            Post(
                title = "Juniffer Lawrence",
                description = "My new car is here! :))",
                images = listOf(R.drawable.baseline_account_circle_24, R.drawable.baseline_account_circle_24),
                likeCount = 100,
                commentCount = 27,
                shareCount = 18
            ),
            Post(
                title = "Dana Wheat",
                description = "Green Lambo Urus <333",
                images = listOf(R.drawable.img),
                likeCount = 107,
                commentCount = 35,
                shareCount = 12
            )
        )
    }

    var showCreatePostScreen by remember { mutableStateOf(false) }

    if (showCreatePostScreen) {
        CreatePostScreen(
            onClose = { showCreatePostScreen = false },
            onPost = { postText ->
                posts.add(
                    Post(
                        title = "You",
                        description = postText,
                        images = emptyList(),
                        likeCount = 0,
                        commentCount = 0,
                        shareCount = 0
                    )
                )
                showCreatePostScreen = false
            }
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // AddPostSection as the first item
            item {
                AddPostSection(
                    onAddPostClick = { showCreatePostScreen = true }
                )
            }

            // Render posts
            items(posts.size) { index ->
                PostCard(
                    post = posts[index],
                    onLike = {
                        posts[index] = posts[index].copy(likeCount = posts[index].likeCount + 1)
                    },
                    onComment = { /* Handle comment action */ },
                    onShare = { /* Handle share action */ }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPostSection(onAddPostClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onAddPostClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Picture
        Image(
            painter = painterResource(id = R.drawable.baseline_account_circle_24), // Replace with your drawable
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Text Input Placeholder
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text(text = "What's new today?") },
            enabled = false, // Disable the text field, make it clickable as a whole
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                disabledBorderColor = Color.LightGray,
                disabledTextColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(onClose: () -> Unit, onPost: (String) -> Unit) {
    var postText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create post") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = {
                        if (postText.isNotEmpty()) {
                            onPost(postText)
                            onClose()
                        }
                    }) {
                        Text("Post")
                    }
                }
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // User Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_account_circle_24), // Replace with your drawable
                        contentDescription = "User Profile Picture",
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(25.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Your Name",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Public", // Replace with post privacy if needed
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                // Input Field
                OutlinedTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    placeholder = { Text("What's on your mind?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedBorderColor = Color.LightGray,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Options List (Photo/Video, Tag People, etc.)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PostOptionItem(icon = painterResource(id = R.drawable.baseline_ondemand_video_24), label = "Photo/Video") {
                        // Handle photo/video selection
                    }

                }
            }
        }
    )
}

@Composable
fun PostOptionItem(icon: Painter, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = label,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}




@Composable
fun PostCard(post: Post, onLike: () -> Unit, onComment: () -> Unit, onShare: () -> Unit) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Info Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_account_circle_24), // Replace with user's profile picture
                    contentDescription = "User Profile Picture",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(20.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Text(
                        text = "7h", // Replace with the actual timestamp
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* Handle more options */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Post Description
            Text(
                text = post.description ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Slidable Images in LazyRow
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(post.images.size) { imageIndex ->
                    Image(
                        painter = painterResource(id = post.images[imageIndex]),
                        contentDescription = "Post Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1.5f)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Action Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Like
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLike) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Like",
                            tint = Color.Red
                        )
                    }
                    Text(text = "${post.likeCount}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Comment
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onComment) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_mode_comment_24),
                            contentDescription = "Comment",
                            tint = Color.Gray
                        )
                    }
                    Text(text = "${post.commentCount}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Share
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onShare) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.Gray
                        )
                    }
                    Text(text = "${post.shareCount}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
