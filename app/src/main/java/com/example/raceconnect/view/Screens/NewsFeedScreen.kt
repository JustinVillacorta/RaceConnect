import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun NewsFeedScreen(viewModel: NewsFeedViewModel = viewModel()) {
    val posts by remember { derivedStateOf { viewModel.posts } }
    var showCreatePostScreen by remember { mutableStateOf(false) }

    if (showCreatePostScreen) {
        CreatePostScreen(
            onClose = { showCreatePostScreen = false },
            onPost = { postText ->
                viewModel.addPost(
                    NewsFeedDataClassItem(
                        id = 0, // Backend will generate ID
                        user_id = 1, // Example user
                        title = "You",
                        content = postText,
                        img_url = "",
                        like_count = 0,
                        comment_count = 0,
                        repost_count = 0,
                        type = "text",
                        created_at = "",
                        updated_at = ""
                    )
                )
                showCreatePostScreen = false
            }
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { AddPostSection { showCreatePostScreen = true } }
            items(posts.size) { index ->
                PostCard(post = posts[index])
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
        // Profile Picture (Placeholder Image)
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(40.dp)
                .padding(4.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Disabled Text Field for Placeholder
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("What's new today?") },
            enabled = false, // Disable it to make it act like a button
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                unfocusedBorderColor = Color.LightGray,
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
                            onPost(postText) // Trigger ViewModel function
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
                OutlinedTextField(
                    value = postText,
                    onValueChange = { postText = it },
                    placeholder = { Text("What's on your mind?") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}





@Composable
fun PostCard(post: NewsFeedDataClassItem) {
    Card(
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier.fillMaxWidth().padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = post.title, style = MaterialTheme.typography.bodyMedium)
            Text(text = post.content, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}




