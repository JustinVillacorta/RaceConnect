package com.example.raceconnect.navigation

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.ui.BottomNavBar
import com.example.raceconnect.ui.MarketplaceScreen
import com.example.raceconnect.ui.ProfileScreen
import com.example.raceconnect.view.FriendsScreen
import com.example.raceconnect.view.Navigation.AuthenticationNavHost
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.NotificationsScreen
import com.example.raceconnect.view.PostDetailScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.ChatSellerScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.CreateMarketplaceItemScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.MarketplaceItemDetailScreen
import com.example.raceconnect.view.Screens.MenuScreens.FavoriteItemsScreen
import com.example.raceconnect.view.Screens.MenuScreens.ListedItemsScreen
import com.example.raceconnect.view.Screens.MenuScreens.NewsFeedPreferencesScreen
import com.example.raceconnect.view.Screens.MenuScreens.PostUserProfileViewScreen
import com.example.raceconnect.view.Screens.MenuScreens.SettingsScreen
import com.example.raceconnect.view.Screens.MenuScreens.UserProfileScreen
import com.example.raceconnect.view.Screens.ProfileScreens.MyProfileScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.CommentSectionScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.CreatePostScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.FullScreenImageViewer
import com.example.raceconnect.view.Screens.NewsFeedScreens.NewsFeedScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.RepostScreen
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModelFactory
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference.NewsFeedPreferenceViewModelFactory
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.example.raceconnect.viewmodel.NotificationClickedViewModel
import com.example.raceconnect.viewmodel.NotificationClickedViewModelFactory
import com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel.MenuViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel.MenuViewModelFactory
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModelFactory
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(userPreferences: UserPreferences) {
    val navController = rememberNavController()
    // Perform DataStore migration before proceeding
    runBlocking {
        userPreferences.migrateOldDataIfNeeded()
        Log.d("AppNavigation", "DataStore migration completed")
    }

    val token = userPreferences.token.collectAsState(initial = null).value
    val context = LocalContext.current
    val user by userPreferences.user.collectAsState(initial = null)
    val loggedInUserId = user?.id ?: 0

    // State for overlays
    var showCreatePostScreen by remember { mutableStateOf(false) }
    var showCreateListing by remember { mutableStateOf(false) }
    var showItemDetailScreen by remember { mutableStateOf<Int?>(null) }
    var showChatSellerScreen by remember { mutableStateOf<Int?>(null) }
    var showFullScreenImage by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var showRepostScreen by remember { mutableStateOf<NewsFeedDataClassItem?>(null) }
    var showFavoriteItems by remember { mutableStateOf(false) }
    var showNewsFeedPreferences by remember { mutableStateOf(false) }
    var showListedItems by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    // Use the new factory method with context and userPreferences
    val newsFeedViewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences, context))
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
                        NewsFeedScreen(
                            navController = navController,
                            userPreferences = userPreferences,
                            onShowCreatePost = { showCreatePostScreen = true },
                            onShowFullScreenImage = { imageUrl, postId -> showFullScreenImage = Pair(imageUrl, postId) },
                            onShowProfileView = { navController.navigate(NavRoutes.ProfileView.createRoute(loggedInUserId)) },
                            onShowRepostScreen = { post -> showRepostScreen = post }
                        )
                    }
                    composable(NavRoutes.Comments.route) { backStackEntry ->
                        val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: -1
                        CommentSectionScreen(
                            postId = postId,
                            navController = navController,
                            userPreferences = userPreferences,
                            onShowProfileView = { navController.navigate(NavRoutes.ProfileView.createRoute(loggedInUserId)) }
                        )
                    }
                    composable(NavRoutes.Profile.route) {
                        val authViewModel: AuthenticationViewModel = viewModel()
                        ProfileScreen(
                            viewModel = authViewModel,
                            menuViewModel = menuViewModel,
                            profileDetailsViewModel = profileDetailsViewModel,
                            onLogoutSuccess = {
                                navController.navigate(NavRoutes.Login.route) {
                                    popUpTo(NavRoutes.Login.route) { inclusive = true }
                                }
                            },
                            navController = navController,
                            onShowFavoriteItems = { showFavoriteItems = true },
                            onShowNewsFeedPreferences = { showNewsFeedPreferences = true },
                            onShowListedItems = { showListedItems = true },
                            onShowSettings = { showSettings = true },
                            userPreferences = userPreferences
                        )
                    }
                    composable(
                        NavRoutes.ProfileView.route,
                        arguments = listOf(
                            navArgument("userId") {
                                type = NavType.IntType
                                defaultValue = 0
                                nullable = false
                            }
                        )
                    ) { backStackEntry ->
                        val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                        Log.d("AppNavigation", "ProfileView route accessed with userId: $userId, loggedInUserId: $loggedInUserId")
                        if (userId == loggedInUserId && userId != 0) {
                            Log.d("AppNavigation", "Navigating to UserProfileScreen for logged-in user (userId: $userId)")
                            UserProfileScreen(
                                navController = navController,
                                context = context,
                                onClose = { navController.popBackStack() }
                            )
                        } else {
                            Log.d("AppNavigation", "Navigating to PostUserProfileViewScreen for userId: $userId")
                            PostUserProfileViewScreen(
                                navController = navController,
                                context = context,
                                userId = userId,
                                onClose = { navController.popBackStack() }
                            )
                        }
                    }
                    composable(NavRoutes.ProfileDetails.route) {
                        Log.d("AppNavigation", "Navigating to ProfileDetails route for logged-in user (userId: $loggedInUserId)")
                        MyProfileScreen(
                            onClose = { navController.popBackStack() },
                            profileDetailsViewModel = profileDetailsViewModel,
                            userPreferences = userPreferences
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
                    composable("notifications") {
                        NotificationsScreen(context = LocalContext.current, navController = navController)
                    }
                    composable(
                        NavRoutes.Post.route,
                        arguments = listOf(
                            navArgument("postId") {
                                type = NavType.IntType
                                defaultValue = 0
                                nullable = false
                            }
                        )
                    ) { backStackEntry ->
                        val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                        if (postId != 0) {
                            PostDetailScreen(
                                navController = navController,
                                postId = postId,
                                repostId = null,
                                userPreferences = userPreferences,
                                viewModel = viewModel(factory = NotificationClickedViewModelFactory())
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Invalid post ID",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    composable(
                        NavRoutes.Repost.route,
                        arguments = listOf(
                            navArgument("postId") {
                                type = NavType.IntType
                                defaultValue = 0
                                nullable = false
                            },
                            navArgument("repostId") {
                                type = NavType.IntType
                                defaultValue = 0
                                nullable = false
                            }
                        )
                    ) { backStackEntry ->
                        val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                        val repostId = backStackEntry.arguments?.getInt("repostId") ?: 0
                        if (postId != 0 && repostId != 0) {
                            PostDetailScreen(
                                navController = navController,
                                postId = postId,
                                repostId = repostId,
                                userPreferences = userPreferences,
                                viewModel = viewModel(factory = NotificationClickedViewModelFactory())
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (postId == 0) "Invalid post ID" else "Invalid repost ID",
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
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
                    composable(NavRoutes.NewsFeedPreferences.route) {
                        NewsFeedPreferencesScreen(
                            navController = navController,
                            onClose = { navController.popBackStack() },
                            factory = NewsFeedPreferenceViewModelFactory(userPreferences)
                        )
                    }
                }
            }

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
                        onClose = { showRepostScreen = null },
                        userPreferences = userPreferences
                    )
                }
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
                    onClose = { showNewsFeedPreferences = false },
                    factory = NewsFeedPreferenceViewModelFactory(userPreferences)
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