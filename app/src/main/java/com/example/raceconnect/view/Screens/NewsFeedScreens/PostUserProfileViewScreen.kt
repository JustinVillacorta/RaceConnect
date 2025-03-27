package com.example.raceconnect.view.Screens.MenuScreens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.paging.compose.collectAsLazyPagingItems
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.view.Screens.NewsFeedScreens.formatTime
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
    val newsFeedViewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences, context))
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
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(posts.itemCount) { index ->
                                posts[index]?.let { post ->
                                    if (post.user_id == userId) {
                                        LaunchedEffect(post.id) {
                                            newsFeedViewModel.getPostImages(post.id)
                                        }
                                        Card(
                                            shape = RectangleShape,
                                            modifier = Modifier.fillMaxWidth(),
                                            elevation = CardDefaults.cardElevation(4.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.Gray)
                                                    ) {
                                                        val painter = if (profileData?.profilePicture != null && profileData!!.profilePicture!!.isNotEmpty()) {
                                                            rememberAsyncImagePainter(model = profileData!!.profilePicture)
                                                        } else {
                                                            painterResource(id = R.drawable.baseline_account_circle_24)
                                                        }
                                                        Image(
                                                            painter = painter,
                                                            contentDescription = "User Profile",
                                                            contentScale = ContentScale.Crop,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = post.username ?: "Anonymous",
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = formatTime(post.created_at),
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = Color.Gray
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = post.content ?: "",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Black
                                                )
                                                Spacer(modifier = Modifier.height(12.dp))
                                                val images = postImages[post.id]
                                                if (images?.isNotEmpty() == true) {
                                                    if (images.size == 1) {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(top = 8.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                        ) {
                                                            val painter = rememberAsyncImagePainter(model = images.first())
                                                            Image(
                                                                painter = painter,
                                                                contentDescription = "Post image",
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .aspectRatio(1.91f),
                                                                contentScale = ContentScale.Crop
                                                            )
                                                        }
                                                    } else {
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(340.dp)
                                                                .padding(top = 8.dp)
                                                        ) {
                                                            val pagerState = rememberPagerState(pageCount = { images.size })
                                                            HorizontalPager(
                                                                state = pagerState,
                                                                modifier = Modifier.fillMaxSize()
                                                            ) { page ->
                                                                Box(
                                                                    modifier = Modifier
                                                                        .fillMaxSize()
                                                                        .clip(RoundedCornerShape(8.dp))
                                                                ) {
                                                                    val painter = rememberAsyncImagePainter(model = images[page])
                                                                    Image(
                                                                        painter = painter,
                                                                        contentDescription = "Post image $page",
                                                                        modifier = Modifier.fillMaxSize(),
                                                                        contentScale = ContentScale.Crop
                                                                    )
                                                                }
                                                            }
                                                            Box(
                                                                modifier = Modifier
                                                                    .align(Alignment.TopEnd)
                                                                    .padding(8.dp)
                                                                    .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(4.dp))
                                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                                            ) {
                                                                Text(
                                                                    text = "${pagerState.currentPage + 1}/${images.size}",
                                                                    color = Color.White,
                                                                    style = MaterialTheme.typography.bodySmall
                                                                )
                                                            }
                                                            Row(
                                                                modifier = Modifier
                                                                    .align(Alignment.BottomCenter)
                                                                    .padding(bottom = 8.dp),
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                            ) {
                                                                images.forEachIndexed { index, _ ->
                                                                    val isSelected = index == pagerState.currentPage
                                                                    Box(
                                                                        modifier = Modifier
                                                                            .size(8.dp)
                                                                            .clip(CircleShape)
                                                                            .background(if (isSelected) Color.White else Color.Transparent)
                                                                            .then(
                                                                                if (!isSelected) Modifier.border(1.dp, Color.White, CircleShape)
                                                                                else Modifier
                                                                            )
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