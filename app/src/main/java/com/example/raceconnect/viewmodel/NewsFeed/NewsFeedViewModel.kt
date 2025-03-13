package com.example.raceconnect.viewmodel.NewsFeed

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.CreateRepostRequest
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.ReportRequest
import com.example.raceconnect.network.NewsFeedPagingSourceAllPosts
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference.NewsFeedPreferenceViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class NewsFeedViewModel(
    private val userPreferences: UserPreferences,
    private val preferenceViewModel: NewsFeedPreferenceViewModel,
    private val context: Context
) : ViewModel() {
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

    private val apiService = RetrofitInstance.api ?: throw IllegalStateException("RetrofitInstance.api must not be null")
    var isInitialRefreshDone = false

    private val _currentUserId = MutableStateFlow<Int?>(-1)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    private val _selectedCategories = MutableStateFlow<List<String>>(listOf("F1"))
    val selectedCategories: StateFlow<List<String>> = _selectedCategories.asStateFlow()

    private val _isDataReady = MutableStateFlow(false)
    val isDataReady: StateFlow<Boolean> = _isDataReady.asStateFlow()

    private val _postsFlow = MutableStateFlow<PagingData<NewsFeedDataClassItem>>(PagingData.empty())
    val postsFlow: StateFlow<PagingData<NewsFeedDataClassItem>> = _postsFlow.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val user = userPreferences.user.first()
                _currentUserId.value = user?.id ?: -1
                Log.d("NewsFeedViewModel", "Fetched user ID: ${_currentUserId.value}")

                val categories = userPreferences.selectedCategories.first().map { brandName ->
                    when (brandName) {
                        "Formula 1" -> "F1"
                        "24H le mans" -> "LEM"
                        "Formula drift" -> "FD"
                        "WRC" -> "WRC"
                        "NASCAR" -> "NAS"
                        "GT CUP" -> "GT"
                        else -> "F1"
                    }
                }.ifEmpty { listOf("F1") }
                _selectedCategories.value = categories
                Log.d("NewsFeedViewModel", "Fetched categories: $categories")

                _isDataReady.value = true
                Log.d("NewsFeedViewModel", "Data is ready")
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "Error fetching initial data", e)
                _isDataReady.value = true
            }
        }

        viewModelScope.launch {
            isDataReady.collect { ready ->
                if (ready) {
                    Log.d("NewsFeedViewModel", "Data is ready, collecting postsFlow")
                    try {
                        combine(
                            currentUserId,
                            selectedCategories
                        ) { userId, categories ->
                            userId to categories
                        }.flatMapLatest { (userId, categories) ->
                            if (userId == null || userId == -1) {
                                Log.w("NewsFeedViewModel", "User ID is null or -1, returning empty PagingData")
                                flowOf(PagingData.empty())
                            } else {
                                Log.d("NewsFeedViewModel", "Creating Pager with userId: $userId, categories: $categories")
                                Pager(
                                    config = PagingConfig(pageSize = 10, prefetchDistance = 2, enablePlaceholders = false),
                                    pagingSourceFactory = {
                                        Log.d("NewsFeedViewModel", "Creating NewsFeedPagingSource with apiService: $apiService")
                                        NewsFeedPagingSourceAllPosts(apiService, userId, categories, context)
                                    }
                                ).flow
                            }
                        }.cachedIn(viewModelScope).collect { pagingData ->
                            _postsFlow.value = pagingData
                            Log.d("NewsFeedViewModel", "Collected new PagingData with items: $pagingData")
                        }
                    } catch (e: Exception) {
                        Log.e("NewsFeedViewModel", "Error collecting postsFlow", e)
                        _postsFlow.value = PagingData.empty()
                    }
                } else {
                    Log.w("NewsFeedViewModel", "Data not ready yet, skipping postsFlow collection")
                }
            }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            Log.d("NewsFeedViewModel", "✅ Refresh triggered")
            _isRefreshing.value = false
        }
    }

    fun resetNewPostTrigger() {
        _newPostTrigger.value = false
    }

    fun addPost(context: Context, content: String, title: String, imageUri: Uri?, category: String, privacy: String) {
        viewModelScope.launch {
            val userId = currentUserId.value
            if (userId == null || userId <= 0) {
                Log.e("NewsFeedViewModel", "❌ Invalid userId: $userId. User not logged in or ID is invalid.")
                return@launch
            }
            try {
                val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId.toString())
                val contentPart = RequestBody.create("text/plain".toMediaTypeOrNull(), content)
                val titlePart = RequestBody.create("text/plain".toMediaTypeOrNull(), title)
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
                    Log.d("NewsFeedViewModel", "✅ Post created successfully")
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
                val userId = currentUserId.value ?: return@launch
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
            val userId = currentUserId.value ?: return@launch
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
                Log.d("ReportPost", "Attempting to report post $postId with reason: $reason")

                val userId = currentUserId.value ?: run {
                    Log.w("ReportPost", "User not logged in, aborting report")
                    onFailure("User not logged in")
                    return@launch
                }

                val finalReason = if (reason == "Others" && otherText != null) otherText else reason
                Log.i("ReportPost", "Final reason determined: $finalReason")

                val reportRequest = ReportRequest(
                    post_id = postId,
                    marketplace_item_id = null,
                    reporter_id = userId,
                    reason = finalReason
                )
                Log.d("ReportPost", "Report request created: $reportRequest")

                val response = apiService.createReport(reportRequest)
                Log.i("ReportPost", "API response received with code: ${response.code()}")

                if (response.isSuccessful) {
                    Log.i("ReportPost", "Post reported successfully")
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ReportPost", "Failed to report post. Error: $errorBody")
                    onFailure("Failed to report post: $errorBody")
                }
            } catch (e: Exception) {
                Log.e("ReportPost", "Exception occurred while reporting post", e)
                onFailure("Error reporting post: ${e.message}")
            }
        }
    }

    fun reportUser(userId: Int, reason: String, otherText: String?) {
        Log.d("NewsFeedViewModel", "Reported user $userId with reason: $reason, otherText: $otherText")
    }

    fun repostPost(postId: Int, comment: String) {
        viewModelScope.launch {
            val userId = currentUserId.value ?: return@launch
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

    fun getPostsByUserId(userId: Int): Flow<PagingData<NewsFeedDataClassItem>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { NewsFeedPagingSourceAllPosts(apiService, userId, selectedCategories.value, context) }
        ).flow.cachedIn(viewModelScope)
    }
}