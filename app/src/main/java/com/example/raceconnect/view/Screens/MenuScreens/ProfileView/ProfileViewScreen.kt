package com.example.raceconnect.view.Screens.MenuScreens.ProfileView

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.PostImage
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModelFactory
import com.google.gson.Gson

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
    val userReposts by newsFeedViewModel.userReposts.collectAsState()
    val profileOriginalPosts by newsFeedViewModel.profileOriginalPosts.collectAsState()

    // State for delete confirmation
    var postToDelete by remember { mutableStateOf<NewsFeedDataClassItem?>(null) }

    LaunchedEffect(userId) {
        Log.d("UserProfileScreen", "Loading profile data")
        profileDetailsViewModel.loadProfileData()
        userId?.let { newsFeedViewModel.fetchUserReposts(it) }
    }

    LaunchedEffect(newsFeedViewModel.newPostTrigger) {
        if (newsFeedViewModel.newPostTrigger.value) {
            posts?.refresh()
            newsFeedViewModel.resetNewPostTrigger()
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
                .padding(top = 16.dp)
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 8.dp)
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
                            .size(150.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    ) {
                        val profilePictureUrl = profileData?.profilePicture
                        val painter = if (profilePictureUrl != null && profilePictureUrl.isNotEmpty()) {
                            rememberAsyncImagePainter(model = profilePictureUrl)
                        } else {
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
                    contentColor = Red, // This sets the text color of the selected tab
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            modifier = Modifier
                                .tabIndicatorOffset(tabPositions[selectedTabIndex])
                                .height(2.dp), // Thickness of the underline
                            color = Red // Set the underline color to red
                        )
                    }
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
                    0 -> PostsSection(
                        posts = posts,
                        postImages = postImages,
                        onEditPost = { post ->
                            val postJson = Gson().toJson(post)
                            navController.navigate(NavRoutes.EditPost.createRoute(postJson))
                        },
                        onDeletePost = { post -> postToDelete = post },
                        onFetchPostImages = { postId -> newsFeedViewModel.getPostImages(postId) },
                        navController = navController
                    )
                    1 -> RepostsSection(
                        userReposts = userReposts,
                        postImages = postImages,
                        profileOriginalPosts = profileOriginalPosts,
                        profileUserId = profileData?.id ?: 0,
                        profileUsername = profileData?.username,
                        profilePicture = profileData?.profilePicture, // Pass profilePicture here
                        onFetchPostImages = { postId -> newsFeedViewModel.getPostImages(postId) },
                        onFetchOriginalPost = { postId ->
                            newsFeedViewModel.fetchProfileOriginalPost(postId)
                        },
                        navController = navController
                    )
                    2 -> PhotosSection(
                        postImages = postImages,
                        navController = navController
                    )
                }
            }
        }

        // Confirmation dialog for deletion
        if (postToDelete != null) {
            AlertDialog(
                onDismissRequest = { postToDelete = null },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete this post?") },
                confirmButton = {
                    TextButton(onClick = {
                        newsFeedViewModel.deletePost(postToDelete!!.id)
                        postToDelete = null
                    }) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { postToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}