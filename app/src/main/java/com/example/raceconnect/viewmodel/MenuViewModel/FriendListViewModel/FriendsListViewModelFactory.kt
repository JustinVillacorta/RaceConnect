package com.example.raceconnect.viewmodel.MenuViewModel.FriendListViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
// ginamit ko muna ung viewmodel ng friends mismo for testing change if needed
class FriendListViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FriendListViewModel::class.java)) {
            return FriendListViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}