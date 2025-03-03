package com.example.raceconnect.view.Navigation

import com.example.raceconnect.R

sealed class BottomNavTab(val title: String, val route: String, val icon: Int) {
    object NewsFeed : BottomNavTab("News Feed", "newsfeed", R.drawable.baseline_home_24)
    object Marketplace : BottomNavTab("Marketplace", "marketplace", R.drawable.baseline_storefront_24)
    object Notifications : BottomNavTab("Notifications", "notifications", R.drawable.baseline_notifications_24)
    object Profile : BottomNavTab("Profile", "profile", R.drawable.baseline_account_circle_24)
}
