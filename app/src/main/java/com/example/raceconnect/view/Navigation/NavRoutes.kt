package com.example.raceconnect.view.Navigation

sealed class NavRoutes(val route: String) {
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
    object ProfileView : NavRoutes("profileView")
    object Friends : NavRoutes("friends")
    object MarketplaceItemDetail : NavRoutes("marketplaceItemDetail/{itemId}") {
        fun createRoute(itemId: Int) = "marketplaceItemDetail/$itemId"
    }
    object ChatSeller : NavRoutes("chatSeller/{itemId}") { // New route for chat seller screen
        fun createRoute(itemId: Int) = "chatSeller/$itemId"
    }
}