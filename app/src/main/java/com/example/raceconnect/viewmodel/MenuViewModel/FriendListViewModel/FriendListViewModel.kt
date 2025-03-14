package com.example.raceconnect.viewmodel.MenuViewModel.FriendListViewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
// ginamit ko muna ung viewmodel ng friends mismo for testing change if needed
class FriendListViewModel : ViewModel() {
    // State for the list of friends
    private val _friends = mutableStateListOf<String>()
    val friends: List<String> get() = _friends

    // State for the currently selected tab index
    private var _selectedTabIndex = 0
    val selectedTabIndex: Int get() = _selectedTabIndex

    init {
        // Initialize with some dummy data (replace with real data source later)
        _friends.addAll(listOf("Jennifer Lawrence", "Dana Wheat", "Takeru Sanada"))
    }

    // Function to switch tabs
    fun switchTab(index: Int) {
        _selectedTabIndex = index
    }

    // Function to add a friend (placeholder for future implementation)
    fun addFriend(friendName: String) {
        _friends.add(friendName)
    }

    // Function to remove a friend (placeholder for future implementation)
    fun removeFriend(friendName: String) {
        _friends.remove(friendName)
    }
}