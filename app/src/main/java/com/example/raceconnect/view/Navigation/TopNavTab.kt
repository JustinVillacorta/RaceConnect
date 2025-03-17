package com.example.raceconnect.view.Navigation

import com.example.raceconnect.R



enum class BottomNavTab(val title: String, val route: String, val icon: Int) {
     NewsFeed("", "newsFeed", R.drawable.baseline_home_24),
    Marketplace("", "marketplace", R.drawable.baseline_storefront_24),
     Notifications ("", "notifications", R.drawable.baseline_notifications_24),
    Profile ("", "profile", R.drawable.baseline_account_circle_24),
   Friends ("", "friends", R.drawable.baseline_person_add_24) // New Friends tab
}
