package com.example.raceconnect

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.raceconnect.ui.NewsFeedScreen

class NewsFeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NewsFeedScreen(
                onNewsFeedActivity = {
                    val intent = Intent(this, ReelsActivity::class.java)
                    startActivity(intent)
                },

                onReelsNavigate = {
                    val intent = Intent(this, ReelsActivity::class.java)
                    startActivity(intent)
                },
                onMarketplaceNavigate = {
                    val intent = Intent(this, MarketplaceActivity::class.java)
                    startActivity(intent)
                },
                onNotificationsNavigate = {
                    val intent = Intent(this, NotificationsActivity::class.java)
                    startActivity(intent)
                },
                onProfileNavigate = {
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                },

            )
        }
    }
}
