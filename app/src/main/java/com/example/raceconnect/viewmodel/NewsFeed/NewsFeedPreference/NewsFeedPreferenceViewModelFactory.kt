package com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.network.ApiService
import com.example.raceconnect.network.RetrofitInstance
import android.util.Log

class NewsFeedPreferenceViewModelFactory(
    private val userPreferences: UserPreferences
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewsFeedPreferenceViewModel::class.java)) {
            Log.d("NewsFeedPreferenceViewModelFactory", "Creating NewsFeedPreferenceViewModel with userPreferences")
            // Explicitly pass RetrofitInstance.api to ensure it's provided
            return NewsFeedPreferenceViewModel(userPreferences, RetrofitInstance.api) as T
        }
        Log.e("NewsFeedPreferenceViewModelFactory", "Unknown ViewModel class: ${modelClass.name}")
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}