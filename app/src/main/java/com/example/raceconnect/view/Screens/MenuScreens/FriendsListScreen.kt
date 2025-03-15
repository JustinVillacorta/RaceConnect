package com.example.raceconnect.view.Screens.MenuScreens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreen(
    navController: NavController,
    onClose: () -> Unit,
    userPreferences: UserPreferences
) {
    val viewModel: FriendsViewModel = viewModel(factory = FriendsViewModelFactory(userPreferences))
    val acceptedFriends by viewModel.acceptedFriends.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        Log.d("FriendsListScreen", "Forcing refresh of friends data")
        viewModel.fetchAcceptedFriends()
    }

    FriendsListScreenContent(
        onBackClick = onClose,
        acceptedFriends = acceptedFriends,
        isLoading = isLoading,
        onRemove = viewModel::removeFriend
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FriendsListScreenContent(
    onBackClick: () -> Unit,
    acceptedFriends: List<Friend>,
    isLoading: Boolean,
    onRemove: (String) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Column {
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
                Box(
                    modifier = Modifier
                        .padding(start = 16.dp, top = 8.dp, bottom = 12.dp)
                        .background(
                            color = Color(0xFFF5F5F5),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${acceptedFriends.size} ${if (acceptedFriends.size == 1) "Friend" else "Friends"}",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF616161),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        },
        containerColor = Color(0xFFF5F5F5)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
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
                            Log.d("FriendsListScreen", "Rendering friend: ${friend.name}")
                            FriendItem(
                                friend = friend,
                                onRemove = onRemove
                            )
                        }
                    }
                }
            }
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
    onRemove: ((String) -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }

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

        Button(
            onClick = { showDialog = true },
            shape = RoundedCornerShape(6.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE0E0E0), contentColor = Color.Black),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Unfriend", fontSize = 14.sp)
        }
    }

    // Alert Dialog for unfriend confirmation
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Confirm Unfriend") },
            text = { Text("Are you sure you want to unfriend ${friend.name}? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onRemove?.let { remove ->
                            remove(friend.id)
                            Log.d("FriendItem", "Unfriend action triggered for friendId: ${friend.id}")
                        }
                        showDialog = false
                    }
                ) {
                    Text("Yes", color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("No", color = Color.Gray)
                }
            }
        )
    }
}