package com.example.raceconnect.view.Screens

import NewsFeedScreen
import NotificationsScreen
import ProfileScreen
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.ui.MarketplaceScreen
import com.example.raceconnect.ui.ReelsScreen
import com.example.raceconnect.ui.TopNavBar
import com.example.raceconnect.view.Activities.TopNavTab
import com.example.raceconnect.ui.MarketplaceNavHost


@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { TopNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopNavTab.NewsFeed.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TopNavTab.NewsFeed.route) { NewsFeedScreen() }
            composable(TopNavTab.Reels.route) { ReelsScreen() }
            composable(TopNavTab.Marketplace.route) { MarketplaceNavHost() }
            composable(TopNavTab.Notifications.route) { NotificationsScreen() }
            composable(TopNavTab.Profile.route) { ProfileScreen() }
        }
    }
}



