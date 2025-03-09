package com.example.raceconnect.view.Screens.NewsFeedScreens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel.MenuViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel.MenuViewModelFactory
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    navController: NavController,
    context: Context,
    onClose: () -> Unit
) {
    val userPreferences = remember { UserPreferences(context) }
    val menuViewModel: MenuViewModel = viewModel(factory = MenuViewModelFactory(userPreferences))
    val profileDetailsViewModel: ProfileDetailsViewModel = viewModel(factory = ProfileDetailsViewModelFactory(userPreferences))
    val profileData by profileDetailsViewModel.profileData.collectAsState(initial = null) // Use ProfileDetailsViewModel
    val newsFeedViewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences))

    val posts = newsFeedViewModel.posts.collectAsLazyPagingItems()
    val postImages by newsFeedViewModel.postImages.collectAsState()

    // Load profile details when screen is first composed
    LaunchedEffect(Unit) {
        Log.d("UserProfileScreen", "Loading profile data")
        profileDetailsViewModel.loadProfileData() // Load detailed profile data
        profileData?.id?.let { userId ->
            newsFeedViewModel.getPostsByUserId(userId)
            newsFeedViewModel.refreshPosts()
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

            if (profileData == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                ProfileHeaderSection(profileData)
                Spacer(modifier = Modifier.height(16.dp))
                ProfileTabsWithContent(profileData, posts, postImages)
            }
        }
    }
}

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
            Log.d("ProfileHeaderSection", "Profile picture URL: $profilePictureUrl")

            val painter = if (profilePictureUrl != null && profilePictureUrl.isNotEmpty()) {
                Log.d("ProfileHeaderSection", "Loading profile picture from URL: $profilePictureUrl")
                rememberAsyncImagePainter(
                    model = profilePictureUrl,
                    onLoading = { Log.d("ProfileHeaderSection", "Loading profile picture...") },
                    onSuccess = { Log.d("ProfileHeaderSection", "Profile picture loaded successfully") },
                    onError = { error ->
                        Log.e("ProfileHeaderSection", "Error loading profile picture: ${error.result.throwable.message}")
                    }
                )
            } else {
                Log.d("ProfileHeaderSection", "Using default profile picture because URL is null or empty")
                painterResource(id = R.drawable.baseline_account_circle_24)
            }

            Image(
                painter = painter,
                contentDescription = "Profile Picture",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
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
    posts: androidx.paging.compose.LazyPagingItems<NewsFeedDataClassItem>,
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
    posts: androidx.paging.compose.LazyPagingItems<NewsFeedDataClassItem>,
    postImages: Map<Int, List<String>>
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(posts.itemCount) { index ->
            posts[index]?.let { post ->
                if (post.user_id == userId) {
                    PostItem(
                        username = "Anonymous", // Use post.username
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