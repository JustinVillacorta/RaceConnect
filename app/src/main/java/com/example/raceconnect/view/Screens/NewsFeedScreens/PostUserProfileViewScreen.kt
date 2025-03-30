package com.example.raceconnect.view.Screens.MenuScreens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.ProfileRepostsDataClass
import com.example.raceconnect.model.Repost
import com.example.raceconnect.view.Screens.MenuScreens.ProfileView.PhotosSection
import com.example.raceconnect.view.Screens.MenuScreens.ProfileView.PostsSection
import com.example.raceconnect.view.Screens.MenuScreens.ProfileView.RepostsSection
import com.example.raceconnect.view.Screens.NewsFeedScreens.formatTime
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.example.raceconnect.viewmodel.ProfileDetails.PostUserProfileViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.PostUserProfileViewModelFactory
import java.util.*

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
    val newsFeedViewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences, context))
    val profileData by viewModel.profileData.collectAsState()
    val posts = newsFeedViewModel.getPostsByUserId(userId).collectAsLazyPagingItems()
    val postImages by newsFeedViewModel.postImages.collectAsState()
    val userReposts by newsFeedViewModel.userReposts.collectAsState()
    val profileOriginalPosts by newsFeedViewModel.profileOriginalPosts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(userId) {
        Log.d("PostUserProfileViewScreen", "Loading data for userId: $userId")
        viewModel.loadProfileData(userId)
        newsFeedViewModel.fetchUserReposts(userId)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
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
                            .size(150.dp)
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
                val tabTitles = listOf("Posts", "Reposts", "Photos")

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
                    0 -> { // Posts Tab
                        PostsSection(
                            posts = posts,
                            postImages = postImages,
                            onEditPost = { /* No-op for other user's profile */ },
                            onDeletePost = { /* No-op for other user's profile */ },
                            onFetchPostImages = { postId -> newsFeedViewModel.getPostImages(postId) },
                            navController = navController,
                            showDropdown = false // Disable dropdown for other user's profile
                        )
                    }
                    1 -> { // Reposts Tab
                        RepostsSection(
                            userReposts = userReposts,
                            postImages = postImages,
                            profileOriginalPosts = profileOriginalPosts,
                            profileUsername = profileData?.username,
                            profileUserId = userId,
                            profilePicture = profileData?.profilePicture, // Pass profilePicture here
                            onFetchPostImages = { postId -> newsFeedViewModel.getPostImages(postId) },
                            onFetchOriginalPost = { postId -> newsFeedViewModel.fetchProfileOriginalPost(postId) },
                            navController = navController
                        )
                    }
                    2 -> { // Photos Tab
                        PhotosSection(
                            postImages = postImages,
                            navController = navController
                        )
                    }
                }
            }
        }
    }
}