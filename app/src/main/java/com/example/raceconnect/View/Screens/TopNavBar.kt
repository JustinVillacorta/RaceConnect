package com.example.raceconnect.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import com.example.raceconnect.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopNavBar(navController: NavController) {
    val items = listOf(
        TopNavTab.NewsFeed,
        TopNavTab.Reels,
        TopNavTab.Marketplace,
        TopNavTab.Notifications,
        TopNavTab.Profile
    )

    TopAppBar(
        title = { /* Empty Title */ },
        actions = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    IconButton(onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = false }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.title,
                            tint = if (navController.currentDestination?.route == item.route) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }
        }
    )
}
