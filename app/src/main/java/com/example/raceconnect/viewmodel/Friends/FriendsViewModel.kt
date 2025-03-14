package com.example.raceconnect.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.Friend
import com.example.raceconnect.model.FriendRequest
import com.example.raceconnect.model.UpdateFriendStatus
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

    private val TAG = "FriendsViewModel"

    init {
        fetchFriends()
    }

    fun fetchFriends() {
        Log.d(TAG, "fetchFriends: Starting to fetch friends list")
        viewModelScope.launch {
            _isLoading.value = true
            Log.i(TAG, "fetchFriends: Set isLoading to true")
            try {
                val userId = userPreferences.user.first()?.id?.toString() ?: run {
                    Log.e(TAG, "fetchFriends: userId is null")
                    return@launch
                }
                Log.d(TAG, "fetchFriends: Logged-in userId: $userId (type: ${userId::class.simpleName})")
                val response = RetrofitInstance.api.getFriendsList(userId = userId)
                Log.d(TAG, "fetchFriends: API call executed, response code: ${response.code()}")
                if (response.isSuccessful) {
                    response.body()?.let { rawFriends ->
                        Log.i(TAG, "fetchFriends: Raw friends response: $rawFriends")
                        val friendsList = rawFriends.mapNotNull { friend ->
                            val id = friend["friend_id"]?.toString()?.replace(".0", "") ?: run {
                                Log.e(TAG, "fetchFriends: friend_id is null for friend: $friend")
                                return@mapNotNull null
                            }
                            val name = friend["username"]?.toString() ?: run {
                                Log.e(TAG, "fetchFriends: username is null for friend: $friend")
                                return@mapNotNull null
                            }
                            val status = friend["status"]?.toString() ?: run {
                                Log.e(TAG, "fetchFriends: status is null for friend: $friend")
                                return@mapNotNull null
                            }
                            val profileImageUrl = friend["profile_picture"]?.toString()
                            val receiverId = when (status) {
                                "Pending" -> {
                                    Log.d(TAG, "fetchFriends: Setting receiverId to $userId for status=$status, id=$id")
                                    userId
                                }
                                "PendingSent" -> {
                                    Log.d(TAG, "fetchFriends: Setting receiverId to $id for status=$status, id=$id")
                                    id
                                }
                                else -> {
                                    Log.d(TAG, "fetchFriends: Setting receiverId to null for status=$status, id=$id")
                                    null
                                }
                            }
                            Friend(id, name, status, profileImageUrl, receiverId)
                        }.distinctBy { it.id }.sortedBy {
                            when (it.status) {
                                "Pending" -> 1
                                "PendingSent" -> 2
                                else -> 3
                            }
                        }
                        _friends.value = friendsList
                        Log.i(TAG, "fetchFriends: Updated friends list with ${friendsList.size} items: $friendsList")
                    } ?: run {
                        Log.e(TAG, "fetchFriends: Response body is null")
                        _friends.value = emptyList()
                    }
                } else {
                    Log.e(TAG, "fetchFriends: Failed with code ${response.code()} - ${response.message()}")
                }
            } catch (e: IOException) {
                Log.e(TAG, "fetchFriends: Network error: ${e.message}", e)
            } catch (e: HttpException) {
                Log.e(TAG, "fetchFriends: HTTP error: ${e.code()} - ${e.message}", e)
            } finally {
                _isLoading.value = false
                Log.i(TAG, "fetchFriends: Set isLoading to false")
            }
            Log.d(TAG, "fetchFriends: Completed")
        }
    }

    fun confirmFriendRequest(friendId: String) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.user.first()?.id?.toString() ?: return@launch
                val response = RetrofitInstance.api.updateFriendStatus(
                    UpdateFriendStatus(userId, friendId, "Accepted")
                )
                if (response.isSuccessful) {
                    fetchFriends()
                }
            } catch (e: Exception) {
                Log.e(TAG, "confirmFriendRequest: Error: ${e.message}", e)
            }
        }
    }

    fun addFriend(friendId: String) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.user.first()?.id?.toString() ?: return@launch
                val response = RetrofitInstance.api.sendFriendRequest(
                    FriendRequest(userId, friendId)
                )
                if (response.isSuccessful) {
                    fetchFriends()
                }
            } catch (e: Exception) {
                Log.e(TAG, "addFriend: Error: ${e.message}", e)
            }
        }
    }

    fun cancelFriendRequest(friendId: String) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.user.first()?.id?.toString() ?: return@launch
                val response = RetrofitInstance.api.removeFriend(userId, friendId)
                if (response.isSuccessful) {
                    fetchFriends()
                }
            } catch (e: Exception) {
                Log.e(TAG, "cancelFriendRequest: Error: ${e.message}", e)
            }
        }
    }

    fun removeFriend(friendId: String) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.user.first()?.id?.toString() ?: return@launch
                val response = RetrofitInstance.api.removeFriend(userId, friendId)
                if (response.isSuccessful) {
                    fetchFriends()
                }
            } catch (e: Exception) {
                Log.e(TAG, "removeFriend: Error: ${e.message}", e)
            }
        }
    }
}