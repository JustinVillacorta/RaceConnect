package com.example.raceconnect.viewmodel.NewsFeed

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference.NewsFeedPreferenceViewModel
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference.NewsFeedPreferenceViewModelFactory

class NewsFeedViewModelFactory(
    private val userPreferences: UserPreferences,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsFeedViewModel::class.java)) {
            val preferenceViewModelFactory = NewsFeedPreferenceViewModelFactory(userPreferences)
            val preferenceViewModel = preferenceViewModelFactory.create(NewsFeedPreferenceViewModel::class.java)
            return NewsFeedViewModel(userPreferences, preferenceViewModel, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}