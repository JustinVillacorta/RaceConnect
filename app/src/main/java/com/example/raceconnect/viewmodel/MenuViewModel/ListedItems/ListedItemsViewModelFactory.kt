package com.example.raceconnect.viewmodel.MenuViewModel.ListedItems

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.raceconnect.datastore.UserPreferences

class ListedItemsViewModelFactory(
    private val userPreferences: UserPreferences,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListedItemsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ListedItemsViewModel(userPreferences, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}