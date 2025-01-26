package com.example.raceconnect.View.Activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.raceconnect.View.Screens.MainScreen
import com.example.raceconnect.ui.ReelsScreen


class ReelsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReelsScreen()
            MainScreen()
        }
    }
}
