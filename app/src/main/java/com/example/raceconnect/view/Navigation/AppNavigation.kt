package com.example.raceconnect.navigation

import FriendsScreen
import NewsFeedScreen
import NotificationsScreen
import ProfileScreen
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.ui.BottomNavBar
import com.example.raceconnect.ui.MarketplaceScreen
import com.example.raceconnect.ui.TopAppBar
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.Navigation.AuthenticationNavHost
import com.example.raceconnect.view.Screens.MarketplaceScreens.MarketplaceItemDetailScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.CommentScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.ProfileViewScreen

@Composable
fun AppNavigation(userPreferences: UserPreferences) {
    val navController = rememberNavController()
    val token = userPreferences.token.collectAsState(initial = null).value
    val context = LocalContext.current // Get the context for ProfileViewScreen

    // If token is null, user is not logged in -> show AuthenticationNavHost
    if (token == null) {
        AuthenticationNavHost()
    } else {
        // User is logged in -> show main app navigation
        Scaffold(
            topBar = { TopAppBar(navController = navController) },
            bottomBar = { BottomNavBar(navController) }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = NavRoutes.NewsFeed.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(NavRoutes.NewsFeed.route) {
                    NewsFeedScreen(navController, userPreferences)
                }
                composable(NavRoutes.Comments.route) { backStackEntry ->
                    val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: -1
                    CommentScreen(postId = postId, navController = navController)
                }
                composable(NavRoutes.Profile.route) {
                    ProfileScreen { navController.navigate(NavRoutes.Login.route) }
                }
                composable(NavRoutes.Marketplace.route) {
                    MarketplaceScreen(userPreferences, navController)
                }
                composable(NavRoutes.Notifications.route) {
                    NotificationsScreen()
                }
                composable(NavRoutes.ProfileView.route) {
                    ProfileViewScreen(
                        navController = navController,
                        context = context
                    )
                }
                composable(NavRoutes.Friends.route) { // Add Friends screen route
                    FriendsScreen(
                        userPreferences = userPreferences,
                        onClose = { navController.popBackStack() } // Navigate back when closing
                    )
                }
                // New route for marketplace item details
                composable(NavRoutes.MarketplaceItemDetail.route) { backStackEntry ->
                    val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull() ?: -1
                    MarketplaceItemDetailScreen(itemId = itemId, navController = navController)
                }
            }
        }
    }
}