package com.example.raceconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.network.ApiService
import com.example.raceconnect.network.RetrofitInstance

class NotificationClickedViewModelFactory(
    private val apiService: ApiService = RetrofitInstance.api,
    private val userPreferences: UserPreferences // No longer optional
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationClickedViewModel::class.java)) {
            return NotificationClickedViewModel(apiService, userPreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}