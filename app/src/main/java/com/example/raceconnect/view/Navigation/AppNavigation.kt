package com.example.raceconnect.navigation

import NewsFeedScreen
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
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.ui.BottomNavBar
import com.example.raceconnect.ui.MarketplaceScreen
import com.example.raceconnect.ui.ProfileScreen
import com.example.raceconnect.view.FriendsScreen
import com.example.raceconnect.view.Navigation.AuthenticationNavHost
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.NotificationsScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.ChatSellerScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.CreateMarketplaceItemScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.MarketplaceItemDetailScreen
import com.example.raceconnect.view.Screens.MenuScreens.FavoriteItemsScreen
import com.example.raceconnect.view.Screens.MenuScreens.ListedItemsScreen
import com.example.raceconnect.view.Screens.MenuScreens.NewsFeedPreferencesScreen
import com.example.raceconnect.view.Screens.MenuScreens.SettingsScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.CommentSectionScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.CreatePostScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.FullScreenImageViewer
import com.example.raceconnect.view.Screens.NewsFeedScreens.RepostScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.UserProfileScreen
import com.example.raceconnect.view.Screens.ProfileScreens.MyProfileScreen
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModelFactory
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel.MenuViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel.MenuViewModelFactory
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModelFactory

@Composable
fun AppNavigation(userPreferences: UserPreferences) {
    val navController = rememberNavController()
    val token = userPreferences.token.collectAsState(initial = null).value
    val context = LocalContext.current

    // State for overlays
    var showCreatePostScreen by remember { mutableStateOf(false) }
    var showCreateListing by remember { mutableStateOf(false) }
    var showItemDetailScreen by remember { mutableStateOf<Int?>(null) }
    var showChatSellerScreen by remember { mutableStateOf<Int?>(null) }
    var showFullScreenImage by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var showRepostScreen by remember { mutableStateOf<NewsFeedDataClassItem?>(null) }
    var showMyProfile by remember { mutableStateOf(false) }
    var showFavoriteItems by remember { mutableStateOf(false) }
    var showNewsFeedPreferences by remember { mutableStateOf(false) }
    var showListedItems by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    val newsFeedViewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences))
    val marketplaceViewModel: MarketplaceViewModel = viewModel(factory = MarketplaceViewModelFactory(userPreferences))
    val menuViewModel: MenuViewModel = viewModel(factory = MenuViewModelFactory(userPreferences))
    val profileDetailsViewModel: ProfileDetailsViewModel = viewModel(factory = ProfileDetailsViewModelFactory(userPreferences))

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
                        NewsFeedScreen( // Changed to NewsFeedScreen
                            navController = navController,
                            userPreferences = userPreferences,
                            onShowCreatePost = { showCreatePostScreen = true },
                            onShowFullScreenImage = { imageUrl, postId -> showFullScreenImage = Pair(imageUrl, postId) },
                            onShowProfileView = { navController.navigate(NavRoutes.ProfileView.route) },
                            onShowRepostScreen = { post -> showRepostScreen = post }
                        )
                    }
                    composable(NavRoutes.Comments.route) { backStackEntry ->
                        val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: -1
                        CommentSectionScreen(
                            postId = postId,
                            navController = navController,
                            userPreferences = userPreferences,
                            onShowProfileView = { navController.navigate(NavRoutes.ProfileView.route) }
                        )
                    }
                    composable(NavRoutes.Profile.route) {
                        val authViewModel: AuthenticationViewModel = viewModel()
                        ProfileScreen(
                            viewModel = authViewModel,
                            menuViewModel = menuViewModel,
                            onLogoutSuccess = {
                                navController.navigate(NavRoutes.Login.route) {
                                    popUpTo(NavRoutes.Login.route) { inclusive = true }
                                }
                            },
                            navController = navController,
                            onShowMyProfile = { showMyProfile = true },
                            onShowFavoriteItems = { showFavoriteItems = true },
                            onShowNewsFeedPreferences = { showNewsFeedPreferences = true },
                            onShowListedItems = { showListedItems = true },
                            onShowSettings = { showSettings = true },
                            profileDetailsViewModel = profileDetailsViewModel
                        )
                    }
                    composable(NavRoutes.ProfileView.route) {
                        UserProfileScreen(
                            navController = navController,
                            context = context,
                            onClose = { navController.popBackStack() }
                        )
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
                        NotificationsScreen(context = context)
                    }
                    composable(NavRoutes.Friends.route) {
                        FriendsScreen(
                            userPreferences = userPreferences,
                            onClose = { navController.popBackStack() }
                        )
                    }
                    composable(NavRoutes.MarketplaceItemDetail.route) { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull() ?: -1
                        MarketplaceItemDetailScreen(
                            itemId = itemId,
                            navController = navController,
                            onClose = { navController.popBackStack() },
                            viewModel = marketplaceViewModel,
                            onClickChat = { showChatSellerScreen = it }
                        )
                    }
                    composable(NavRoutes.ChatSeller.route) { backStackEntry ->
                        val itemId = backStackEntry.arguments?.getString("itemId")?.toIntOrNull() ?: -1
                        ChatSellerScreen(
                            itemId = itemId,
                            navController = navController,
                            onClose = { showChatSellerScreen = null }
                        )
                    }
                }
            }

            // Overlay for CreatePostScreen
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

            // Overlay for CreateMarketplaceItemScreen
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

            // Overlay for MarketplaceItemDetailScreen
            showItemDetailScreen?.let { itemId ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                ) {
                    MarketplaceItemDetailScreen(
                        itemId = itemId,
                        navController = navController,
                        viewModel = marketplaceViewModel,
                        onClose = { showItemDetailScreen = null },
                        onClickChat = { showChatSellerScreen = it }
                    )
                }
            }

            // Overlay for ChatSellerScreen
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

            // Overlay for FullScreenImageViewer
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

            // Overlay for RepostScreen
            showRepostScreen?.let { post ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
                ) {
                    RepostScreen(
                        post = post,
                        navController = navController,
                        viewModel = newsFeedViewModel,
                        onClose = { showRepostScreen = null }
                    )
                }
            }

            // Overlays for menu options
            // Inside AppNavigation.kt
            AnimatedVisibility(
                visible = showMyProfile,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                MyProfileScreen(
                    onClose = {
                        showMyProfile = false // Close the overlay and return to ProfileScreen
                        profileDetailsViewModel.loadProfileData() // Refresh profile data after saving
                    },
                    profileDetailsViewModel = profileDetailsViewModel,
                    userPreferences = userPreferences
                )
            }

            AnimatedVisibility(
                visible = showFavoriteItems,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                FavoriteItemsScreen(
                    navController = navController,
                    menuViewModel = menuViewModel,
                    onClose = { showFavoriteItems = false }
                )
            }

            AnimatedVisibility(
                visible = showNewsFeedPreferences,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                NewsFeedPreferencesScreen(
                    navController = navController,
                    menuViewModel = menuViewModel,
                    onClose = { showNewsFeedPreferences = false }
                )
            }

            AnimatedVisibility(
                visible = showListedItems,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                ListedItemsScreen(
                    navController = navController,
                    menuViewModel = menuViewModel,
                    onClose = { showListedItems = false }
                )
            }

            AnimatedVisibility(
                visible = showSettings,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut(animationSpec = tween(300))
            ) {
                SettingsScreen(
                    navController = navController,
                    menuViewModel = menuViewModel,
                    onClose = { showSettings = false }
                )
            }
        }
    }
}