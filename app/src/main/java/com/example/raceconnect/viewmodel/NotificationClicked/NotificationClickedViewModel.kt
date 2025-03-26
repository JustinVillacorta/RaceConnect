package com.example.raceconnect.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.LikeRequest
import com.example.raceconnect.model.PostByIdResponse
import com.example.raceconnect.model.PostComment
import com.example.raceconnect.model.Repost
import com.example.raceconnect.network.ApiService
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationClickedViewModel(
    private val apiService: ApiService = RetrofitInstance.api,
    private val userPreferences: UserPreferences? = null // Optional dependency injection
) : ViewModel() {

    private val _repost = MutableStateFlow<PostByIdResponse?>(null)
    val repost: StateFlow<PostByIdResponse?> = _repost.asStateFlow()

    private val _originalPost = MutableStateFlow<PostByIdResponse?>(null)
    val originalPost: StateFlow<PostByIdResponse?> = _originalPost.asStateFlow()

    private val _repostData = MutableStateFlow<Repost?>(null) // New StateFlow for Repost data
    val repostData: StateFlow<Repost?> = _repostData.asStateFlow()

    private val _comments = MutableStateFlow<List<PostComment>>(emptyList())
    val comments: StateFlow<List<PostComment>> = _comments.asStateFlow()

    private val _isLiked = MutableStateFlow(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _likeCount = MutableStateFlow(0)
    val likeCount: StateFlow<Int> = _likeCount.asStateFlow()

    private val _isRepostLiked = MutableStateFlow(false)
    val isRepostLiked: StateFlow<Boolean> = _isRepostLiked.asStateFlow()

    private val _repostLikeCount = MutableStateFlow(0)
    val repostLikeCount: StateFlow<Int> = _repostLikeCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    internal val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var authToken: String? = null
    private var lastFetchedPostId: Int? = null
    private var lastFetchedRepostId: Int? = null

    fun setAuthToken(token: String) {
        authToken = "Bearer $token"
    }

    fun fetchPost(postId: Int, repostId: Int?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                if (repostId != null) {
                    // Fetch repost details first
                    val repostResponse = apiService.getRepostByRepostId(repostId)
                    if (repostResponse.isSuccessful) {
                        val repostData = repostResponse.body()
                        if (repostData != null) {
                            _repostData.value = repostData // Store the Repost data

                            // Fetch the repost post details
                            val repostPostResponse = apiService.getPostDetailById(repostData.id)
                            if (repostPostResponse.isSuccessful) {
                                _repost.value = repostPostResponse.body()
                            }

                            // Fetch the original post using the postId from repost
                            val originalPostResponse = apiService.getPostDetailById(repostData.postId)
                            if (originalPostResponse.isSuccessful) {
                                _originalPost.value = originalPostResponse.body()
                            } else {
                                _error.value = "Failed to fetch original post: ${originalPostResponse.errorBody()?.string()}"
                            }
                        } else {
                            _error.value = "Repost data is null"
                        }
                    } else {
                        _error.value = "Failed to fetch repost: ${repostResponse.errorBody()?.string()}"
                    }
                } else {
                    // Fetch regular post
                    val response = apiService.getPostDetailById(postId)
                    if (response.isSuccessful) {
                        _repost.value = response.body()
                        _originalPost.value = null
                        _repostData.value = null // Clear repost data for non-reposts
                    } else {
                        _error.value = "Failed to fetch post: ${response.errorBody()?.string()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error fetching post: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchComments(postId: Int) {
        viewModelScope.launch {
            try {
                val token = authToken ?: run {
                    _error.value = "Authentication token is missing"
                    return@launch
                }
                val response = apiService.getCommentsByPostId(token, postId)
                if (response.isSuccessful) {
                    _comments.value = response.body() ?: emptyList()
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch comments: ${response.code()} - ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Error fetching comments: ${e.message}"
                Log.e("NotificationClickedViewModel", "Exception in fetchComments", e)
            }
        }
    }

    fun fetchPostLikes(postId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.getPostLikes(postId)
                if (response.isSuccessful) {
                    val likes = response.body() ?: emptyList()
                    val currentUserId = getUserId() ?: 1 // Fallback to 1 if userId not available
                    _isLiked.value = likes.any { it.userId == currentUserId }
                    _likeCount.value = likes.size
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch likes: ${response.code()} - ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Error fetching likes: ${e.message}"
                Log.e("NotificationClickedViewModel", "Exception in fetchPostLikes", e)
            }
        }
    }

    fun toggleLike(postId: Int, ownerId: Int) {
        viewModelScope.launch {
            try {
                val token = authToken ?: run {
                    _error.value = "Authentication token is missing"
                    return@launch
                }
                val currentUserId = getUserId() ?: run {
                    _error.value = "User ID is missing"
                    return@launch
                }

                if (_isLiked.value) {
                    val like = apiService.getPostLikes(postId).body()?.find { it.userId == currentUserId }
                    like?.id?.let { likeId ->
                        val response = apiService.unlikePost(likeId)
                        if (response.isSuccessful) {
                            _isLiked.value = false
                            _likeCount.value = (_likeCount.value - 1).coerceAtLeast(0)
                            _error.value = null
                        } else {
                            _error.value = "Failed to unlike post: ${response.code()} - ${response.errorBody()?.string()}"
                        }
                    } ?: run {
                        _error.value = "Like ID not found for user"
                    }
                } else {
                    val requestBody = LikeRequest(
                        user_id = currentUserId,
                        post_id = postId,
                        owner_id = ownerId
                    )
                    val response = apiService.likePost(requestBody)
                    if (response.isSuccessful) {
                        _isLiked.value = true
                        _likeCount.value = _likeCount.value + 1
                        _error.value = null
                    } else {
                        _error.value = "Failed to like post: ${response.code()} - ${response.errorBody()?.string()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error toggling like: ${e.message}"
                Log.e("NotificationClickedViewModel", "Exception in toggleLike", e)
            }
        }
    }

    fun addComment(postId: Int, content: String) {
        viewModelScope.launch {
            try {
                val token = authToken ?: run {
                    _error.value = "Authentication token is missing"
                    return@launch
                }
                val currentUserId = getUserId() ?: run {
                    _error.value = "User ID is missing"
                    return@launch
                }
                val comment = PostComment(postId = postId, comment = content, userId = currentUserId)
                val response = apiService.addComment(token, comment)
                if (response.isSuccessful) {
                    fetchComments(postId) // Refresh comments
                    _error.value = null
                } else {
                    _error.value = "Failed to add comment: ${response.code()} - ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Error adding comment: ${e.message}"
                Log.e("NotificationClickedViewModel", "Exception in addComment", e)
            }
        }
    }

    fun clearPost() {
        _repost.value = null
        _originalPost.value = null
        _repostData.value = null // Clear repost data
        _comments.value = emptyList()
        _isLiked.value = false
        _likeCount.value = 0
        _isRepostLiked.value = false
        _repostLikeCount.value = 0
        _error.value = null
        lastFetchedPostId = null
        lastFetchedRepostId = null
    }

    private suspend fun getUserId(): Int? {
        return userPreferences?.getUserId() ?: run {
            Log.w("NotificationClickedViewModel", "UserPreferences not provided, using default userId")
            1 // Fallback to 1 if UserPreferences is null
        }
    }
}