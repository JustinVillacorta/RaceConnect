package com.example.raceconnect.view.Screens.MenuScreens

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
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.example.raceconnect.viewmodel.ProfileDetails.PostUserProfileViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.PostUserProfileViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostUserProfileViewScreen(
    navController: NavController,
    context: Context,
    userId: Int,
    onClose: () -> Unit
) {
    val userPreferences = UserPreferences(context)
    val viewModel = viewModel<PostUserProfileViewModel>(
        factory = PostUserProfileViewModelFactory(userPreferences)
    )
    val newsFeedViewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences))
    val profileData by viewModel.profileData.collectAsState()
    val posts = newsFeedViewModel.getPostsByUserId(userId).collectAsLazyPagingItems()
    val postImages by newsFeedViewModel.postImages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(userId) {
        Log.d("PostUserProfileViewScreen", "Loading data for userId: $userId")
        viewModel.loadProfileData(userId)
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

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else if (profileData == null && errorMessage != null) {
                Text(
                    text = errorMessage ?: "Unknown error",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else if (profileData != null) {
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
                        val profilePictureUrl = profileData?.profilePicture
                        Log.d("PostUserProfileViewScreen", "Profile picture URL: $profilePictureUrl")

                        val painter = if (profilePictureUrl != null && profilePictureUrl.isNotEmpty()) {
                            Log.d("PostUserProfileViewScreen", "Loading profile picture from URL: $profilePictureUrl")
                            rememberAsyncImagePainter(
                                model = profilePictureUrl,
                                onLoading = { Log.d("PostUserProfileViewScreen", "Loading profile picture...") },
                                onSuccess = { Log.d("PostUserProfileViewScreen", "Profile picture loaded successfully") },
                                onError = { error ->
                                    Log.e("PostUserProfileViewScreen", "Error loading profile picture: ${error.result.throwable.message}")
                                }
                            )
                        } else {
                            Log.d("PostUserProfileViewScreen", "Using default profile picture because URL is null or empty")
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
                        text = profileData?.username ?: "Guest User",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = profileData?.email ?: "No email available",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    0 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(posts.itemCount) { index ->
                                posts[index]?.let { post ->
                                    if (post.user_id == userId) {
                                        LaunchedEffect(post.id) {
                                            newsFeedViewModel.getPostImages(post.id)
                                        }
                                        Log.d("PostUserProfileViewScreen", "Processing post with id: ${post.id}, user_id: ${post.user_id}, image URLs: ${postImages[post.id]}")
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
                                                        Text(text = post.username ?: "Anonymous", fontWeight = FontWeight.Bold)
                                                        Text(text = post.created_at ?: "Just now", color = Color.Gray)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(text = post.content ?: "")
                                                val imageUrl = postImages[post.id]?.firstOrNull()
                                                imageUrl?.let { url ->
                                                    Log.d("PostUserProfileViewScreen", "Attempting to load image from URL: $url")
                                                    val painter = rememberAsyncImagePainter(
                                                        model = url,
                                                        onLoading = { Log.d("PostUserProfileViewScreen", "Loading post image...") },
                                                        onSuccess = { Log.d("PostUserProfileViewScreen", "Post image loaded successfully") },
                                                        onError = { error ->
                                                            Log.e("PostUserProfileViewScreen", "Error loading post image: ${error.result.throwable.message}")
                                                        }
                                                    )
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Image(
                                                        painter = painter,
                                                        contentDescription = "Post Image",
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .height(200.dp)
                                                            .clip(RoundedCornerShape(8.dp)),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                } ?: Log.w("PostUserProfileViewScreen", "No image URL available for post id: ${post.id}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    1 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            postImages.forEach { (postId, images) ->
                                images.forEach { imageUrl ->
                                    Log.d("PostUserProfileViewScreen", "Loading photo for postId: $postId, URL: $imageUrl")
                                    item {
                                        val painter = rememberAsyncImagePainter(
                                            model = imageUrl,
                                            onLoading = { Log.d("PostUserProfileViewScreen", "Loading photo image...") },
                                            onSuccess = { Log.d("PostUserProfileViewScreen", "Photo image loaded successfully") },
                                            onError = { error ->
                                                Log.e("PostUserProfileViewScreen", "Error loading photo image: ${error.result.throwable.message}")
                                            }
                                        )
                                        Image(
                                            painter = painter,
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
                }
            }
        }
    }
}