package com.example.raceconnect.view.Activities
import NewsFeedAppNavigation
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.view.Screens.MainScreen



class NewsFeedActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val userPreferences = UserPreferences(this)
            MainScreen(userPreferences = userPreferences)




        }
    }
}


