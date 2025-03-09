package com.example.raceconnect.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel.MenuViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModel

// Your brand's red color
private val BrandRed = Color(0xFFC62828)

@Composable
fun ProfileScreen(
    viewModel: AuthenticationViewModel,
    menuViewModel: MenuViewModel,
    profileDetailsViewModel: ProfileDetailsViewModel, // Add ProfileDetailsViewModel
    onLogoutSuccess: () -> Unit,
    navController: NavController,
    onShowMyProfile: () -> Unit,
    onShowFavoriteItems: () -> Unit,
    onShowNewsFeedPreferences: () -> Unit,
    onShowListedItems: () -> Unit,
    onShowSettings: () -> Unit
) {
    val profileData by profileDetailsViewModel.profileData.collectAsState() // Switch to ProfileDetailsViewModel
    var showMenu by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

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
                            navController.navigate(NavRoutes.ProfileView.route)
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

                // GRID OF MENU CARDS
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // First row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MenuOptionCard(
                            iconResId = R.drawable.baseline_account_circle_24,
                            text = "My Profile",
                            onClick = onShowMyProfile
                        )
                        MenuOptionCard(
                            iconResId = R.drawable.baseline_favorite_24,
                            text = "Favorite Items",
                            onClick = onShowFavoriteItems
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Second row
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

                    // Third row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        MenuOptionCard(
                            iconResId = R.drawable.baseline_settings_24,
                            text = "Settings",
                            onClick = onShowSettings
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
                            viewModel.logout(menuViewModel) {
                                onLogoutSuccess()
                            }
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