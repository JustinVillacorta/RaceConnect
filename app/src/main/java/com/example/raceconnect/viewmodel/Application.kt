package com.example.raceconnect

import android.app.Application
import com.example.raceconnect.datastore.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RaceConnectApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val userPreferences = UserPreferences(this)
        CoroutineScope(Dispatchers.IO).launch {
            userPreferences.migrateOldDataIfNeeded()
        }
    }
}