package com.example.raceconnect.View.Activities

import com.example.raceconnect.R

sealed class TopNavTab(val title: String, val route: String, val icon: Int) {
    object NewsFeed : TopNavTab("News Feed", "newsfeed", R.drawable.baseline_home_24)
    object Reels : TopNavTab("Reels", "reels", R.drawable.baseline_ondemand_video_24)
    object Marketplace : TopNavTab("Marketplace", "marketplace", R.drawable.baseline_storefront_24)
    object Notifications : TopNavTab("Notifications", "notifications", R.drawable.baseline_notifications_24)
    object Profile : TopNavTab("Profile", "profile", R.drawable.baseline_account_circle_24)
}
