package com.example.raceconnect.view.Screens



import NewsFeedAppNavigation
import NotificationsScreen
import ProfileScreen
import android.app.Application
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.ui.BottomNavBar
import com.example.raceconnect.view.Navigation.BottomNavTab
import com.example.raceconnect.datastore.UserPreferences

import com.example.raceconnect.ui.MarketplaceScreen
import com.example.raceconnect.ui.TopAppBar


@Composable
fun MainScreen(userPreferences: UserPreferences) {
    val navController = rememberNavController()

    Scaffold(
        topBar = { TopAppBar(navController = navController) },
        bottomBar = { BottomNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavTab.NewsFeed.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(BottomNavTab.NewsFeed.route) {
                val application = LocalContext.current.applicationContext as Application
                NewsFeedAppNavigation(application = application, userPreferences = userPreferences)
            }
            composable(BottomNavTab.Marketplace.route) {
                MarketplaceScreen(userPreferences = userPreferences)
            }
            composable(BottomNavTab.Notifications.route) {
                NotificationsScreen()
            }
            composable(BottomNavTab.Profile.route) {
                ProfileScreen(onLogoutSuccess = {
                    navController.navigate(BottomNavTab.NewsFeed.route)
                })
            }
        }
    }
}
