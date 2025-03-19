package com.example.raceconnect.view.Screens.MenuScreens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
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
    val originalPosts by newsFeedViewModel.originalPosts.collectAsState()

    // State for delete and dropdown
    var postToDelete by remember { mutableStateOf<NewsFeedDataClassItem?>(null) }
    var showDropdown by remember { mutableStateOf(false) }
    var currentPostId by remember { mutableStateOf<Int?>(null) }

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
                        val painter = if (profilePictureUrl != null && profilePictureUrl.isNotEmpty()) {
                            rememberAsyncImagePainter(
                                model = profilePictureUrl,
                                onLoading = { Log.d("UserProfileScreen", "Loading profile picture...") },
                                onSuccess = { Log.d("UserProfileScreen", "Profile picture loaded successfully") },
                                onError = { error ->
                                    Log.e("UserProfileScreen", "Error loading profile picture: ${error.result.throwable.message}")
                                }
                            )
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
                        // Posts tab: Show only original posts (isRepost != true)
                        posts?.let { pagingItems ->
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(pagingItems.itemCount) { index ->
                                    pagingItems[index]?.let { post ->
                                        if (post.isRepost != true) {
                                            LaunchedEffect(post.id) {
                                                newsFeedViewModel.getPostImages(post.id)
                                            }
                                            Card(
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp),
                                                elevation = CardDefaults.cardElevation(4.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(16.dp)) {
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
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(text = post.username ?: "Anonymous", fontWeight = FontWeight.Bold)
                                                            Text(text = post.created_at ?: "Just now", color = Color.Gray)
                                                        }
                                                        Box {
                                                            IconButton(onClick = {
                                                                currentPostId = post.id
                                                                showDropdown = true
                                                            }) {
                                                                Icon(
                                                                    imageVector = Icons.Default.MoreVert,
                                                                    contentDescription = "More Options",
                                                                    tint = MaterialTheme.colorScheme.onBackground
                                                                )
                                                            }
                                                            DropdownMenu(
                                                                expanded = showDropdown && currentPostId == post.id,
                                                                onDismissRequest = { showDropdown = false }
                                                            ) {
                                                                DropdownMenuItem(
                                                                    text = { Text("Edit") },
                                                                    onClick = {
                                                                        val postJson = Gson().toJson(post)
                                                                        navController.navigate(
                                                                            NavRoutes.EditPost.createRoute(postJson)
                                                                        )
                                                                        showDropdown = false
                                                                    }
                                                                )
                                                                DropdownMenuItem(
                                                                    text = { Text("Delete") },
                                                                    onClick = {
                                                                        postToDelete = post
                                                                        showDropdown = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(8.dp))
                                                    Text(text = post.content ?: "")
                                                    // Display multiple images in a LazyRow
                                                    if (postImages[post.id]?.isNotEmpty() == true) {
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        LazyRow(
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(200.dp)
                                                        ) {
                                                            items(postImages[post.id]!!) { imageUrl ->
                                                                val painter = rememberAsyncImagePainter(
                                                                    model = imageUrl,
                                                                    onLoading = { Log.d("UserProfileScreen", "Loading post image...") },
                                                                    onSuccess = { Log.d("UserProfileScreen", "Post image loaded successfully") },
                                                                    onError = { error ->
                                                                        Log.e("UserProfileScreen", "Error loading post image: ${error.result.throwable.message}")
                                                                    }
                                                                )
                                                                Image(
                                                                    painter = painter,
                                                                    contentDescription = "Post Image",
                                                                    modifier = Modifier
                                                                        .width(200.dp)
                                                                        .fillMaxHeight()
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
                        } ?: CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                    2 -> {
                        // Photos tab: Display all images from posts
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            postImages.forEach { (postId, images) ->
                                images.forEach { imageUrl ->
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
                    1 -> {
                        // Reposts tab: Show reposts with original post details
                        if (userReposts.isEmpty()) {
                            Text(
                                text = "No reposts yet",
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(userReposts.size) { index ->
                                    val repost = userReposts[index]
                                    val originalPost = originalPosts[repost.postId]

                                    // Fetch images for both repost and original post
                                    LaunchedEffect(repost.id, repost.postId) {
                                        newsFeedViewModel.getPostImages(repost.id) // Repost images
                                        newsFeedViewModel.getPostImages(repost.postId) // Original post images
                                        if (originalPost == null) {
                                            newsFeedViewModel.fetchOriginalPost(repost.postId)
                                        }
                                    }

                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        elevation = CardDefaults.cardElevation(4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            // Repost Header
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
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = "${profileData?.username ?: "Anonymous"} reposted",
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = repost.createdAt ?: "Just now",
                                                        color = Color.Gray
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))

                                            // Repost Quote (if any)
                                            if (!repost.quote.isNullOrEmpty()) {
                                                Text(text = repost.quote)
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }

                                            // Repost Images (if any)
                                            if (postImages[repost.id]?.isNotEmpty() == true) {
                                                LazyRow(
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(200.dp)
                                                ) {
                                                    items(postImages[repost.id]!!) { imageUrl ->
                                                        val painter = rememberAsyncImagePainter(
                                                            model = imageUrl,
                                                            onLoading = { Log.d("UserProfileScreen", "Loading repost image...") },
                                                            onSuccess = { Log.d("UserProfileScreen", "Repost image loaded successfully") },
                                                            onError = { error ->
                                                                Log.e("UserProfileScreen", "Error loading repost image: ${error.result.throwable.message}")
                                                            }
                                                        )
                                                        Image(
                                                            painter = painter,
                                                            contentDescription = "Repost Image",
                                                            modifier = Modifier
                                                                .width(200.dp)
                                                                .fillMaxHeight()
                                                                .clip(RoundedCornerShape(8.dp)),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(8.dp))
                                            }

                                            // Original Post Details
                                            if (originalPost != null) {
                                                Column {
                                                    Text(
                                                        text = "Original post by ${originalPost.username ?: "Unknown"}",
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(text = originalPost.content ?: "")
                                                    // Original Post Images (if any)
                                                    if (postImages[repost.postId]?.isNotEmpty() == true) {
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        LazyRow(
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .height(200.dp)
                                                        ) {
                                                            items(postImages[repost.postId]!!) { imageUrl ->
                                                                val painter = rememberAsyncImagePainter(
                                                                    model = imageUrl,
                                                                    onLoading = { Log.d("UserProfileScreen", "Loading original post image...") },
                                                                    onSuccess = { Log.d("UserProfileScreen", "Original post image loaded successfully") },
                                                                    onError = { error ->
                                                                        Log.e("UserProfileScreen", "Error loading original post image: ${error.result.throwable.message}")
                                                                    }
                                                                )
                                                                Image(
                                                                    painter = painter,
                                                                    contentDescription = "Original Post Image",
                                                                    modifier = Modifier
                                                                        .width(200.dp)
                                                                        .fillMaxHeight()
                                                                        .clip(RoundedCornerShape(8.dp)),
                                                                    contentScale = ContentScale.Crop
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.align(Alignment.CenterHorizontally)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPostScreen(
    post: NewsFeedDataClassItem,
    viewModel: NewsFeedViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences(context) }
    val user by userPreferences.user.collectAsState(initial = null)

    // State variables
    var postContent by remember { mutableStateOf(post.content ?: "") }
    var selectedCategory by remember { mutableStateOf(post.category ?: "Formula 1") }
    var selectedPrivacy by remember { mutableStateOf(post.privacy ?: "Public") }
    var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) } // Changed to List<Uri>
    var existingImageUrl by remember { mutableStateOf(post.images?.firstOrNull()) }

    // Image picker launcher for multiple images
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri>? ->
        selectedImageUris = uris ?: emptyList()
    }

    val categories = listOf("Formula 1", "24 Hours of Lemans", "World Rally Championship", "NASCAR", "Formula Drift", "GT Championship")
    val privacyOptions = listOf("Public", "Friends Only", "Only me")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with back button and save button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Edit Post",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        if (postContent.isNotEmpty()) {
                            viewModel.updatePost(
                                postId = post.id,
                                updatedContent = postContent,
                                updatedTitle = null,
                                updatedCategory = selectedCategory,
                                updatedPrivacy = selectedPrivacy,
                                imageUri = selectedImageUris.firstOrNull(), // Pass only first URI for now
                                onSuccess = { onClose() },
                                onFailure = { error ->
                                    Log.e("EditPostScreen", "Failed to update post: $error")
                                }
                            )
                        }
                    },
                    enabled = postContent.isNotEmpty(),
                    modifier = Modifier.padding(end = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Red,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Save")
                }
            }
            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                thickness = 1.dp,
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            )

            // Main content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                // User info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    val profilePictureUri = user?.profilePicture?.let { Uri.parse(it) }
                    if (profilePictureUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(profilePictureUri),
                            contentDescription = "Profile Picture",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profile Picture",
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = user?.username ?: "Anonymous",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                // Content input field
                OutlinedTextField(
                    value = postContent,
                    onValueChange = { postContent = it },
                    placeholder = { Text("What's on your mind?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Color.Transparent, shape = RoundedCornerShape(8.dp)),
                    textStyle = MaterialTheme.typography.bodyLarge,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent
                    ),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                )

                // Category and privacy dropdowns
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    var categoryExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    var privacyExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = privacyExpanded,
                        onExpandedChange = { privacyExpanded = !privacyExpanded },
                        modifier = Modifier.weight(1f)
                    ) {
                        TextField(
                            value = selectedPrivacy,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Privacy") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = privacyExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(16.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = privacyExpanded,
                            onDismissRequest = { privacyExpanded = false }
                        ) {
                            privacyOptions.forEach { privacy ->
                                DropdownMenuItem(
                                    text = { Text(privacy) },
                                    onClick = {
                                        selectedPrivacy = privacy
                                        privacyExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Display existing and selected images in a LazyRow
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    // Existing image (if any)
                    existingImageUrl?.let { url ->
                        item {
                            Image(
                                painter = rememberAsyncImagePainter(url),
                                contentDescription = "Existing Image",
                                modifier = Modifier
                                    .width(150.dp)
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    // Selected images
                    items(selectedImageUris) { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(uri),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .width(150.dp)
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Button to add photos
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Image, contentDescription = "Add images")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Photos")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}