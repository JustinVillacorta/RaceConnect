package com.example.raceconnect.navigation

import NewsFeedScreen
import NotificationsScreen
import ProfileScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.ui.BottomNavBar
import com.example.raceconnect.ui.MarketplaceScreen
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.Screens.Authentication.AuthenticationNavHost
import com.example.raceconnect.view.Screens.NewsFeedScreens.CommentScreen


@Composable
fun AppNavigation(userPreferences: UserPreferences) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = NavRoutes.NewsFeed.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavRoutes.NewsFeed.route) {
                NewsFeedScreen(navController, userPreferences) // ✅ Pass correct parameters
            }
            composable(NavRoutes.Comments.route) { backStackEntry ->
                val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: -1
                CommentScreen(postId = postId, navController = navController)
            }
            composable(NavRoutes.Profile.route) {
                ProfileScreen { navController.navigate(NavRoutes.Login.route) }
            }
            composable(NavRoutes.Marketplace.route) {
                MarketplaceScreen(userPreferences)
            }
            composable(NavRoutes.Notifications.route) {
                NotificationsScreen()
            }
        }
    }
}
