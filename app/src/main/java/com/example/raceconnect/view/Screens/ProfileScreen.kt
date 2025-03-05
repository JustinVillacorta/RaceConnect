import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.raceconnect.R
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel
import com.example.raceconnect.view.ui.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AuthenticationViewModel = viewModel(),
    onLogoutSuccess: () -> Unit // Callback to navigate after logout
) {
    val user by viewModel.loggedInUser.collectAsState()
    var showLogoutDialog by remember { mutableStateOf(false) } // State for showing logout confirmation dialog
    var logoutErrorMessage by remember { mutableStateOf<String?>(null) } // State for logout error message

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Menu",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Color.White
                    )
                },
                actions = {
                    // Search icon
                    IconButton(onClick = { /* Handle search click */ }) {
                        Icon(
                            painter = painterResource(id = com.example.raceconnect.R.drawable.baseline_search_24),
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Apply padding from Scaffold to avoid overlap with TopAppBar
                .padding(16.dp)
        ) {
            // Profile Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_account_circle_24), // Placeholder image
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = user?.username ?: "Guest",
                    style = MaterialTheme.typography.headlineSmall,
                )
                Text(
                    text = user?.email ?: "Not logged in",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Options List
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    ProfileOptionItem(
                        iconResId = R.drawable.baseline_account_circle_24,
                        text = "Personal Details",
                        onClick = { /* Handle Personal Details Click */ }
                    )
                }
                item {
                    ProfileOptionItem(
                        iconResId = R.drawable.baseline_favorite_24,
                        text = "Favorite Items",
                        onClick = { /* Handle Favorite Items Click */ }
                    )
                }
                item {
                    ProfileOptionItem(
                        iconResId = R.drawable.baseline_home_24,
                        text = "News Feed Preferences",
                        onClick = { /* Handle Preferences Click */ }
                    )
                }
                item {
                    ProfileOptionItem(
                        iconResId = R.drawable.baseline_settings_24,
                        text = "Settings",
                        onClick = { /* Handle Settings Click */ }
                    )
                }
                item {
                    ProfileOptionItem(
                        iconResId = R.drawable.baseline_settings_24,
                        text = "FAQ",
                        onClick = { /* Handle FAQ Click */ }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f)) // Push the logout button to the bottom

            // Logout Button with Confirmation Dialog
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showLogoutDialog = true } // Show confirmation dialog
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ExitToApp,
                    contentDescription = "Logout",
                    tint = Color.Red
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log out",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Red
                )
            }

            // Logout Confirmation Dialog
            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Confirm Logout") },
                    text = { Text("Do you want to log out?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLogoutDialog = false
                                viewModel.logout {
                                    onLogoutSuccess() // Navigate to login screen
                                }
                            }
                        ) {
                            Text("Yes")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showLogoutDialog = false }
                        ) {
                            Text("No")
                        }
                    }
                )
            }

            // Show Logout Error Message
            logoutErrorMessage?.let { errorMsg ->
                Text(
                    text = errorMsg,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileOptionItem(iconResId: Int, text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = text,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}