package com.example.raceconnect.view.Screens

import NewsFeedAppNavigation
import NewsFeedScreen
import NotificationsScreen
import ProfileScreen
import android.app.Application
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.ui.MarketplaceScreen
import com.example.raceconnect.ui.TopNavBar
import com.example.raceconnect.view.Activities.TopNavTab
import com.example.raceconnect.datastore.UserPreferences

@Composable
fun MainScreen(userPreferences: UserPreferences) {
    val navController = rememberNavController()

    Scaffold(
        topBar = { TopNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopNavTab.NewsFeed.route,
            modifier = Modifier.padding(innerPadding)
        ) {

          composable(TopNavTab.NewsFeed.route) {val application = LocalContext.current.applicationContext as Application  // ✅ Get Application
              NewsFeedAppNavigation(application = application, userPreferences = userPreferences) }
            composable(TopNavTab.Marketplace.route) { MarketplaceScreen(userPreferences = userPreferences) }
            composable(TopNavTab.Notifications.route) { NotificationsScreen() }
            composable(TopNavTab.Profile.route) { ProfileScreen(onLogoutSuccess = { navController.navigate(TopNavTab.NewsFeed.route) }) }
        }
    }
}