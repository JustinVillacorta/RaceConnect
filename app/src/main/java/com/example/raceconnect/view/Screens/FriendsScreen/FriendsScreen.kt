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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.Friend
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.FriendsViewModel
import com.example.raceconnect.viewmodel.FriendsViewModelFactory
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.text.style.TextOverflow
import com.example.raceconnect.view.ui.theme.fontFamily

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    userPreferences: UserPreferences,
    onClose: () -> Unit,
    onNavigateToProfile: (String) -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = FriendsViewModelFactory(userPreferences))
) {
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    val friends by viewModel.friends.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val loggedInUser by userPreferences.user.collectAsState(initial = null)
    val loggedInUserId = loggedInUser?.id?.toString()

    LaunchedEffect(Unit) {
        viewModel.fetchFriends()
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Friends",
                                fontFamily = fontFamily,
                                color = Color.White,
                                fontSize = 30.sp
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
                    onActiveChange = {
                        isSearchActive = it
                        if (!it) {
                            searchQuery = ""
                            viewModel.clearSearchResults()
                        } else {
                            viewModel.searchUsers(searchQuery)
                        }
                    },
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
                                    viewModel.clearSearchResults()
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
                            .heightIn(max = 300.dp)
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
                                        onAdd = if (user.status == "NonFriends") {
                                            {
                                                viewModel.addFriend(user.id)
                                                searchQuery = ""
                                                isSearchActive = false
                                            }
                                        } else null,
                                        onConfirm = if (user.status == "Pending") {
                                            {
                                                viewModel.confirmFriendRequest(user.id)
                                                searchQuery = ""
                                                isSearchActive = false
                                            }
                                        } else null,
                                        onCancel = if (user.status == "Pending") {
                                            {
                                                viewModel.cancelFriendRequest(user.id)
                                                searchQuery = ""
                                                isSearchActive = false
                                            }
                                        } else null,
                                        onRemove = if (user.status == "PendingSent") {
                                            {
                                                viewModel.cancelFriendRequest(user.id)
                                                searchQuery = ""
                                                isSearchActive = false
                                            }
                                        } else null,
                                        onProfileClick = { onNavigateToProfile(user.id.toString()) }
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
                    .background(MaterialTheme.colorScheme.surface) // Set the background color here
                    .padding(paddingValues)
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
                        onCancel = { viewModel.cancelFriendRequest(friend.id) },
                        onProfileClick = { onNavigateToProfile(friend.id.toString()) }
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
                    FriendItem(
                        friend = friend,
                        onAdd = if (friend.status == "NonFriends") {
                            { viewModel.addFriend(friend.id) }
                        } else null,
                        onRemove = if (friend.status == "PendingSent") {
                            { viewModel.cancelFriendRequest(friend.id) }
                        } else null,
                        onProfileClick = { onNavigateToProfile(friend.id.toString()) }
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
    onRemove: (() -> Unit)? = null,
    onProfileClick: (String) -> Unit
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
        Row(
            modifier = Modifier.clickable { onProfileClick(friend.id.toString()) },
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .width(120.dp)
                            .height(36.dp)
                    ) {
                        Text("Confirm", fontSize = 14.sp)
                    }
                    Button(
                        onClick = { onCancel?.invoke() },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .width(120.dp)
                            .height(36.dp)
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
                        containerColor = Color(0xFFE0E0E0),
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .width(120.dp)
                        .height(36.dp)
                ) {
                    Text("Pending", fontSize = 14.sp,)
                }
            }
            "NonFriends" -> {
                Button(
                    onClick = { onAdd?.invoke() },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF9C0C13),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .width(120.dp) // Ensures minimum width
                        .height(36.dp)
                ) {
                    Text("Add Friend",
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}