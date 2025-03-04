package com.example.raceconnect.view.Navigation

import com.example.raceconnect.R



sealed class BottomNavTab(val title: String, val route: String, val icon: Int) {
    object NewsFeed : BottomNavTab("", "newsfeed", R.drawable.baseline_home_24)
    object Marketplace : BottomNavTab("", "marketplace", R.drawable.baseline_storefront_24)
    object Notifications : BottomNavTab("", "notifications", R.drawable.baseline_notifications_24)
    object Profile : BottomNavTab("", "profile", R.drawable.baseline_account_circle_24)
    object Friends : BottomNavTab("", "friends", R.drawable.baseline_person_add_24) // New Friends tab
}
