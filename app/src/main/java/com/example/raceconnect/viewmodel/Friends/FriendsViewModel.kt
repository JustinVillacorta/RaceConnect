package com.example.raceconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.Friend
import com.example.raceconnect.model.FriendStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class FriendsViewModel(private val userPreferences: UserPreferences) : ViewModel() {

    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchFriends()
    }

    private fun fetchFriends() {
        viewModelScope.launch {
            _isLoading.value = true
            // Simulate fetching friends from a data source (e.g., API or database)
            val currentUserId = userPreferences.user.first()?.id ?: return@launch
            val friendList = listOf(
                Friend(1, "Blence Gilly", "https://example.com/blence_profile.jpg", FriendStatus.REQUEST),
                Friend(2, "Isagi Yoichi", "https://example.com/isagi_profile.jpg", FriendStatus.EXPLORE)
            )
            _friends.value = friendList
            _isLoading.value = false
        }
    }

    fun confirmFriendRequest(friendId: Int) {
        viewModelScope.launch {
            // Simulate confirming a friend request
            val updatedFriends = _friends.value.map { friend ->
                if (friend.id == friendId && friend.status == FriendStatus.REQUEST) {
                    friend.copy(status = FriendStatus.FRIEND)
                } else {
                    friend
                }
            }
            _friends.value = updatedFriends
            // Optionally, call backend API to update friend status
        }
    }

    fun cancelFriendRequest(friendId: Int) {
        viewModelScope.launch {
            // Simulate canceling a friend request
            val updatedFriends = _friends.value.filterNot { it.id == friendId && it.status == FriendStatus.REQUEST }
            _friends.value = updatedFriends
            // Optionally, call backend API to remove friend request
        }
    }

    fun addFriend(friendId: Int) {
        viewModelScope.launch {
            // Simulate adding a friend from explore section
            val updatedFriends = _friends.value.map { friend ->
                if (friend.id == friendId && friend.status == FriendStatus.EXPLORE) {
                    friend.copy(status = FriendStatus.FRIEND)
                } else {
                    friend
                }
            }
            _friends.value = updatedFriends
            // Optionally, call backend API to add friend
        }
    }

    fun removeFriend(friendId: Int) {
        viewModelScope.launch {
            // Simulate removing a friend or explore suggestion
            val updatedFriends = _friends.value.filterNot { it.id == friendId }
            _friends.value = updatedFriends
            // Optionally, call backend API to remove friend
        }
    }
}