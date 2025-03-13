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
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
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
    val profileDetailsViewModel: ProfileDetailsViewModel = viewModel(factory = ProfileDetailsViewModelFactory(userPreferences))
    val profileData by profileDetailsViewModel.profileData.collectAsState(initial = null)
    val newsFeedViewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences, context))
    val userId = profileData?.id
    val postsFlow = userId?.let { newsFeedViewModel.getPostsByUserId(it) }
    val posts = postsFlow?.collectAsLazyPagingItems()
    val postImages by newsFeedViewModel.postImages.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("UserProfileScreen", "Loading profile data")
        profileDetailsViewModel.loadProfileData()
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    ) {
                        val profilePictureUrl = profileData?.profilePicture
                        Log.d("UserProfileScreen", "Profile picture URL: $profilePictureUrl")

                        val painter = if (profilePictureUrl != null && profilePictureUrl.isNotEmpty()) {
                            Log.d("UserProfileScreen", "Loading profile picture from URL: $profilePictureUrl")
                            rememberAsyncImagePainter(
                                model = profilePictureUrl,
                                onLoading = { Log.d("UserProfileScreen", "Loading profile picture...") },
                                onSuccess = { Log.d("UserProfileScreen", "Profile picture loaded successfully") },
                                onError = { error ->
                                    Log.e("UserProfileScreen", "Error loading profile picture: ${error.result.throwable.message}")
                                }
                            )
                        } else {
                            Log.d("UserProfileScreen", "Using default profile picture because URL is null or empty")
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
                        posts?.let { pagingItems ->
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(pagingItems.itemCount) { index ->
                                    pagingItems[index]?.let { post ->
                                        if (post.user_id == profileData?.id) {
                                            LaunchedEffect(post.id) {
                                                newsFeedViewModel.getPostImages(post.id)
                                            }
                                            Log.d("UserProfileScreen", "Processing post with id: ${post.id}, user_id: ${post.user_id}, image URLs: ${postImages[post.id]}")
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
                                                        Log.d("UserProfileScreen", "Attempting to load image from URL: $url")
                                                        val painter = rememberAsyncImagePainter(
                                                            model = url,
                                                            onLoading = { Log.d("UserProfileScreen", "Loading post image...") },
                                                            onSuccess = { Log.d("UserProfileScreen", "Post image loaded successfully") },
                                                            onError = { error ->
                                                                Log.e("UserProfileScreen", "Error loading post image: ${error.result.throwable.message}")
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
                                                    } ?: Log.w("UserProfileScreen", "No image URL available for post id: ${post.id}")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } ?: CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    1 -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            postImages.forEach { (postId, images) ->
                                images.forEach { imageUrl ->
                                    Log.d("UserProfileScreen", "Loading photo for postId: $postId, URL: $imageUrl")
                                    item {
                                        val painter = rememberAsyncImagePainter(
                                            model = imageUrl,
                                            onLoading = { Log.d("UserProfileScreen", "Loading photo image...") },
                                            onSuccess = { Log.d("UserProfileScreen", "Photo image loaded successfully") },
                                            onError = { error ->
                                                Log.e("UserProfileScreen", "Error loading photo image: ${error.result.throwable.message}")
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