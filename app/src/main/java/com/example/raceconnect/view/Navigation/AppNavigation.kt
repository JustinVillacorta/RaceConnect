package com.example.raceconnect.navigation

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.MarketplaceDataClassItem
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.ui.BottomNavBar
import com.example.raceconnect.ui.MarketplaceScreen
import com.example.raceconnect.ui.MenuScreen
import com.example.raceconnect.view.FriendsScreen
import com.example.raceconnect.view.Navigation.AuthenticationNavHost
import com.example.raceconnect.view.Navigation.NavRoutes
import com.example.raceconnect.view.NotificationsScreen
import com.example.raceconnect.view.PostDetailScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.CreateMarketplaceItemScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.EditMarketplaceItemScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.MarketplaceFullScreenImageViewer // Add this import
import com.example.raceconnect.view.Screens.MarketplaceScreens.MarketplaceItemDetailScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.SellerViewMarketplaceItemDetailScreen
import com.example.raceconnect.view.Screens.MarketplaceScreens.ChatSellerScreen
import com.example.raceconnect.view.Screens.MenuScreens.ConversationsScreen
import com.example.raceconnect.view.Screens.MenuScreens.FavoriteItemsScreen
import com.example.raceconnect.view.Screens.MenuScreens.FriendsListScreen
import com.example.raceconnect.view.Screens.MenuScreens.ListedItemsScreen
import com.example.raceconnect.view.Screens.MenuScreens.NewsFeedPreferencesScreen
import com.example.raceconnect.view.Screens.MenuScreens.PostUserProfileViewScreen
import com.example.raceconnect.view.Screens.MenuScreens.ProfileView.UserProfileScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.CommentSectionScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.CreatePostScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.FullScreenImageViewer
import com.example.raceconnect.view.Screens.NewsFeedScreens.NewsFeedScreen
import com.example.raceconnect.view.Screens.NewsFeedScreens.RepostScreen
import com.example.raceconnect.view.Screens.ProfileScreens.MyProfileScreen
import com.example.raceconnect.view.Screens.MenuScreens.ProfileView.EditPostScreen
import com.example.raceconnect.viewmodel.Authentication.AuthenticationViewModel
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModel
import com.example.raceconnect.viewmodel.Marketplace.MarketplaceViewModelFactory
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference.NewsFeedPreferenceViewModelFactory
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedViewModelFactory
import com.example.raceconnect.viewmodel.NotificationClickedViewModelFactory
import com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel.MenuViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.MenuViewModel.MenuViewModelFactory
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModel
import com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel.ProfileDetailsViewModelFactory
import com.google.gson.Gson
import kotlinx.coroutines.launch
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavBackStackEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(userPreferences: UserPreferences) {
    val navController = rememberNavController()
    val token by userPreferences.token.collectAsState(initial = null)
    val context = LocalContext.current
    val user by userPreferences.user.collectAsState(initial = null)
    val loggedInUserId = user?.id ?: 0

    val newsFeedViewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences, context))
    val menuViewModel: MenuViewModel = viewModel(factory = MenuViewModelFactory(userPreferences))
    val profileDetailsViewModel: ProfileDetailsViewModel = viewModel(factory = ProfileDetailsViewModelFactory(userPreferences))

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Define main routes where bottom nav should be visible
    val mainRoutes = listOf(
        NavRoutes.NewsFeed.route,
        NavRoutes.Friends.route,
        NavRoutes.Marketplace.route,
        "notifications",
        NavRoutes.Profile.route
    )

    if (token == null) {
        AuthenticationNavHost()
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                bottomBar = {
                    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: ""
                    if (currentRoute in mainRoutes) {
                        BottomNavBar(navController)
                    }
                },
                snackbarHost = {
                    SnackbarHost(hostState = snackbarHostState) { data ->
                        Snackbar(
                            snackbarData = data,
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = "newsfeed_tab",
                    modifier = Modifier.padding(paddingValues)
                ) {
                    navigation(
                        startDestination = NavRoutes.NewsFeed.route,
                        route = "newsfeed_tab"
                    ) {
                        composable(
                            route = NavRoutes.NewsFeed.route,
                            enterTransition = mainScreenEnterTransition(),
                            exitTransition = mainScreenExitTransition(),
                            popEnterTransition = mainScreenPopEnterTransition(),
                            popExitTransition = mainScreenPopExitTransition()
                        ) {
                            NewsFeedScreen(
                                navController = navController,
                                userPreferences = userPreferences,
                                onShowCreatePost = { navController.navigate(NavRoutes.CreatePost.route) },
                                onShowFullScreenImage = { imageUrls, initialIndex, postId ->
                                    navController.navigate(NavRoutes.FullScreenImage.createRoute(postId, imageUrls, initialIndex))
                                },
                                onShowProfileView = { userId ->
                                    navController.navigate(NavRoutes.ProfileView.createRoute(userId))
                                },
                                onShowRepostScreen = { post ->
                                    val postJson = Gson().toJson(post)
                                    navController.navigate(NavRoutes.RepostScreen.createRoute(postJson))
                                }
                            )
                        }
                        composable(
                            route = NavRoutes.Comments.route,
                            arguments = listOf(navArgument("postId") { type = NavType.IntType }),
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) { backStackEntry ->
                            val postId = backStackEntry.arguments?.getString("postId")?.toIntOrNull() ?: -1
                            CommentSectionScreen(
                                postId = postId,
                                navController = navController,
                                userPreferences = userPreferences,
                                onShowProfileView = { userId ->
                                    navController.navigate(NavRoutes.ProfileView.createRoute(userId))
                                }
                            )
                        }
                        composable(
                            route = NavRoutes.Profile.route,
                            enterTransition = mainScreenEnterTransition(),
                            exitTransition = mainScreenExitTransition(),
                            popEnterTransition = mainScreenPopEnterTransition(),
                            popExitTransition = mainScreenPopExitTransition()
                        ) {
                            val authViewModel: AuthenticationViewModel = viewModel()
                            val marketplaceViewModel: MarketplaceViewModel = viewModel(factory = MarketplaceViewModelFactory(userPreferences))
                            MenuScreen(
                                viewModel = authViewModel,
                                menuViewModel = menuViewModel,
                                profileDetailsViewModel = profileDetailsViewModel,
                                marketplaceViewModel = marketplaceViewModel,
                                onLogoutSuccess = {
                                    navController.navigate(NavRoutes.NewsFeed.route) {
                                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                                        launchSingleTop = true
                                    }
                                },
                                navController = navController,
                                onShowFavoriteItems = { navController.navigate(NavRoutes.FavoriteItems.route) },
                                onShowNewsFeedPreferences = { navController.navigate(NavRoutes.NewsFeedPreferences.route) },
                                onShowListedItems = { navController.navigate(NavRoutes.ListedItems.route) },
                                onShowFriendListScreen = { navController.navigate(NavRoutes.FriendListScreen.route) },
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
                            ),
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) { backStackEntry ->
                            val userId = backStackEntry.arguments?.getInt("userId") ?: 0
                            if (userId == loggedInUserId && userId != 0) {
                                UserProfileScreen(
                                    navController = navController,
                                    context = context,
                                    onClose = { navController.popBackStack() }
                                )
                            } else {
                                PostUserProfileViewScreen(
                                    navController = navController,
                                    context = context,
                                    userId = userId,
                                    onClose = { navController.popBackStack() }
                                )
                            }
                        }
                        composable(
                            route = NavRoutes.ProfileDetails.route,
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) {
                            MyProfileScreen(
                                onClose = { navController.popBackStack() },
                                profileDetailsViewModel = profileDetailsViewModel,
                                userPreferences = userPreferences
                            )
                        }
                        composable(
                            route = "notifications",
                            enterTransition = mainScreenEnterTransition(),
                            exitTransition = mainScreenExitTransition(),
                            popEnterTransition = mainScreenPopEnterTransition(),
                            popExitTransition = mainScreenPopExitTransition()
                        ) {
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
                            ),
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) { backStackEntry ->
                            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                            if (postId != 0) {
                                PostDetailScreen(
                                    navController = navController,
                                    postId = postId,
                                    repostId = null,
                                    userPreferences = userPreferences,
                                    viewModel = viewModel(factory = NotificationClickedViewModelFactory(userPreferences = userPreferences))
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
                            ),
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) { backStackEntry ->
                            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                            val repostId = backStackEntry.arguments?.getInt("repostId") ?: 0
                            if (postId != 0 && repostId != 0) {
                                PostDetailScreen(
                                    navController = navController,
                                    postId = postId,
                                    repostId = repostId,
                                    userPreferences = userPreferences,
                                    viewModel = viewModel(factory = NotificationClickedViewModelFactory(userPreferences = userPreferences))
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
                        composable(
                            route = NavRoutes.Friends.route,
                            enterTransition = mainScreenEnterTransition(),
                            exitTransition = mainScreenExitTransition(),
                            popEnterTransition = mainScreenPopEnterTransition(),
                            popExitTransition = mainScreenPopExitTransition()
                        ) {
                            FriendsScreen(
                                userPreferences = userPreferences,
                                onClose = { navController.popBackStack() },
                                onNavigateToProfile = { userId ->
                                    val userIdInt = userId.toIntOrNull()
                                    if (userIdInt != null) {
                                        navController.navigate(NavRoutes.ProfileView.createRoute(userIdInt))
                                    }
                                }
                            )
                        }
                        composable(
                            route = NavRoutes.FriendListScreen.route,
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) {
                            FriendsListScreen(
                                navController = navController,
                                onClose = { navController.popBackStack() },
                                userPreferences = userPreferences,
                                onNavigateToProfile = { userId ->
                                    val userIdInt = userId.toIntOrNull() ?: return@FriendsListScreen
                                    navController.navigate(NavRoutes.ProfileView.createRoute(userIdInt))
                                }
                            )
                        }
                        composable(
                            route = NavRoutes.MarketplaceItemDetail.route,
                            arguments = listOf(
                                navArgument("itemId") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                    nullable = false
                                }
                            ),
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) { backStackEntry ->
                            val marketplaceViewModel: MarketplaceViewModel = viewModel(factory = MarketplaceViewModelFactory(userPreferences))
                            val itemId = backStackEntry.arguments?.getInt("itemId") ?: -1
                            val items by marketplaceViewModel.marketplaceItems.collectAsState()
                            val userItems by marketplaceViewModel.userItems.collectAsState()
                            var item by remember { mutableStateOf<MarketplaceDataClassItem?>(items.find { it.id == itemId } ?: userItems.find { it.id == itemId }) }

                            LaunchedEffect(itemId) {
                                if (item == null && itemId != -1) {
                                    Log.d("AppNavigation", "Item $itemId not found in local lists, fetching from API...")
                                    item = marketplaceViewModel.fetchItemById(itemId)
                                    Log.d("AppNavigation", "Fetched item: $item")
                                }
                            }

                            var refreshListedItems by remember { mutableStateOf(false) }

                            when {
                                itemId == -1 -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Text("Invalid item ID", color = MaterialTheme.colorScheme.error)
                                    }
                                }
                                item == null -> {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        CircularProgressIndicator()
                                        Text("Loading item...", color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                                item?.seller_id == loggedInUserId && loggedInUserId != 0 -> {
                                    SellerViewMarketplaceItemDetailScreen(
                                        itemId = itemId,
                                        navController = navController,
                                        viewModel = marketplaceViewModel,
                                        onClose = { navController.popBackStack() },
                                        onRefreshListedItems = { refreshListedItems = true }
                                    )
                                }
                                else -> {
                                    MarketplaceItemDetailScreen(
                                        itemId = itemId,
                                        navController = navController,
                                        userPreferences = userPreferences,
                                        onLikeError = { errorMessage ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    message = errorMessage,
                                                    actionLabel = "Retry",
                                                    duration = SnackbarDuration.Short
                                                )
                                            }
                                        },
                                        onMessageSent = { sentMessage ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Message sent: $sentMessage")
                                            }
                                        }
                                    )
                                }
                            }

                            LaunchedEffect(refreshListedItems) {
                                if (refreshListedItems) {
                                    navController.navigate(NavRoutes.ListedItems.route) {
                                        popUpTo(NavRoutes.ListedItems.route) { inclusive = false }
                                        launchSingleTop = true
                                    }
                                    refreshListedItems = false
                                }
                            }
                        }
                        composable(
                            route = NavRoutes.EditMarketplaceItem.route,
                            arguments = listOf(
                                navArgument("itemId") {
                                    type = NavType.IntType
                                    defaultValue = -1
                                    nullable = false
                                }
                            ),
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) { backStackEntry ->
                            val marketplaceViewModel: MarketplaceViewModel = viewModel(factory = MarketplaceViewModelFactory(userPreferences))
                            val itemId = backStackEntry.arguments?.getInt("itemId") ?: -1
                            EditMarketplaceItemScreen(
                                itemId = itemId,
                                navController = navController,
                                viewModel = marketplaceViewModel,
                                onClose = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = NavRoutes.ChatSeller.route,
                            arguments = listOf(
                                navArgument("itemId") { type = NavType.IntType },
                                navArgument("conversationId") { type = NavType.IntType },
                                navArgument("sellerId") { type = NavType.IntType },
                                navArgument("itemTitle") { type = NavType.StringType },
                                navArgument("itemImage") { type = NavType.StringType; nullable = true }
                            ),
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) { backStackEntry ->
                            val itemId = backStackEntry.arguments?.getInt("itemId") ?: 0
                            val conversationId = backStackEntry.arguments?.getInt("conversationId") ?: 0
                            val sellerId = backStackEntry.arguments?.getInt("sellerId") ?: 0
                            val itemTitle = backStackEntry.arguments?.getString("itemTitle") ?: "Chat"
                            val itemImage = backStackEntry.arguments?.getString("itemImage")

                            ChatSellerScreen(
                                itemId = itemId,
                                conversationId = conversationId,
                                sellerId = sellerId,
                                itemTitle = itemTitle,
                                itemImage = itemImage,
                                navController = navController,
                                userPreferences = userPreferences,
                                onClose = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = NavRoutes.ListedItems.route,
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) {
                            ListedItemsScreen(
                                navController = navController,
                                userPreferences = userPreferences,
                                onClose = { navController.popBackStack() },
                                onRefresh = { /* No-op here, handled by LaunchedEffect in ListedItemsScreen */ }
                            )
                        }
                        composable(
                            route = NavRoutes.FavoriteItems.route,
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) {
                            val marketplaceViewModel: MarketplaceViewModel = viewModel(factory = MarketplaceViewModelFactory(userPreferences))
                            FavoriteItemsScreen(
                                navController = navController,
                                userPreferences = userPreferences,
                                onClose = { navController.popBackStack() },
                                onShowItemDetail = { itemId -> navController.navigate(NavRoutes.MarketplaceItemDetail.createRoute(itemId)) },
                                viewModel = marketplaceViewModel
                            )
                        }
                        composable(
                            route = NavRoutes.Conversations.route,
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) {
                            ConversationsScreen(
                                navController = navController,
                                userPreferences = userPreferences
                            )
                        }
                        composable(
                            route = NavRoutes.CreatePost.route,
                            enterTransition = { slideInVertically(initialOffsetY = { it }) },
                            exitTransition = { slideOutVertically(targetOffsetY = { it }) },
                            popEnterTransition = { slideInVertically(initialOffsetY = { it }) },
                            popExitTransition = { slideOutVertically(targetOffsetY = { it }) }
                        ) {
                            CreatePostScreen(
                                viewModel = newsFeedViewModel,
                                onClose = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = NavRoutes.Marketplace.route,
                            enterTransition = mainScreenEnterTransition(),
                            exitTransition = mainScreenExitTransition(),
                            popEnterTransition = mainScreenPopEnterTransition(),
                            popExitTransition = mainScreenPopExitTransition()
                        ) {
                            val marketplaceViewModel: MarketplaceViewModel = viewModel(factory = MarketplaceViewModelFactory(userPreferences))
                            MarketplaceScreen(
                                userPreferences = userPreferences,
                                navController = navController,
                                onShowCreateListing = { navController.navigate(NavRoutes.CreateMarketplaceItem.route) },
                                onShowItemDetail = { itemId -> navController.navigate(NavRoutes.MarketplaceItemDetail.createRoute(itemId)) },
                                viewModel = marketplaceViewModel
                            )
                        }
                        composable(
                            route = NavRoutes.CreateMarketplaceItem.route,
                            enterTransition = { slideInVertically(initialOffsetY = { it }) },
                            exitTransition = { slideOutVertically(targetOffsetY = { it }) },
                            popEnterTransition = { slideInVertically(initialOffsetY = { it }) },
                            popExitTransition = { slideOutVertically(targetOffsetY = { it }) }
                        ) {
                            val marketplaceViewModel: MarketplaceViewModel = viewModel(factory = MarketplaceViewModelFactory(userPreferences))
                            CreateMarketplaceItemScreen(
                                userPreferences = userPreferences,
                                onClose = { navController.popBackStack() },
                                viewModel = marketplaceViewModel
                            )
                        }
                        composable(
                            route = "fullScreenImage/{postId}/{imageUrls}/{initialIndex}",
                            arguments = listOf(
                                navArgument("postId") { type = NavType.IntType },
                                navArgument("imageUrls") { type = NavType.StringType },
                                navArgument("initialIndex") { type = NavType.IntType }
                            ),
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) { backStackEntry ->
                            val postId = backStackEntry.arguments?.getInt("postId") ?: 0
                            val imageUrlsString = backStackEntry.arguments?.getString("imageUrls") ?: ""
                            val initialIndex = backStackEntry.arguments?.getInt("initialIndex") ?: 0
                            val imageUrls = imageUrlsString.split(",").map { Uri.decode(it) }.filter { it.isNotEmpty() }

                            Log.d("FullScreenImage", "Nav Args - postId: $postId, imageUrlsString: '$imageUrlsString', initialIndex: $initialIndex")
                            Log.d("FullScreenImage", "Parsed imageUrls: $imageUrls")

                            if (imageUrls.isEmpty()) {
                                Log.e("FullScreenImage", "No valid image URLs found, navigating back")
                                navController.popBackStack()
                                return@composable
                            }

                            val safeInitialIndex = initialIndex.coerceIn(0, imageUrls.size - 1)
                            if (safeInitialIndex != initialIndex) {
                                Log.w("FullScreenImage", "Adjusted initialIndex from $initialIndex to $safeInitialIndex due to bounds")
                            }

                            val newsFeedViewModel: NewsFeedViewModel = viewModel(factory = NewsFeedViewModelFactory(userPreferences, LocalContext.current))

                            FullScreenImageViewer(
                                imageUrls = imageUrls,
                                initialIndex = safeInitialIndex,
                                postId = postId,
                                onDismiss = { navController.popBackStack() },
                                onLikeClick = { isLiked ->
                                    if (isLiked) newsFeedViewModel.toggleLike(postId, 0)
                                    else newsFeedViewModel.unlikePost(postId)
                                },
                                onCommentClick = { navController.navigate(NavRoutes.Comments.createRoute(postId)) }
                            )
                        }
                        // Add Marketplace Fullscreen Image Viewer
                        composable(
                            route = "marketplaceFullScreenImage/{marketplaceItemId}/{imageUrls}/{initialIndex}",
                            arguments = listOf(
                                navArgument("marketplaceItemId") { type = NavType.IntType },
                                navArgument("imageUrls") { type = NavType.StringType },
                                navArgument("initialIndex") { type = NavType.IntType }
                            ),
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) { backStackEntry ->
                            val marketplaceItemId = backStackEntry.arguments?.getInt("marketplaceItemId") ?: 0
                            val imageUrlsString = backStackEntry.arguments?.getString("imageUrls") ?: ""
                            val initialIndex = backStackEntry.arguments?.getInt("initialIndex") ?: 0
                            val imageUrls = imageUrlsString.split(",").map { Uri.decode(it) }.filter { it.isNotEmpty() }

                            if (imageUrls.isEmpty()) {
                                Log.e("MarketplaceFullScreenImage", "No valid image URLs found, navigating back")
                                navController.popBackStack()
                                return@composable
                            }

                            val safeInitialIndex = initialIndex.coerceIn(0, imageUrls.size - 1)
                            if (safeInitialIndex != initialIndex) {
                                Log.w("MarketplaceFullScreenImage", "Adjusted initialIndex from $initialIndex to $safeInitialIndex due to bounds")
                            }

                            MarketplaceFullScreenImageViewer(
                                imageUrls = imageUrls,
                                initialIndex = safeInitialIndex,
                                marketplaceItemId = marketplaceItemId,
                                onDismiss = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = NavRoutes.EditPost.route,
                            arguments = listOf(navArgument("postJson") { type = NavType.StringType }),
                            enterTransition = { slideInVertically(initialOffsetY = { it }) },
                            exitTransition = { slideOutVertically(targetOffsetY = { it }) },
                            popEnterTransition = { slideInVertically(initialOffsetY = { it }) },
                            popExitTransition = { slideOutVertically(targetOffsetY = { it }) }
                        ) { backStackEntry ->
                            val postJson = backStackEntry.arguments?.getString("postJson")?.let { Uri.decode(it) }
                            val post = postJson?.let { Gson().fromJson(it, NewsFeedDataClassItem::class.java) }
                            if (post != null) {
                                EditPostScreen(
                                    post = post,
                                    viewModel = newsFeedViewModel,
                                    onClose = { navController.popBackStack() }
                                )
                            } else {
                                LaunchedEffect(Unit) { navController.popBackStack() }
                            }
                        }
                        composable(
                            route = NavRoutes.RepostScreen.route,
                            arguments = listOf(navArgument("postJson") { type = NavType.StringType }),
                            enterTransition = { slideInVertically(initialOffsetY = { it }) },
                            exitTransition = { slideOutVertically(targetOffsetY = { it }) },
                            popEnterTransition = { slideInVertically(initialOffsetY = { it }) },
                            popExitTransition = { slideOutVertically(targetOffsetY = { it }) }
                        ) { backStackEntry ->
                            val postJson = backStackEntry.arguments?.getString("postJson")?.let { Uri.decode(it) }
                            val post = postJson?.let { Gson().fromJson(it, NewsFeedDataClassItem::class.java) }
                            if (post != null) {
                                RepostScreen(
                                    post = post,
                                    navController = navController,
                                    viewModel = newsFeedViewModel,
                                    onClose = { navController.popBackStack() },
                                    userPreferences = userPreferences
                                )
                            } else {
                                LaunchedEffect(Unit) { navController.popBackStack() }
                            }
                        }
                        composable(
                            route = NavRoutes.NewsFeedPreferences.route,
                            enterTransition = { slideInHorizontally(initialOffsetX = { it }) },
                            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) },
                            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) },
                            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) }
                        ) {
                            NewsFeedPreferencesScreen(
                                navController = navController,
                                onClose = { navController.popBackStack() },
                                factory = NewsFeedPreferenceViewModelFactory(userPreferences)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Reusable animation functions and tabOrder remain unchanged
val tabOrder = mapOf(
    NavRoutes.NewsFeed.route to 0,
    NavRoutes.Friends.route to 1,
    NavRoutes.Marketplace.route to 2,
    "notifications" to 3,
    NavRoutes.Profile.route to 4
)

fun mainScreenEnterTransition(): @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route
    val initialIndex = tabOrder[initialRoute] ?: -1
    val targetIndex = tabOrder[targetRoute] ?: -1
    if (initialIndex != -1 && targetIndex != -1 && initialIndex != targetIndex) {
        if (targetIndex > initialIndex) {
            slideInHorizontally(initialOffsetX = { it })
        } else {
            slideInHorizontally(initialOffsetX = { -it })
        }
    } else {
        slideInHorizontally(initialOffsetX = { it })
    }
}

fun mainScreenExitTransition(): @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route
    val initialIndex = tabOrder[initialRoute] ?: -1
    val targetIndex = tabOrder[targetRoute] ?: -1
    if (initialIndex != -1 && targetIndex != -1 && initialIndex != targetIndex) {
        if (targetIndex > initialIndex) {
            slideOutHorizontally(targetOffsetX = { -it })
        } else {
            slideOutHorizontally(targetOffsetX = { it })
        }
    } else {
        slideOutHorizontally(targetOffsetX = { -it })
    }
}

fun mainScreenPopEnterTransition(): @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)? = {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route
    val initialIndex = tabOrder[initialRoute] ?: -1
    val targetIndex = tabOrder[targetRoute] ?: -1
    if (initialIndex != -1 && targetIndex != -1 && initialIndex != targetIndex) {
        if (targetIndex < initialIndex) {
            slideInHorizontally(initialOffsetX = { -it })
        } else {
            slideInHorizontally(initialOffsetX = { it })
        }
    } else {
        slideInHorizontally(initialOffsetX = { -it })
    }
}

fun mainScreenPopExitTransition(): @JvmSuppressWildcards() (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)? = {
    val initialRoute = initialState.destination.route
    val targetRoute = targetState.destination.route
    val initialIndex = tabOrder[initialRoute] ?: -1
    val targetIndex = tabOrder[targetRoute] ?: -1
    if (initialIndex != -1 && targetIndex != -1 && initialIndex != targetIndex) {
        if (targetIndex < initialIndex) {
            slideOutHorizontally(targetOffsetX = { it })
        } else {
            slideOutHorizontally(targetOffsetX = { -it })
        }
    } else {
        slideOutHorizontally(targetOffsetX = { it })
    }
}