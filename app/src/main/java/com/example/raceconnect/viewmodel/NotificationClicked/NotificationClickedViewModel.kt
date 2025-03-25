package com.example.raceconnect.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.model.LikeRequest
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.PostComment
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

    fun fetchPost(postId: Int?, repostId: Int? = null) {
        if (postId == null || postId <= 0) {
            _error.value = "Invalid post ID: $postId"
            Log.e("NotificationClickedViewModel", "Invalid post ID: $postId")
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (repostId != null && repostId > 0) {
                    // Step 1: Fetch the repost details from the PostReposts endpoint
                    Log.d("NotificationClickedViewModel", "Fetching reposts for original postId: $postId")
                    val repostsResponse = apiService.getRepostsByPostId(postId)
                    if (repostsResponse.isSuccessful) {
                        val reposts = repostsResponse.body() ?: emptyList()
                        val repost = reposts.find { it.id == repostId }
                        if (repost != null) {
                            // Construct a NewsFeedDataClassItem for the repost
                            val repostItem = NewsFeedDataClassItem(
                                id = repost.id,
                                user_id = repost.userId,
                                username = null, // Fetch username separately if needed
                                title = null,
                                content = repost.quote ?: "",
                                imgUrl = null,
                                like_count = 0, // Fetch separately if needed
                                comment_count = 0, // Fetch separately if needed
                                repost_count = 0, // Fetch separately if needed
                                category = "Repost",
                                privacy = "Public",
                                type = "repost",
                                postType = "repost",
                                status = null,
                                created_at = repost.createdAt,
                                updated_at = repost.createdAt,
                                report = null,
                                archived_at = null,
                                profile_picture = null,
                                images = null,
                                isLiked = false,
                                isRepost = true,
                                original_post_id = postId,
                                quote = repost.quote
                            )
                            _repost.value = repostItem
                            Log.d("NotificationClickedViewModel", "Repost fetched: $repostItem")

                            // Step 2: Fetch the original post
                            Log.d("NotificationClickedViewModel", "Fetching original post with postId: $postId")
                            val originalPostResponse = apiService.getPostById(postId)
                            if (originalPostResponse.isSuccessful && originalPostResponse.body() != null) {
                                _originalPost.value = originalPostResponse.body()
                                Log.d("NotificationClickedViewModel", "Original post fetched: ${_originalPost.value}")
                            } else {
                                _error.value = "Original post unavailable: ${originalPostResponse.message()}"
                                Log.w("NotificationClickedViewModel", "Failed to fetch original post: ${originalPostResponse.code()}")
                            }
                        } else {
                            _error.value = "Repost with ID $repostId not found for post $postId"
                            Log.w("NotificationClickedViewModel", "Repost ID $repostId not found in reposts list")
                        }
                    } else {
                        _error.value = "Failed to fetch reposts: ${repostsResponse.message()}"
                        Log.w("NotificationClickedViewModel", "Failed to fetch reposts: ${repostsResponse.code()}")
                    }
                } else {
                    // No repost, just fetch the post
                    Log.d("NotificationClickedViewModel", "Fetching post with postId: $postId")
                    val response = apiService.getPostById(postId)
                    if (response.isSuccessful && response.body() != null) {
                        _repost.value = response.body() // Treat as the main post
                        _originalPost.value = null
                        Log.d("NotificationClickedViewModel", "Post fetched: ${_repost.value}")
                    } else {
                        _error.value = "Failed to fetch post: ${response.message()}"
                        Log.w("NotificationClickedViewModel", "Failed to fetch post: ${response.code()}")
                    }
                }

                // Update last fetched IDs
                lastFetchedPostId = postId
                lastFetchedRepostId = repostId
            } catch (e: Exception) {
                _error.value = "Error fetching post/repost: ${e.message}"
                Log.e("NotificationClickedViewModel", "Exception during fetch", e)
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

    fun toggleLike(postId: Int, ownerId: Int) {
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
                    val requestBody = LikeRequest(
                        user_id = userId,
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
        get() = 1 // Replace with actual user ID from userPreferences
}