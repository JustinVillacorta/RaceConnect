package com.example.raceconnect.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.raceconnect.R
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel.MenuViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModel

private val BrandRed = Color(0xFFC62828)

@Composable
fun MenuScreen(
    viewModel: AuthenticationViewModel,
    menuViewModel: MenuViewModel,
    profileDetailsViewModel: ProfileDetailsViewModel,
    marketplaceViewModel: MarketplaceViewModel, // Added MarketplaceViewModel as a parameter
    onLogoutSuccess: () -> Unit,
    navController: NavController,
    onShowFavoriteItems: () -> Unit,
    onShowNewsFeedPreferences: () -> Unit,
    onShowListedItems: () -> Unit,
    onShowFriendListScreen: () -> Unit,
    userPreferences: UserPreferences
) {
    val profileData by profileDetailsViewModel.profileData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val userState by userPreferences.user.collectAsState(initial = null)
    val loggedInUserId = userState?.id ?: 0

    LaunchedEffect(Unit) {
        if (profileData == null && loggedInUserId != 0) {
            Log.d("ProfileScreen", "Loading profile data on screen start")
            profileDetailsViewModel.loadProfileData()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(BrandRed)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )

                Box(modifier = Modifier.wrapContentSize(Alignment.TopEnd)) {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = Color.White
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false },
                        modifier = Modifier
                            .shadow(8.dp, RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                    ) {
                        DropdownMenuItem(
                            text = { Text("Logout", style = MaterialTheme.typography.labelLarge) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.ExitToApp,
                                    contentDescription = "Logout"
                                )
                            },
                            onClick = {
                                showMenu = false
                                showLogoutDialog = true
                            }
                        )
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 80.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                        .clickable {
                            if (loggedInUserId != 0) {
                                navController.navigate(NavRoutes.ProfileView.createRoute(loggedInUserId))
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray)
                        ) {
                            val imagePainter = profileData?.profilePicture?.let { url ->
                                Log.d("ProfileScreen", "Loading profile picture from URL: $url")
                                rememberAsyncImagePainter(
                                    model = url,
                                    onError = { error ->
                                        Log.e("ProfileScreen", "Failed to load image: ${error.result.throwable.message}")
                                    }
                                )
                            } ?: painterResource(id = R.drawable.baseline_account_circle_24)

                            Image(
                                painter = imagePainter,
                                contentDescription = "Profile Picture",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = profileData?.username ?: "Guest",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Text(
                            text = profileData?.email ?: "Guest",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MenuOptionCard(
                            iconResId = R.drawable.baseline_account_circle_24,
                            text = "Profile Details",
                            onClick = { navController.navigate(NavRoutes.ProfileDetails.route) }
                        )
                        MenuOptionCard(
                            iconResId = R.drawable.baseline_favorite_24,
                            text = "Favorite Items",
                            onClick = onShowFavoriteItems
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MenuOptionCard(
                            iconResId = R.drawable.baseline_home_24,
                            text = "Preferences",
                            onClick = onShowNewsFeedPreferences
                        )
                        MenuOptionCard(
                            iconResId = R.drawable.baseline_sell_24,
                            text = "Listed Items",
                            onClick = onShowListedItems
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MenuOptionCard(
                            iconResId = R.drawable.baseline_people_24,
                            text = "Friends",
                            onClick = onShowFriendListScreen
                        )

                        MenuOptionCard(
                            iconResId = R.drawable.baseline_chat_24,
                            text = "Conversations",
                            onClick = onShowFriendListScreen
                        )
                    }
                }
            }
        }

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text("Confirm Logout") },
                text = { Text("Are you sure you want to log out?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout(
                                menuViewModel = menuViewModel,
                                marketplaceViewModel = marketplaceViewModel, // Pass the MarketplaceViewModel
                                onLogoutResult = { success, error ->
                                    if (success) {
                                        profileDetailsViewModel.apply {
                                            _profileData.value = null
                                            _isEditMode.value = false
                                        }
                                        onLogoutSuccess()
                                    } else {
                                        Log.e("ProfileScreen", "Logout failed: $error")
                                        // Optionally show a snackbar or toast with the error
                                    }
                                }
                            )
                        }
                    ) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
                        Text("No")
                    }
                }
            )
        }

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = BrandRed
            )
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

@Composable
fun MenuOptionCard(
    iconResId: Int,
    text: String,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        modifier = Modifier
            .size(width = 150.dp, height = 100.dp)
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}