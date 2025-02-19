package com.example.raceconnect.viewmodel.NewsFeed

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.raceconnect.datastore.UserPreferences

class NewsFeedViewModelFactory(
    private val application: Application,
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsFeedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NewsFeedViewModel(application, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}