package com.example.raceconnect.view.Navigation

import android.net.Uri

sealed class NavRoutes(val route: String) {
    // Existing routes remain unchanged
    object Login : NavRoutes("login")
    object Signup : NavRoutes("signup")
    object ForgotPassword : NavRoutes("forgot_password")
    object VerifyOtp : NavRoutes("verify_otp/{email}") {
        fun createRoute(email: String) = "verify_otp/$email"
    }
    object ResetPassword : NavRoutes("reset_password/{email}") {
        fun createRoute(email: String) = "reset_password/$email"
    }
    object NewsFeed : NavRoutes("newsFeed")
    object Comments : NavRoutes("comments/{postId}") {
        fun createRoute(postId: Int) = "comments/$postId"
    }
    object Profile : NavRoutes("profile")
    object CreatePost : NavRoutes("createPost")
    object Marketplace : NavRoutes("marketplace")
    object Notifications : NavRoutes("notifications")
    object Post : NavRoutes("postDetail/{postId}") {
        fun createRoute(postId: Int) = "postDetail/$postId"
    }
    object Repost : NavRoutes("postDetail/{postId}/repost/{repostId}") {
        fun createRoute(postId: Int, repostId: Int) = "postDetail/$postId/repost/$repostId"
    }
    object ProfileView : NavRoutes("profileView/{userId}") {
        fun createRoute(userId: Int) = "profileView/$userId"
    }
    object Friends : NavRoutes("friends")
    object MarketplaceItemDetail : NavRoutes("marketplaceItemDetail/{itemId}") {
        fun createRoute(itemId: Int) = "marketplaceItemDetail/$itemId"
    }
    object EditMarketplaceItem : NavRoutes("editMarketplaceItem/{itemId}") {
        fun createRoute(itemId: Int) = "editMarketplaceItem/$itemId"
    }
    object ChatSeller : NavRoutes("chatSeller/{itemId}/{conversationId}/{sellerId}/{itemTitle}/{itemImage}") {
        fun createRoute(itemId: Int, conversationId: Int, sellerId: Int, itemTitle: String, itemImage: String?) =
            "chatSeller/$itemId/$conversationId/$sellerId/${Uri.encode(itemTitle)}/${Uri.encode(itemImage ?: "")}"
    }
    object ProfileDetails : NavRoutes("profileDetails")
    object FavoriteItems : NavRoutes("favoriteItems")
    object NewsFeedPreferences : NavRoutes("newsFeedPreferences")
    object ListedItems : NavRoutes("listedItems")
    object FriendListScreen : NavRoutes("FriendsListScreen")
    object Conversations : NavRoutes("conversations")
    object CreateMarketplaceItem : NavRoutes("createMarketplaceItem")
    object FullScreenImage : NavRoutes("fullScreenImage/{postId}/{imageUrl}") {
        fun createRoute(postId: Int, imageUrl: String) = "fullScreenImage/$postId/${imageUrl.replace("/", "%2F")}"
    }

    // New EditPost route
    object EditPost : NavRoutes("editPost/{postJson}") {
        fun createRoute(postJson: String) = "editPost/${Uri.encode(postJson)}"
    }

    object RepostScreen {
        const val route = "repost_screen/{postJson}"
        fun createRoute(postJson: String) = "repost_screen/${Uri.encode(postJson)}"
    }
}