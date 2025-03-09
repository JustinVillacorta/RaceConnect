package com.example.raceconnect.viewmodel.ProfileDetails.ProfileDetailsViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.raceconnect.datastore.UserPreferences

class ProfileDetailsViewModelFactory(private val userPreferences: UserPreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfileDetailsViewModel(userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}