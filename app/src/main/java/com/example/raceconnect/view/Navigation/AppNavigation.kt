package com.example.raceconnect.navigation

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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.ui.BottomNavBar
import com.example.raceconnect.ui.MarketplaceScreen
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.Navigation.AuthenticationNavHost
import com.example.raceconnect.view.Screens.MarketplaceScreens.ChatSellerScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.CreateMarketplaceItemScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.MarketplaceItemDetailScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.CommentScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.CreatePostScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.FullScreenImageViewer
import com.example.raceconnect.view.Screens.NewsFeedScreens.ProfileViewScreen
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModelFactory
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.example.raceconnect.view.FriendsScreen


@Composable
fun AppNavigation(userPreferences: UserPreferences) {
    val navController = rememberNavController()
    val token = userPreferences.token.collectAsState(initial = null).value
    val context = LocalContext.current

    // State to control screen visibility
    var showCreatePostScreen by remember { mutableStateOf(false) }
    var showCreateListing by remember { mutableStateOf(false) }
    var showItemDetailScreen by remember { mutableStateOf<Int?>(null) }
    var showChatSellerScreen by remember { mutableStateOf<Int?>(null) }
    var showFullScreenImage by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var showProfileViewScreen by remember { mutableStateOf(false) }

    val newsFeedViewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences))
    val marketplaceViewModel: MarketplaceViewModel = viewModel(factory = MarketplaceViewModelFactory(userPreferences))

    // Get the current route
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    if (token == null) {
        AuthenticationNavHost()
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
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
                            onShowCreatePost = { showCreatePostScreen = true },
                            onShowFullScreenImage = { imageUrl, postId -> showFullScreenImage = Pair(imageUrl, postId) },
                            onShowProfileView = { showProfileViewScreen = true }
                        )
                    }
                    composable(NavRoutes.Comments.route) { backStackEntry ->
                        val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: -1
                        CommentScreen(
                            postId = postId,
                            navController = navController,
                            onShowProfileView = { showProfileViewScreen = true }
                        )
                    }
                    composable(NavRoutes.Profile.route) {
                        ProfileScreen { navController.navigate(NavRoutes.Login.route) }
                    }
                    composable(NavRoutes.Marketplace.route) {
                        MarketplaceScreen(
                            userPreferences = userPreferences,
                            navController = navController,
                            onShowCreateListing = { showCreateListing = true },
                            onShowItemDetail = { itemId -> showItemDetailScreen = itemId }
                        )
                    }
                    composable(NavRoutes.Notifications.route) {
                        NotificationsScreen()
                    }
                    composable(NavRoutes.Friends.route) {
                        FriendsScreen(
                            userPreferences = userPreferences,
                            onClose = { navController.popBackStack() }
                        )
                    }
                }
            }

            // Overlay CreatePostScreen
            AnimatedVisibility(
                visible = showCreatePostScreen,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                CreatePostScreen(
                    viewModel = newsFeedViewModel,
                    onClose = { showCreatePostScreen = false }
                )
            }

            // Overlay CreateMarketplaceItemScreen
            AnimatedVisibility(
                visible = showCreateListing,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                CreateMarketplaceItemScreen(
                    userPreferences = userPreferences,
                    onClose = { showCreateListing = false },
                    viewModel = marketplaceViewModel
                )
            }

            // Overlay MarketplaceItemDetailScreen
            showItemDetailScreen?.let { itemId ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                ) {
                    MarketplaceItemDetailScreen(
                        itemId = itemId,
                        navController = navController,
                        onClose = { showItemDetailScreen = null },
                        onClickChat = { showChatSellerScreen = it }
                    )
                }
            }

            // Overlay ChatSellerScreen
            showChatSellerScreen?.let { itemId ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                ) {
                    ChatSellerScreen(
                        itemId = itemId,
                        navController = navController,
                        onClose = { showChatSellerScreen = null }
                    )
                }
            }

            // Overlay FullScreenImageViewer
            showFullScreenImage?.let { (imageUrl, postId) ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                ) {
                    FullScreenImageViewer(
                        imageUrl = imageUrl,
                        onDismiss = { showFullScreenImage = null },
                        onLikeClick = { isLiked ->
                            if (isLiked) newsFeedViewModel.toggleLike(postId, 0)
                            else newsFeedViewModel.unlikePost(postId)
                        },
                        onCommentClick = { navController.navigate(NavRoutes.Comments.createRoute(postId)) }
                    )
                }
            }

            // Overlay ProfileViewScreen
            AnimatedVisibility(
                visible = showProfileViewScreen,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                ProfileViewScreen(
                    navController = navController,
                    context = context,
                    onClose = { showProfileViewScreen = false }
                )
            }
        }
    }
}