package com.example.raceconnect.view.Screens.MenuScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.Friend
import com.example.raceconnect.viewmodel.FriendsViewModel
import com.example.raceconnect.viewmodel.FriendsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(
    navController: NavController,
    onClose: () -> Unit,
    userPreferences: UserPreferences
) {
    val viewModel: FriendsViewModel = viewModel(factory = FriendsViewModelFactory(userPreferences))
    val friends by viewModel.friends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    FriendsListScreenContent(
        onBackClick = onClose,
        friends = friends,
        isLoading = isLoading,
        onConfirm = viewModel::confirmFriendRequest,
        onCancel = viewModel::cancelFriendRequest,
        onAdd = viewModel::addFriend,
        onRemove = viewModel::removeFriend
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreenContent(
    onBackClick: () -> Unit,
    friends: List<Friend>,
    isLoading: Boolean,
    onConfirm: (String) -> Unit,
    onCancel: (String) -> Unit,
    onAdd: (String) -> Unit,
    onRemove: (String) -> Unit
) {
    val selectedTabIndex = remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Friends", "Add Friends")
    val searchQuery = remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Friends",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex.intValue,
                containerColor = Color.White,
                contentColor = Color.Black,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex.intValue]),
                        height = 3.dp,
                        color = Color(0xFF007AFF)
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex.intValue == index,
                        onClick = { selectedTabIndex.intValue = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 16.sp,
                                fontWeight = if (selectedTabIndex.intValue == index) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTabIndex.intValue == index) Color.Black else Color.Gray
                            )
                        }
                    )
                }
            }

            when (selectedTabIndex.intValue) {
                0 -> {
                    val acceptedFriends = friends.filter { it.status == "Accepted" }
                    when {
                        isLoading -> LoadingState()
                        acceptedFriends.isEmpty() -> EmptyState("No friends yet")
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(acceptedFriends) { friend ->
                                    FriendItem(
                                        friend = friend,
                                        onRemove = { onRemove(friend.id) }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    val nonFriends = friends.filter { it.status != "Accepted" }
                    val filteredNonFriends = nonFriends.filter {
                        it.name.contains(searchQuery.value, ignoreCase = true)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                    ) {
                        SearchBar(searchQuery)
                        when {
                            isLoading -> LoadingState()
                            filteredNonFriends.isEmpty() -> EmptyState("No users found")
                            else -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(filteredNonFriends) { friend ->
                                        FriendItem(
                                            friend = friend,
                                            onConfirm = if (friend.status == "Pending") { { onConfirm(friend.id) } } else null,
                                            onCancel = if (friend.status == "Pending") { { onCancel(friend.id) } } else null,
                                            onAdd = if (friend.status == "NonFriends") { { onAdd(friend.id) } } else null,
                                            onRemove = if (friend.status == "PendingSent") { { onRemove(friend.id) } } else null
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

@Composable
private fun SearchBar(searchQuery: MutableState<String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp)
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                decorationBox = { innerTextField ->
                    if (searchQuery.value.isEmpty()) {
                        Text(text = "Search for friends", color = Color.Gray, fontSize = 16.sp)
                    }
                    innerTextField()
                }
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, fontSize = 16.sp, color = Color.Gray)
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
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
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

        when (friend.status) {
            "Pending" -> {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onConfirm?.invoke() },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F), contentColor = Color.White),
                        modifier = Modifier
                            .width(100.dp)
                            .height(40.dp)
                    ) {
                        Text("Confirm", fontSize = 14.sp)
                    }
                    Button(
                        onClick = { onCancel?.invoke() },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black),
                        modifier = Modifier
                            .width(100.dp)
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Pending", fontSize = 14.sp)
                }
            }
            "NonFriends" -> {
                Button(
                    onClick = { onAdd?.invoke() },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C0C13), contentColor = Color.White),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Add Friend", fontSize = 14.sp)
                }
            }
            "Accepted" -> {
                Box {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { showMenu = true }
                    )
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier.background(Color.White)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Unfriend", color = Color.Black, fontSize = 14.sp) },
                            onClick = {
                                onRemove?.invoke()
                                showMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}