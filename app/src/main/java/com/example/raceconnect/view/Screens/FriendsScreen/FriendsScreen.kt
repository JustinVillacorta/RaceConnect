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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.Friend
import com.example.raceconnect.view.ui.theme.Red
import com.example.raceconnect.viewmodel.FriendsViewModel
import com.example.raceconnect.viewmodel.FriendsViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsScreen(
    userPreferences: UserPreferences,
    onClose: () -> Unit,
    viewModel: FriendsViewModel = viewModel(factory = FriendsViewModelFactory(userPreferences))
) {
    val friends by viewModel.friends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Friends",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(onClick = { /* Handle search click */ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_search_24),
                            contentDescription = "Search",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Red,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Friend Requests Section
            item {
                Text(
                    text = "Friend requests",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                )
            }
            items(friends.filter { it.status == "Pending" }) { friend ->
                FriendItem(
                    friend = friend,
                    onConfirm = { viewModel.confirmFriendRequest(friend.id) },
                    onCancel = { viewModel.cancelFriendRequest(friend.id) }
                )
            }

            // Explore Friends Section
            item {
                Text(
                    text = "Explore friends",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                )
            }
            items(friends.filter { it.status == "NonFriends" }) { friend ->
                FriendItem(
                    friend = friend,
                    onAdd = { viewModel.addFriend(friend.id) },
                    onRemove = { viewModel.removeFriend(friend.id) }
                )
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
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter(friend.profileImageUrl ?: ""), // Use actual URL or fallback
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.Gray)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = friend.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            when (friend.status) {
                "Pending" -> {
                    Button(
                        onClick = { onConfirm?.invoke() },
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Confirm")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onCancel?.invoke() },
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Cancel")
                    }
                }
                "NonFriends" -> {
                    Button(
                        onClick = { onAdd?.invoke() },
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9C0C13),
                            contentColor = Color.White
                        )
                    ) {
                        Text("Add friend")
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { onRemove?.invoke() },
                        modifier = Modifier.fillMaxWidth(0.7f),
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE0E0E0),
                            contentColor = Color.Black
                        )
                    ) {
                        Text("Remove")
                    }
                }
            }
        }
    }
}