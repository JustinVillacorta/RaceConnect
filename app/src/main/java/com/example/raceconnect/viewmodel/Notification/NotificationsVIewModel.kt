package com.example.raceconnect.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.model.*
import com.example.raceconnect.network.ApiService
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val apiService: ApiService = RetrofitInstance.api) : ViewModel() {
    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedPost = MutableStateFlow<NewsFeedDataClassItem?>(null)
    val selectedPost: StateFlow<NewsFeedDataClassItem?> = _selectedPost.asStateFlow()

    fun fetchNotifications(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getAllNotifications(userId)
                if (response.isSuccessful) {
                    _notifications.value = response.body() ?: emptyList()
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch notifications: ${response.code()} - ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                android.util.Log.e("NotificationViewModel", "Exception in fetchNotifications", e)
                _error.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getNotificationById(id: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.getNotificationById(id)
                if (response.isSuccessful) {
                    response.body()?.let { notification ->
                        _notifications.value = _notifications.value.map {
                            if (it.id == id) notification else it
                        }
                    }
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch notification: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun createNotification(request: NotificationRequest) {
        viewModelScope.launch {
            try {
                val response = apiService.createNotification(request)
                if (response.isSuccessful) {
                    fetchNotifications(request.userId)
                    _error.value = null
                } else {
                    _error.value = "Failed to create notification: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.markAsRead(notificationId)
                if (response.isSuccessful) {
                    _notifications.value = _notifications.value.map {
                        if (it.id == notificationId) it.copy(isReadInt = 1) else it
                    }
                    _error.value = null
                } else {
                    _error.value = "Failed to mark as read: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteNotification(id: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteNotification(id)
                if (response.isSuccessful) {
                    _notifications.value = _notifications.value.filter { it.id != id }
                    _error.value = null
                } else {
                    _error.value = "Failed to delete notification: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
            }
        }
    }

    fun fetchPost(postId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.getPostById(postId)
                if (response.isSuccessful) {
                    _selectedPost.value = response.body()
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch post: ${response.code()} - ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Error fetching post: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}