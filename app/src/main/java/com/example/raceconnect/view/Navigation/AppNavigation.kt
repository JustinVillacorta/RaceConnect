package com.example.raceconnect.navigation

import FriendsScreen
import NewsFeedScreen
import NotificationsScreen
import ProfileScreen
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.ui.BottomNavBar
import com.example.raceconnect.ui.MarketplaceScreen
import com.example.raceconnect.ui.TopAppBar
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.Navigation.AuthenticationNavHost
import com.example.raceconnect.view.Screens.MarketplaceScreens.ChatSellerScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.MarketplaceItemDetailScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.CommentScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.CreatePostScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.ProfileViewScreen
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory

@Composable
fun AppNavigation(userPreferences: UserPreferences) {
    val navController = rememberNavController()
    val token = userPreferences.token.collectAsState(initial = null).value
    val context = LocalContext.current

    // State to control CreatePostScreen visibility
    var showCreatePostScreen by remember { mutableStateOf(false) }

    // Use viewModel() with factory to instantiate NewsFeedViewModel
    val viewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences))

    if (token == null) {
        AuthenticationNavHost()
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
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
                        NewsFeedScreen(
                            navController = navController,
                            userPreferences = userPreferences,
                            onShowCreatePost = { showCreatePostScreen = true }
                        )
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
                        ProfileViewScreen(navController = navController, context = context)
                    }
                    composable(NavRoutes.Friends.route) {
                        FriendsScreen(
                            userPreferences = userPreferences,
                            onClose = { navController.popBackStack() }
                        )
                    }
                    composable(NavRoutes.MarketplaceItemDetail.route) { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull() ?: -1
                        MarketplaceItemDetailScreen(itemId = itemId, navController = navController)
                    }
                    composable(NavRoutes.ChatSeller.route) { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull() ?: -1
                        ChatSellerScreen(itemId = itemId, navController = navController)
                    }
                }
            }

            // Overlay CreatePostScreen on top of everything
            AnimatedVisibility(
                visible = showCreatePostScreen,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = tween(durationMillis = 300)
                ) + fadeOut(animationSpec = tween(durationMillis = 300))
            ) {
                CreatePostScreen(
                    viewModel = viewModel,
                    onClose = { showCreatePostScreen = false }
                )
            }
        }
    }
}