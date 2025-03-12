package com.example.raceconnect.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.PostComment
import com.example.raceconnect.model.PostLike
import com.example.raceconnect.network.ApiService
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationClickedViewModel(private val apiService: ApiService = RetrofitInstance.api) : ViewModel() {

    private val _repost = MutableStateFlow<NewsFeedDataClassItem?>(null)
    val repost: StateFlow<NewsFeedDataClassItem?> = _repost.asStateFlow()

    private val _originalPost = MutableStateFlow<NewsFeedDataClassItem?>(null)
    val originalPost: StateFlow<NewsFeedDataClassItem?> = _originalPost.asStateFlow()

    private val _comments = MutableStateFlow<List<PostComment>>(emptyList())
    val comments: StateFlow<List<PostComment>> = _comments.asStateFlow()

    private val _isLiked = MutableStateFlow<Boolean>(false)
    val isLiked: StateFlow<Boolean> = _isLiked.asStateFlow()

    private val _likeCount = MutableStateFlow<Int>(0)
    val likeCount: StateFlow<Int> = _likeCount.asStateFlow()

    private val _isRepostLiked = MutableStateFlow<Boolean>(false)
    val isRepostLiked: StateFlow<Boolean> = _isRepostLiked.asStateFlow()

    private val _repostLikeCount = MutableStateFlow<Int>(0)
    val repostLikeCount: StateFlow<Int> = _repostLikeCount.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    internal val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _post = MutableStateFlow<NewsFeedDataClassItem?>(null)
    val post: StateFlow<NewsFeedDataClassItem?> = _post

    private var authToken: String? = null
    private var lastFetchedPostId: Int? = null
    private var lastFetchedRepostId: Int? = null

    fun setAuthToken(token: String) {
        authToken = "Bearer $token"
    }

    fun fetchPost(postId: Int?, repostId: Int? = null) {
        Log.d("NotificationClickedViewModel", "fetchPost called with postId: $postId, repostId: $repostId")
        if (postId == null || postId <= 0) {
            _error.value = "Invalid post ID: $postId"
            Log.e("NotificationClickedViewModel", "Invalid post ID: $postId")
            return
        }

        // Prevent duplicate fetches for the same postId and repostId
        if (postId == lastFetchedPostId && repostId == lastFetchedRepostId && _post.value != null) {
            Log.d("NotificationClickedViewModel", "Skipping duplicate fetch for postId: $postId, repostId: $repostId")
            return
        }

        lastFetchedPostId = postId
        lastFetchedRepostId = repostId

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _post.value = null
            _repost.value = null
            _originalPost.value = null
            Log.d("NotificationClickedViewModel", "Starting fetch for postId: $postId")

            try {
                if (repostId != null && repostId > 0) {
                    Log.d("NotificationClickedViewModel", "Fetching repost with repostId: $repostId")
                    val repostResponse = apiService.getPostById(repostId)
                    Log.d("NotificationClickedViewModel", "Repost API response code: ${repostResponse.code()}, body: ${repostResponse.body()}")
                    if (repostResponse.isSuccessful && repostResponse.body() != null) {
                        _repost.value = repostResponse.body()
                        Log.d("NotificationClickedViewModel", "Repost fetched: ${_repost.value}")
                        Log.d("NotificationClickedViewModel", "Fetching original post with postId: $postId")
                        val originalPostResponse = apiService.getPostById(postId)
                        Log.d("NotificationClickedViewModel", "Original post API response code: ${originalPostResponse.code()}, body: ${originalPostResponse.body()}")
                        if (originalPostResponse.isSuccessful && originalPostResponse.body() != null) {
                            _originalPost.value = originalPostResponse.body()
                            Log.d("NotificationClickedViewModel", "Original post fetched: ${_originalPost.value}")
                        } else {
                            _error.value = "Failed to fetch original post: ${originalPostResponse.message()}"
                            Log.e("NotificationClickedViewModel", "Original post fetch failed: ${originalPostResponse.message()}")
                        }
                    } else {
                        _error.value = "Failed to fetch repost: ${repostResponse.message()}"
                        Log.e("NotificationClickedViewModel", "Repost fetch failed: ${repostResponse.message()}")
                    }
                } else {
                    Log.d("NotificationClickedViewModel", "Fetching regular post with postId: $postId")
                    val response = apiService.getPostById(postId)
                    Log.d("NotificationClickedViewModel", "Post API response code: ${response.code()}, body: ${response.body()}")
                    if (response.isSuccessful && response.body() != null) {
                        _post.value = response.body()
                        _repost.value = response.body() // Sync repost for UI consistency
                        Log.d("NotificationClickedViewModel", "Post fetched: ${_post.value}")
                    } else {
                        _error.value = "Failed to fetch post: ${response.message()}"
                        Log.e("NotificationClickedViewModel", "Post fetch failed: ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error: ${e.message}"
                Log.e("NotificationClickedViewModel", "Exception during fetch: ${e.message}", e)
            } finally {
                _isLoading.value = false
                Log.d("NotificationClickedViewModel", "Fetch completed. isLoading: ${_isLoading.value}, error: ${_error.value}")
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
                    _isLiked.value = likes.any { it.userId == userId }
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

    fun fetchRepostLikes(repostId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.getPostLikes(repostId)
                if (response.isSuccessful) {
                    val likes = response.body() ?: emptyList()
                    _isRepostLiked.value = likes.any { it.userId == userId }
                    _repostLikeCount.value = likes.size
                    _error.value = null
                } else {
                    _error.value = "Failed to fetch repost likes: ${response.code()} - ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Error fetching repost likes: ${e.message}"
                Log.e("NotificationClickedViewModel", "Exception in fetchRepostLikes", e)
            }
        }
    }

    fun toggleLike(postId: Int) {
        viewModelScope.launch {
            try {
                val token = authToken ?: run {
                    _error.value = "Authentication token is missing"
                    return@launch
                }
                if (_isLiked.value) {
                    val like = apiService.getPostLikes(postId).body()?.find { it.userId == userId }
                    like?.id?.let { likeId ->
                        val response = apiService.unlikePost(likeId)
                        if (response.isSuccessful) {
                            _isLiked.value = false
                            _likeCount.value = (_likeCount.value - 1).coerceAtLeast(0)
                            _error.value = null
                        } else {
                            _error.value = "Failed to unlike post: ${response.code()} - ${response.errorBody()?.string()}"
                        }
                    }
                } else {
                    val requestBody = mapOf("post_id" to postId, "user_id" to userId)
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

    fun toggleRepostLike(repostId: Int) {
        viewModelScope.launch {
            try {
                val token = authToken ?: run {
                    _error.value = "Authentication token is missing"
                    return@launch
                }
                if (_isRepostLiked.value) {
                    val like = apiService.getPostLikes(repostId).body()?.find { it.userId == userId }
                    like?.id?.let { likeId ->
                        val response = apiService.unlikePost(likeId)
                        if (response.isSuccessful) {
                            _isRepostLiked.value = false
                            _repostLikeCount.value = (_repostLikeCount.value - 1).coerceAtLeast(0)
                            _error.value = null
                        } else {
                            _error.value = "Failed to unlike repost: ${response.code()} - ${response.errorBody()?.string()}"
                        }
                    }
                } else {
                    val requestBody = mapOf("post_id" to repostId, "user_id" to userId)
                    val response = apiService.likePost(requestBody)
                    if (response.isSuccessful) {
                        _isRepostLiked.value = true
                        _repostLikeCount.value = _repostLikeCount.value + 1
                        _error.value = null
                    } else {
                        _error.value = "Failed to like repost: ${response.code()} - ${response.errorBody()?.string()}"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error toggling repost like: ${e.message}"
                Log.e("NotificationClickedViewModel", "Exception in toggleRepostLike", e)
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
                val comment = PostComment(postId = postId, comment = content, userId = userId)
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
        _post.value = null
        _comments.value = emptyList()
        _isLiked.value = false
        _likeCount.value = 0
        _isRepostLiked.value = false
        _repostLikeCount.value = 0
        _error.value = null
        lastFetchedPostId = null
        lastFetchedRepostId = null
    }

    private val userId: Int
        get() = 1 // Replace with userPreferences.user.collectAsState().value?.id
}