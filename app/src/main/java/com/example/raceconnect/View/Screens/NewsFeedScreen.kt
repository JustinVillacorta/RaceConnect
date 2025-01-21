package com.example.raceconnect.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.raceconnect.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsFeedScreen(
    onNewsFeedActivity: () -> Unit,
    onReelsNavigate: () -> Unit,
    onMarketplaceNavigate: () -> Unit,
    onNotificationsNavigate: () -> Unit,
    onProfileNavigate: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("") },
                actions = {
                    // News Feed Navigation
                    IconButton(onClick = onNewsFeedActivity) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_home_24),
                            contentDescription = stringResource(id = R.string.NewsFeed)
                        )
                    }
                    // Reels Navigation
                    IconButton(onClick = onReelsNavigate) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_ondemand_video_24),
                            contentDescription = stringResource(id = R.string.reels)
                        )
                    }
                    // Marketplace Navigation
                    IconButton(onClick = onMarketplaceNavigate) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_storefront_24),
                            contentDescription = stringResource(id = R.string.marketplace)
                        )
                    }
                    // Notifications Navigation
                    IconButton(onClick = onNotificationsNavigate) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_notifications_24),
                            contentDescription = stringResource(id = R.string.notifications)
                        )
                    }
                    // Profile Navigation
                    IconButton(onClick = onProfileNavigate) {
                        Icon(
                            painter = painterResource(id = R.drawable.baseline_account_circle_24),
                            contentDescription = stringResource(id = R.string.profile)
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            // Main content goes here (e.g., News Feed)
            Column(modifier = Modifier.padding(paddingValues)) {
                Text(
                    text = "Welcome to the News Feed",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    )
}
