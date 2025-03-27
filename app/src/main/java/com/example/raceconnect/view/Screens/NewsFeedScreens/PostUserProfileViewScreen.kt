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
    val userReposts by newsFeedViewModel.userReposts.collectAsState() // Assuming this exists or needs to be added
    val profileOriginalPosts by newsFeedViewModel.profileOriginalPosts.collectAsState() // Assuming this exists or needs to be added
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(userId) {
        Log.d("PostUserProfileViewScreen", "Loading data for userId: $userId")
        viewModel.loadProfileData(userId)
        newsFeedViewModel.fetchUserReposts(userId) // Assuming this method exists or needs to be added
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
                val tabTitles = listOf("Posts", "Reposts", "Photos") // Added "Reposts" as middle tab

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
                    1 -> { // Reposts Tab
                        RepostsSection(
                            userReposts = userReposts,
                            postImages = postImages,
                            profileOriginalPosts = profileOriginalPosts,
                            profileUsername = profileData?.username,
                            profileUserId = userId,
                            onFetchPostImages = { postId -> newsFeedViewModel.getPostImages(postId) },
                            onFetchOriginalPost = { postId -> newsFeedViewModel.fetchProfileOriginalPost(postId) }
                        )
                    }
                    2 -> { // Photos Tab
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

// ExpandableText Composable (required by RepostsSection)
@Composable
fun ExpandableText(
    text: String,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var isTruncated by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            maxLines = if (expanded) Int.MAX_VALUE else 3,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium,
            onTextLayout = { textLayoutResult ->
                if (!expanded && textLayoutResult.hasVisualOverflow) {
                    isTruncated = true
                }
            }
        )
        if (isTruncated) {
            val toggleText = if (expanded) "See Less" else "See More"
            Text(
                text = toggleText,
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .clickable(enabled = enabled) {
                        if (enabled) {
                            expanded = !expanded
                        }
                    }
                    .padding(top = 2.dp)
            )
        }
    }
}

// RepostsSection Composable
@Composable
fun RepostsSection(
    userReposts: List<Repost>,
    postImages: Map<Int, List<String>>,
    profileOriginalPosts: Map<Int, ProfileRepostsDataClass>,
    profileUsername: String?,
    profileUserId: Int,
    onFetchPostImages: (Int) -> Unit,
    onFetchOriginalPost: (Int) -> Unit
) {
    val myReposts = userReposts.filter { it.userId == profileUserId }

    if (myReposts.isEmpty()) {
        Text(
            text = "No reposts yet",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            items(myReposts.size) { index ->
                val reversedIndex = myReposts.size - 1 - index
                val repost = myReposts[reversedIndex]
                val originalPost = profileOriginalPosts[repost.postId]

                LaunchedEffect(repost.id, repost.postId) {
                    onFetchPostImages(repost.id)
                    onFetchPostImages(repost.postId)
                    if (originalPost == null) {
                        onFetchOriginalPost(repost.postId)
                    }
                }

                Card(
                    shape = RectangleShape,
                    elevation = CardDefaults.cardElevation(2.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(vertical = 2.dp)
                ) {
                    Box(modifier = Modifier.padding(8.dp)) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
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
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Repeat,
                                            contentDescription = "Repost Icon",
                                            tint = Color.Gray,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${profileUsername ?: "Anonymous"} reposted",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = formatTime(repost.createdAt),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        maxLines = 1
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            if (!repost.quote.isNullOrEmpty()) {
                                Text(
                                    text = repost.quote,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (postImages[repost.id]?.isNotEmpty() == true) {
                                LazyRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    items(postImages[repost.id]!!.size) { index ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxHeight()
                                                .width(200.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            val painter = rememberAsyncImagePainter(model = postImages[repost.id]!![index])
                                            Image(
                                                painter = painter,
                                                contentDescription = "Repost image $index",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            if (originalPost != null) {
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.Gray)
                                            ) {
                                                Image(
                                                    painter = painterResource(id = R.drawable.baseline_account_circle_24),
                                                    contentDescription = "Original Post User Profile",
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = originalPost.username ?: "Unknown",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.Black
                                                )
                                                Text(
                                                    text = formatTime(originalPost.created_at),
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))

                                        if (!originalPost.content.isNullOrEmpty()) {
                                            ExpandableText(
                                                text = originalPost.content,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            Spacer(modifier = Modifier.height(12.dp))
                                        }

                                        if (postImages[repost.postId]?.isNotEmpty() == true) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(200.dp)
                                                    .padding(top = 8.dp)
                                            ) {
                                                val pagerState = rememberPagerState(pageCount = { postImages[repost.postId]!!.size })
                                                HorizontalPager(
                                                    state = pagerState,
                                                    modifier = Modifier.fillMaxSize()
                                                ) { page ->
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .clip(RoundedCornerShape(8.dp))
                                                    ) {
                                                        val painter = rememberAsyncImagePainter(model = postImages[repost.postId]!![page])
                                                        Image(
                                                            painter = painter,
                                                            contentDescription = "Original post image $page",
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
                                                        text = "${pagerState.currentPage + 1}/${postImages[repost.postId]!!.size}",
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
                                                    postImages[repost.postId]!!.forEachIndexed { index, _ ->
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
                            } else {
                                Text(
                                    text = "Original post unavailable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}