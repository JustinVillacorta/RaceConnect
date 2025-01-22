package com.example.raceconnect


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.raceconnect.ui.*

class NewsFeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainScreen()
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { TopNavBar(navController = navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = TopNavTab.NewsFeed.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(TopNavTab.NewsFeed.route) { NewsFeedScreen() }
            composable(TopNavTab.Reels.route) { ReelsScreen() }
            composable(TopNavTab.Marketplace.route) { MarketplaceScreen() }
            composable(TopNavTab.Notifications.route) { NotficationScreen() }
            composable(TopNavTab.Profile.route) { ProfileScreen() }
        }
    }
}
