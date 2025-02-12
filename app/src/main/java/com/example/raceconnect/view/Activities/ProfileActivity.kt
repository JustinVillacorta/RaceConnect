package com.example.raceconnect.view.Activities

import ProfileScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.view.Screens.MainScreen


class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProfileScreen()
            val userPreferences = UserPreferences(this)
            MainScreen(userPreferences = userPreferences)
        }
    }
}
