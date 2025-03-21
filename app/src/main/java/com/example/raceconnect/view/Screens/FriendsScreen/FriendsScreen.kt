package com.example.raceconnect.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Icon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.Friend
import com.example.raceconnect.view.ui.theme.Red // Assuming Red is defined as #D32F2F or similar
import com.example.raceconnect.viewmodel.FriendsViewModel
import com.example.raceconnect.viewmodel.FriendsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    userPreferences: UserPreferences,
    onClose: () -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = FriendsViewModelFactory(userPreferences))
) {

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val friends by viewModel.friends.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val loggedInUser by userPreferences.user.collectAsState(initial = null)
    // Assuming loggedInUser is of type Users?, extract the id field
    val loggedInUserId = loggedInUser?.id?.toString() // Ensure id is accessed correctly

    LaunchedEffect(Unit) {
        viewModel.fetchFriends() // Ensure data is fetched on screen load
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Friends",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Red,
                        titleContentColor = Color.White
                    )
                )
                SearchBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        viewModel.searchUsers(it)
                    },
                    onSearch = { viewModel.searchUsers(it) },
                    active = isSearchActive,
                    onActiveChange = { isSearchActive = it },
                    placeholder = { Text("Search users") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_search_24),
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (isSearchActive) {
                            IconButton(onClick = {
                                if (searchQuery.isNotEmpty()) {
                                    searchQuery = ""
                                    viewModel.searchUsers("")
                                } else {
                                    isSearchActive = false
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = "Close Search"
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp) // Limit height of search results
                    ) {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .size(36.dp)
                                    .align(Alignment.Center)
                            )
                        } else {
                            LazyColumn {
                                items(searchResults) { user ->
                                    FriendItem(
                                        friend = user,
                                        onAdd = {
                                            viewModel.addFriend(user.id)
                                            searchQuery = ""
                                            isSearchActive = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (!isSearchActive) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Color(0xFFF5F5F5))
            ) {
                // Friend Requests Section
                item {
                    Text(
                        text = "Friend Requests",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                    )
                }
                items(friends.filter { friend ->
                    val isPending = friend.status == "Pending"
                    println("Friend Requests Check - Friend: ${friend.name}, id=${friend.id}, status=${friend.status}, receiverId=${friend.receiverId}, isPending=$isPending")
                    isPending
                }) { friend ->
                    FriendItem(
                        friend = friend,
                        onConfirm = { viewModel.confirmFriendRequest(friend.id) },
                        onCancel = { viewModel.cancelFriendRequest(friend.id) }
                    )
                }

                // People You May Know Section
                item {
                    Text(
                        text = "People You May Know",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
                    )
                }
                items(
                    friends.filter { friend ->
                        val isNotReceiver = if (friend.receiverId != null && loggedInUserId != null) {
                            val result = friend.receiverId != loggedInUserId
                            println("Comparison: receiverId=${friend.receiverId} (type: ${friend.receiverId?.let { it::class.simpleName } ?: "null"}) vs loggedInUserId=$loggedInUserId (type: ${loggedInUserId?.let { it::class.simpleName } ?: "null"}), result=$result")
                            result
                        } else {
                            println("Null check: receiverId=${friend.receiverId}, loggedInUserId=$loggedInUserId, defaulting to true")
                            true
                        }
                        val shouldShow = (friend.status == "NonFriends" || friend.status == "PendingSent") && isNotReceiver
                        println("People You May Know Check - Friend: ${friend.name}, id=${friend.id}, status=${friend.status}, receiverId=${friend.receiverId}, loggedInUserId=$loggedInUserId, isNotReceiver=$isNotReceiver, shouldShow=$shouldShow")
                        shouldShow
                    }
                ) { friend ->
                    println("Rendering FriendItem in People You May Know - Friend: ${friend.name}, id=${friend.id}, status=${friend.status}")
                    FriendItem(
                        friend = friend,
                        onAdd = { viewModel.addFriend(friend.id) },
                        onRemove = { viewModel.removeFriend(friend.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun FriendItem(
    friend: Friend,
    onConfirm: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
    onAdd: (() -> Unit)? = null,
    onRemove: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: Profile picture and username
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(
                    model = friend.profileImageUrl ?: "",
                    placeholder = painterResource(id = R.drawable.ic_launcher_background),
                    error = painterResource(id = R.drawable.ic_launcher_background)
                ),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = friend.name,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )
            )
        }

        // Right side: Vertically stacked buttons for Friend Requests
        when (friend.status) {
            "Pending" -> {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Space between buttons
                ) {
                    Button(
                        onClick = { onConfirm?.invoke() },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F), // Red from screenshot
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .width(100.dp) // Fixed width for consistency
                            .height(40.dp)
                    ) {
                        Text("Confirm", fontSize = 14.sp)
                    }
                    Button(
                        onClick = { onCancel?.invoke() },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0), // Light gray
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .width(100.dp) // Fixed width for consistency
                            .height(40.dp)
                    ) {
                        Text("Delete", fontSize = 14.sp)
                    }
                }
            }
            "PendingSent" -> {
                Button(
                    onClick = { onRemove?.invoke() },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE0E0E0), // Your light gray
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Pending", fontSize = 14.sp)
                }
            }
            "NonFriends" -> {
                Button(
                    onClick = { onAdd?.invoke() },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C0C13), // Your darker red from original code
                        contentColor = Color.White
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Add Friend", fontSize = 14.sp)
                }
            }
        }
    }
}