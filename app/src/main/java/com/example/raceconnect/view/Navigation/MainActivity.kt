package com.example.raceconnect.view.Navigation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userPreferences = UserPreferences(applicationContext)
            AppNavigation(userPreferences)
        }
    }
}
