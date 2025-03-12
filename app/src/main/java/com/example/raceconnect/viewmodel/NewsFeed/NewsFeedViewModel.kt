package com.example.raceconnect.viewmodel.NewsFeed

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.CreateRepostRequest
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.ReportRequest
import com.example.raceconnect.network.NewsFeedPagingSourceAllPosts
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class NewsFeedViewModel(private val userPreferences: UserPreferences) : ViewModel() {
    private val _postLikes = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    private val _likeCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val postLikes: StateFlow<Map<Int, Boolean>> = _postLikes.asStateFlow()
    val likeCounts: StateFlow<Map<Int, Int>> = _likeCounts.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _newPostTrigger = MutableStateFlow(false)
    val newPostTrigger: StateFlow<Boolean> = _newPostTrigger.asStateFlow()

    private val _postImages = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val postImages: StateFlow<Map<Int, List<String>>> = _postImages.asStateFlow()

    private val apiService = RetrofitInstance.api
    var isInitialRefreshDone = false

    // Get the current user's ID once during initialization
    private val currentUserId: Int by lazy {
        runBlocking { userPreferences.user.first()?.id ?: -1 }
    }

    // Main news feed flow for the current user
    val postsFlow: Flow<PagingData<NewsFeedDataClassItem>> = Pager(
        config = PagingConfig(pageSize = 10, prefetchDistance = 2, enablePlaceholders = false),
        pagingSourceFactory = { NewsFeedPagingSourceAllPosts(apiService, currentUserId) }
    ).flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            postsFlow.collect { pagingData ->
                Log.d("NewsFeedViewModel", "Collected new PagingData with items")
            }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            Log.d("NewsFeedViewModel", "✅ Refresh triggered")
            // Invalidate the PagingSource to trigger a refresh
            Pager(
                config = PagingConfig(pageSize = 10, prefetchDistance = 2, enablePlaceholders = false),
                pagingSourceFactory = { NewsFeedPagingSourceAllPosts(apiService, currentUserId) }
            ).flow.cachedIn(viewModelScope) // This recreates the Pager, effectively refreshing
            _isRefreshing.value = false
        }
    }

    fun resetNewPostTrigger() {
        _newPostTrigger.value = false
    }

    fun addPost(context: Context, content: String, imageUri: Uri?, category: String, privacy: String) {
        viewModelScope.launch {
            val userId = userPreferences.user.first()?.id ?: return@launch
            try {
                val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId.toString())
                val contentPart = RequestBody.create("text/plain".toMediaTypeOrNull(), content)
                val titlePart = RequestBody.create("text/plain".toMediaTypeOrNull(), "You")
                val categoryPart = RequestBody.create("text/plain".toMediaTypeOrNull(), category)
                val privacyPart = RequestBody.create("text/plain".toMediaTypeOrNull(), privacy)
                val typePart = RequestBody.create("text/plain".toMediaTypeOrNull(), if (imageUri != null) "image" else "text")
                val postTypePart = RequestBody.create("text/plain".toMediaTypeOrNull(), "normal")

                val imagePart = imageUri?.let { uri ->
                    val tempFile = getFileFromUri(context, uri)
                    tempFile?.let {
                        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                        MultipartBody.Part.createFormData("image", it.name, requestFile)
                    }
                }

                val response = apiService.createPostWithImage(
                    userIdPart, contentPart, titlePart, categoryPart, privacyPart, typePart, postTypePart, imagePart
                )

                if (response.isSuccessful) {
                    _newPostTrigger.value = true
                    refreshPosts()
                } else {
                    Log.e("NewsFeedViewModel", "❌ Failed to create post: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error adding post", e)
            }
        }
    }

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}.jpg")
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
            file
        } catch (e: Exception) {
            Log.e("FileUtil", "❌ Failed to get file from URI", e)
            null
        }
    }

    fun getPostImages(postId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.GetPostImg(postId)
                if (response.isSuccessful) {
                    val postResponses = response.body() ?: emptyList()
                    val imageUrls = postResponses.map { it.image_url }
                    _postImages.value = _postImages.value.toMutableMap().apply {
                        this[postId] = imageUrls
                    }
                } else {
                    _postImages.value = _postImages.value.toMutableMap().apply {
                        this[postId] = emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "Error fetching post images", e)
            }
        }
    }

    fun fetchPostLikes(postId: Int) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.user.first()?.id ?: return@launch
                val response = apiService.getPostLikes(postId)
                if (response.isSuccessful) {
                    val likes = response.body() ?: emptyList()
                    val isLiked = likes.any { it.userId == userId }
                    val likeCount = likes.size
                    _postLikes.value = _postLikes.value + (postId to isLiked)
                    _likeCounts.value = _likeCounts.value + (postId to likeCount)
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error fetching likes", e)
            }
        }
    }

    fun toggleLike(postId: Int, ownerId: Int) {
        viewModelScope.launch {
            val userId = userPreferences.user.first()?.id ?: return@launch
            _postLikes.value = _postLikes.value + (postId to true)
            _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) + 1)
            try {
                val requestBody = mapOf(
                    "user_id" to userId,
                    "post_id" to postId,
                    "owner_id" to ownerId
                )
                val response = apiService.likePost(requestBody)
                if (!response.isSuccessful) {
                    _postLikes.value = _postLikes.value + (postId to false)
                    _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) - 1)
                }
            } catch (e: Exception) {
                _postLikes.value = _postLikes.value + (postId to false)
                _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) - 1)
            }
        }
    }

    fun unlikePost(postId: Int) {
        viewModelScope.launch {
            _postLikes.value = _postLikes.value + (postId to false)
            _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) - 1)
            try {
                val response = apiService.unlikePost(postId)
                if (!response.isSuccessful) {
                    _postLikes.value = _postLikes.value + (postId to true)
                    _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) + 1)
                }
            } catch (e: Exception) {
                _postLikes.value = _postLikes.value + (postId to true)
                _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) + 1)
            }
        }
    }

    fun reportPost(
        postId: Int,
        reason: String,
        otherText: String?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (currentUserId == -1) {
                    onFailure("User not logged in")
                    return@launch
                }
                val finalReason = if (reason == "Others" && otherText != null) otherText else reason
                val reportRequest = ReportRequest(
                    post_id = postId,
                    marketplace_item_id = null,
                    reporter_id = currentUserId,
                    reason = finalReason
                )
                val response = apiService.createReport(reportRequest)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("Failed to report post: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                onFailure("Error reporting post: ${e.message}")
            }
        }
    }

    fun reportUser(userId: Int, reason: String, otherText: String?) {
        // Logic to report the user (same as reportPost for now)
        Log.d("NewsFeedViewModel", "Reported user $userId with reason: $reason, otherText: $otherText")
    }

    fun repostPost(postId: Int, comment: String) {
        viewModelScope.launch {
            val userId = userPreferences.user.first()?.id ?: return@launch
            try {
                val request = CreateRepostRequest(
                    userId = userId,
                    postId = postId,
                    quote = comment.takeIf { it.isNotBlank() }
                )
                val response = apiService.createRepost(request)
                if (response.isSuccessful) {
                    _newPostTrigger.value = true
                    refreshPosts()
                    Log.d("NewsFeedViewModel", "✅ Successfully reposted post $postId")
                } else {
                    Log.e("NewsFeedViewModel", "❌ Failed to repost: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error reposting post", e)
            }
        }
    }

    fun getPostsByUserId(userId: Int): StateFlow<PagingData<NewsFeedDataClassItem>> {
        val userPostsFlow = MutableStateFlow<PagingData<NewsFeedDataClassItem>>(PagingData.empty())
        viewModelScope.launch {
            try {
                val pager = Pager(
                    config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                    pagingSourceFactory = { NewsFeedPagingSourceAllPosts(apiService, userId) }
                )
                pager.flow.cachedIn(viewModelScope).collect { pagingData ->
                    userPostsFlow.value = pagingData
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "Error fetching posts for user $userId", e)
            }
        }
        return userPostsFlow.asStateFlow()
    }
}