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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun NewsFeedScreen(viewModel: NewsFeedViewModel = viewModel()) {
    val posts by viewModel.posts.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()

    var showCreatePostScreen by remember { mutableStateOf(false) }

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
                    PostCard(post = post)
                }
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
fun CreatePostScreen(viewModel: NewsFeedViewModel, onClose: () -> Unit) {
    var postText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create post") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Button(onClick = {
                        if (postText.isNotEmpty()) {
                            viewModel.addPost(postText) // Call ViewModel to handle post creation
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Safeguard against null values
            Text(
                text = post.title ?: "Untitled Post",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = post.content ?: "No content available.",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}




