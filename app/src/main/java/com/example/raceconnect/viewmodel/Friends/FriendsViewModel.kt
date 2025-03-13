package com.example.raceconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.Friend
import com.example.raceconnect.model.FriendRequest
import com.example.raceconnect.model.UpdateFriendStatus
import com.example.raceconnect.model.RemoveFriendRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class FriendsViewModel(private val userPreferences: UserPreferences) : ViewModel() {
    private val _friends = MutableStateFlow<List<Friend>>(emptyList())
    val friends: StateFlow<List<Friend>> = _friends.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchFriends()
    }

    fun fetchFriends() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = userPreferences.user.first()?.id?.toString() ?: run {
                    println("Error: userId is null")
                    return@launch
                }
                println("Logged-in userId: $userId (type: ${if (userId != null) userId::class.simpleName else "null"})")
                val response = RetrofitInstance.api.getFriendsList(userId = userId)
                if (response.isSuccessful) {
                    response.body()?.let { rawFriends ->
                        println("Raw friends response: $rawFriends")
                        val friendsList = rawFriends.mapNotNull { friend ->
                            val id = friend["friend_id"]?.toString()?.replace(".0", "") ?: run {
                                println("Error: friend_id is null for friend: $friend")
                                return@mapNotNull null
                            }
                            val name = friend["username"]?.toString() ?: run {
                                println("Error: username is null for friend: $friend")
                                return@mapNotNull null
                            }
                            val status = friend["status"]?.toString() ?: run {
                                println("Error: status is null for friend: $friend")
                                return@mapNotNull null
                            }
                            val profileImageUrl = friend["profile_picture"]?.toString()
                            val receiverId = when (status) {
                                "Pending" -> {
                                    println("Setting receiverId to $userId (type: ${userId::class.simpleName}) for status=$status, id=$id")
                                    userId
                                }
                                "PendingSent" -> {
                                    println("Setting receiverId to $id (type: ${id::class.simpleName}) for status=$status, id=$id")
                                    id
                                }
                                else -> {
                                    println("Setting receiverId to null for status=$status, id=$id")
                                    null
                                }
                            }
                            Friend(id, name, status, profileImageUrl, receiverId)
                        }.distinctBy { it.id }.sortedBy {
                            // Prioritize Pending over PendingSent over NonFriends
                            when (it.status) {
                                "Pending" -> 1
                                "PendingSent" -> 2
                                else -> 3
                            }
                        }.takeWhile { friend ->
                            // Keep only the first entry for each id
                            true
                        }
                        _friends.value = friendsList
                        println("Final friends list: ${_friends.value}")
                    } ?: run {
                        println("Error: Response body is null")
                        _friends.value = emptyList()
                    }
                } else {
                    println("Error: ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                println("Network error: ${e.message}")
            } catch (e: HttpException) {
                println("HTTP error: ${e.code()} - ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun confirmFriendRequest(friendId: String) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.user.first()?.id ?: return@launch
                val response = RetrofitInstance.api.updateFriendStatus(
                    UpdateFriendStatus(userId.toString(), friendId, "Accepted")
                )
                if (response.isSuccessful) {
                    fetchFriends()
                }
            } catch (e: Exception) {
                println("Confirm error: ${e.message}")
            }
        }
    }

    fun addFriend(friendId: String) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.user.first()?.id ?: return@launch
                val response = RetrofitInstance.api.sendFriendRequest(
                    FriendRequest(userId.toString(), friendId)
                )
                if (response.isSuccessful) {
                    fetchFriends()
                } else {
                    println("Add friend failed: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                println("Add error: ${e.message}")
            }
        }
    }

    fun cancelFriendRequest(friendId: String) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.user.first()?.id ?: return@launch
                val response = RetrofitInstance.api.removeFriend(
                    userId = userId.toString(), // Pass as query parameter
                    friendId = friendId
                )
                if (response.isSuccessful) {
                    fetchFriends()
                }
            } catch (e: Exception) {
                println("Cancel error: ${e.message}")
            }
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.user.first()?.id ?: return@launch
                val response = RetrofitInstance.api.removeFriend(
                    userId = userId.toString(), // Pass as query parameter
                    friendId = friendId
                )
                if (response.isSuccessful) {
                    fetchFriends()
                }
            } catch (e: Exception) {
                println("Remove error: ${e.message}")
            }
        }
    }
}