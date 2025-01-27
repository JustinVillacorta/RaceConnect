package com.example.raceconnect.view.Activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.raceconnect.view.Screens.MainScreen
import com.example.raceconnect.ui.MarketplaceNavHost


class MarketplaceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MarketplaceNavHost()
            MainScreen()
        }
    }
}
