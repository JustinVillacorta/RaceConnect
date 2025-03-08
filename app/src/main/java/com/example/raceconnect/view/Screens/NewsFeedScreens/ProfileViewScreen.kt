package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.users
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileViewScreen(
    navController: NavController,
    context: Context,
    onClose: () -> Unit
) {
    val userPreferences = remember { UserPreferences(context) }
    val user by userPreferences.user.collectAsState(initial = null)
    val viewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences))

    // Collect posts as LazyPagingItems without 'by'
    val posts = viewModel.posts.collectAsLazyPagingItems()
    val postImages by viewModel.postImages.collectAsState()

    // Fetch user's posts and images when user is available
    LaunchedEffect(user) {
        user?.id?.let { userId ->
            viewModel.getPostsByUserId(userId) // Fetch user's posts
            viewModel.refreshPosts() // Ensure fresh data
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(top = 16.dp, start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            ProfileHeaderSection(user)
            Spacer(modifier = Modifier.height(16.dp))
            ProfileTabsWithContent(user, posts, postImages)
        }
    }
}

// Rest of the code remains unchanged
@Composable
fun ProfileHeaderSection(user: users?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.Gray)
        ) {
            val profilePictureUrl = user?.profilePicture
            // Add logging to debug
            println("Profile picture URL: $profilePictureUrl")

            if (profilePictureUrl != null && profilePictureUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(profilePictureUrl),
                    contentDescription = "Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.baseline_account_circle_24),
                    contentDescription = "Default Profile Picture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = user?.username ?: "Guest User",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = user?.email ?: "No email available",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun ProfileTabsWithContent(
    user: users?,
    posts: androidx.paging.compose.LazyPagingItems<com.example.raceconnect.model.NewsFeedDataClassItem>,
    postImages: Map<Int, List<String>>
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Posts", "Photos")

    TabRow(
        selectedTabIndex = selectedTabIndex,
        containerColor = Color.White,
        contentColor = Color.Red
    ) {
        tabTitles.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = { Text(title) }
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    when (selectedTabIndex) {
        0 -> PostsSection(user?.id, posts, postImages)
        1 -> PhotosSection(user?.id, postImages)
    }
}

@Composable
fun PostsSection(
    userId: Int?,
    posts: androidx.paging.compose.LazyPagingItems<com.example.raceconnect.model.NewsFeedDataClassItem>,
    postImages: Map<Int, List<String>>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(posts.itemCount) { index ->
            posts[index]?.let { post ->
                if (post.user_id == userId) { // Filter posts by user ID
                    PostItem(
                        username = "Anonymous", // Replace with actual username if available
                        content = post.content ?: "",
                        timestamp = post.created_at ?: "Just now",
                        imageUrl = postImages[post.id]?.firstOrNull()
                    )
                }
            }
        }
    }
}

@Composable
fun PostItem(username: String, content: String, timestamp: String, imageUrl: String?) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_account_circle_24),
                        contentDescription = "User Profile",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(text = username, fontWeight = FontWeight.Bold)
                    Text(text = timestamp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = content)
            imageUrl?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Image(
                    painter = rememberAsyncImagePainter(it),
                    contentDescription = "Post Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun PhotosSection(userId: Int?, postImages: Map<Int, List<String>>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        postImages.forEach { (postId, images) ->
            images.forEach { imageUrl ->
                item {
                    Image(
                        painter = rememberAsyncImagePainter(imageUrl),
                        contentDescription = "User Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}