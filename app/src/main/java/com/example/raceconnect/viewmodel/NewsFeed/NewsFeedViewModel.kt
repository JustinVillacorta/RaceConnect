package com.example.raceconnect.viewmodel.NewsFeed

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
import com.example.raceconnect.model.LikeRequest
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.ReportRequest
import com.example.raceconnect.model.Repost
import com.example.raceconnect.model.UpdatePostRequest
import com.example.raceconnect.network.NewsFeedPagingSourceAllPosts
import com.example.raceconnect.network.RetrofitInstance
import com.example.raceconnect.network.UserPostsPagingSource
import com.example.raceconnect.viewmodel.NewsFeed.NewsFeedPreference.NewsFeedPreferenceViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class NewsFeedViewModel(
    private val userPreferences: UserPreferences,
    private val preferenceViewModel: NewsFeedPreferenceViewModel,
    private val context: Context
) : ViewModel() {

    private val _postLikes = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val postLikes: StateFlow<Map<Int, Boolean>> = _postLikes.asStateFlow()

    private val _likeCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val likeCounts: StateFlow<Map<Int, Int>> = _likeCounts.asStateFlow()

    private val _userLikeIds = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val userLikeIds: StateFlow<Map<Int, Int>> = _userLikeIds

    // New StateFlows for comment and repost counts
    private val _commentCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val commentCounts: StateFlow<Map<Int, Int>> = _commentCounts.asStateFlow()

    private val _repostCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val repostCounts: StateFlow<Map<Int, Int>> = _repostCounts.asStateFlow()

    private val _newPostTrigger = MutableStateFlow(false)
    val newPostTrigger: StateFlow<Boolean> = _newPostTrigger.asStateFlow()

    private val _postImages = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val postImages: StateFlow<Map<Int, List<String>>> = _postImages.asStateFlow()

    private val apiService = RetrofitInstance.api
    var isInitialRefreshDone = false

    private val _currentUserId = MutableStateFlow<Int?>(null)
    val currentUserId: StateFlow<Int?> = _currentUserId.asStateFlow()

    private val _selectedCategories = MutableStateFlow<List<String>>(emptyList())
    val selectedCategories: StateFlow<List<String>> = _selectedCategories.asStateFlow()

    // Map categories from user preferences to API-compatible codes
    private fun mapCategories(brandNames: List<String>): List<String> {
        return brandNames.map { brandName ->
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
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val postsFlow: Flow<PagingData<NewsFeedDataClassItem>> = combine(
        currentUserId,
        userPreferences.selectedCategories
    ) { userId, rawCategories ->
        userId to mapCategories(rawCategories)
    }.flatMapLatest { (userId, mappedCategories) ->
        if (userId == null || userId == -1) {
            Pager(
                config = PagingConfig(pageSize = 10, enablePlaceholders = false),
                pagingSourceFactory = { NewsFeedPagingSourceAllPosts(apiService, userId ?: -1, mappedCategories, context) }
            ).flow.cachedIn(viewModelScope)
        } else {
            Pager(
                config = PagingConfig(pageSize = 10, prefetchDistance = 2, enablePlaceholders = false),
                pagingSourceFactory = { NewsFeedPagingSourceAllPosts(apiService, userId, mappedCategories, context) }
            ).flow.cachedIn(viewModelScope)
        }
    }

    // Updated getPostsByUserId
    fun getPostsByUserId(userId: Int): Flow<PagingData<NewsFeedDataClassItem>> {
        return Pager(
            config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = { UserPostsPagingSource(apiService, userId) }
        ).flow.cachedIn(viewModelScope)
    }

    init {
        viewModelScope.launch {
            // Observe user changes continuously
            userPreferences.user.collect { user ->
                _currentUserId.value = user?.id
                Log.d("NewsFeedViewModel", "User ID updated: ${_currentUserId.value}")

                // Reset post-related state when user changes
                if (user == null) {
                    _postLikes.value = emptyMap()
                    _likeCounts.value = emptyMap()
                    _postImages.value = emptyMap()
                    Log.d("NewsFeedViewModel", "Cleared post-related state due to user change")
                }
            }
        }

        viewModelScope.launch {
            try {
                val rawCategories = userPreferences.selectedCategories.first()
                val mappedCategories = mapCategories(rawCategories)
                _selectedCategories.value = mappedCategories
                Log.d("NewsFeedViewModel", "Initial categories: $mappedCategories (from raw: $rawCategories)")
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "Error fetching initial data", e)
            }
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            Log.d("NewsFeedViewModel", "Refresh triggered")
            _newPostTrigger.value = true
        }
    }

    fun resetNewPostTrigger() {
        _newPostTrigger.value = true
    }

    fun addPost(context: Context, content: String, title: String, imageUris: List<Uri>, category: String, privacy: String) {
        viewModelScope.launch {
            val userId = currentUserId.value ?: run {
                Log.e("NewsFeedViewModel", "Invalid userId")
                return@launch
            }
            try {
                val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId.toString())
                val contentPart = RequestBody.create("text/plain".toMediaTypeOrNull(), content)
                val titlePart = RequestBody.create("text/plain".toMediaTypeOrNull(), title)
                val categoryPart = RequestBody.create("text/plain".toMediaTypeOrNull(), category)
                val privacyPart = RequestBody.create("text/plain".toMediaTypeOrNull(), privacy)
                val typePart = RequestBody.create("text/plain".toMediaTypeOrNull(), if (imageUris.isNotEmpty()) "image" else "text")
                val postTypePart = RequestBody.create("text/plain".toMediaTypeOrNull(), "normal")

                // Create image parts, using "image[]" as the field name to match MarketplaceViewModel
                val imageParts: List<MultipartBody.Part> = imageUris.mapNotNull { uri ->
                    val tempFile = getFileFromUri(context, uri)
                    tempFile?.let {
                        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                        MultipartBody.Part.createFormData("image[]", it.name, requestFile)
                    }
                }
                Log.d("NewsFeedViewModel", "Prepared ${imageParts.size} image parts for upload: $imageParts")

                val response = apiService.createPostWithImage(
                    userIdPart, contentPart, titlePart, categoryPart, privacyPart, typePart, postTypePart,
                    images  = if (imageParts.isNotEmpty()) imageParts else null
                )

                if (response.isSuccessful) {
                    _newPostTrigger.value = true
                    refreshPosts()
                    Log.d("NewsFeedViewModel", "✅ Post created with images")
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
                    Log.d("NewsFeedViewModel", "Raw response for postId=$postId: ${response.body()}")
                    val imageUrls = postResponses.map { it.image_url }
                    _postImages.value = _postImages.value.toMutableMap().apply {
                        this[postId] = imageUrls
                    }
                    fetchPostLikes(postId)
                    fetchPostComments(postId)
                    fetchPostReposts(postId)
                } else {
                    Log.w("NewsFeedViewModel", "Failed to fetch images for post ID: $postId, HTTP ${response.code()}")
                    _postImages.value = _postImages.value.toMutableMap().apply {
                        this[postId] = emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "Error fetching post images for postId=$postId", e)
            }
        }
    }
    // Fetch comments for a post
    fun fetchPostComments(postId: Int) {
        viewModelScope.launch {
            try {
                val token = userPreferences.token.first() ?: run {
                    Log.w("NewsFeedViewModel", "No auth token available for fetching comments for post ID: $postId")
                    return@launch
                }
                Log.d("NewsFeedViewModel", "Fetching comments for post ID: $postId")
                val response = apiService.getCommentsByPostId("Bearer $token", postId)
                if (response.isSuccessful) {
                    val comments = response.body() ?: emptyList()
                    Log.d("NewsFeedViewModel", "Comments fetched: ${comments.size} for post ID: $postId")
                    val commentCount = comments.size
                    _commentCounts.value = _commentCounts.value + (postId to commentCount)
                } else {
                    Log.w("NewsFeedViewModel", "Failed to fetch comments for post ID: $postId, HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error fetching comments for post ID: $postId", e)
            }
        }
    }

    // Fetch reposts for a post
    fun fetchPostReposts(postId: Int) {
        viewModelScope.launch {
            try {
                Log.d("NewsFeedViewModel", "Fetching reposts for post ID: $postId")
                val response = apiService.getRepostsCountByPostId(postId, limit = 1000, offset = 0)
                if (response.isSuccessful) {
                    val reposts = response.body() ?: emptyList()
                    // Filter reposts client-side
                    val filteredReposts = reposts.filter { it.postId == postId }
                    Log.d("NewsFeedViewModel", "Raw reposts response for post ID $postId: $reposts")
                    Log.d("NewsFeedViewModel", "Filtered reposts for post ID $postId: $filteredReposts")
                    val repostCount = filteredReposts.size
                    _repostCounts.value = _repostCounts.value + (postId to repostCount)
                    Log.d("NewsFeedViewModel", "Updated _repostCounts for postId=$postId: ${_repostCounts.value}")
                } else {
                    Log.w("NewsFeedViewModel", "Failed to fetch reposts for post ID: $postId, HTTP ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error fetching reposts for post ID: $postId", e)
            }
        }
    }

    fun fetchPostLikes(postId: Int) {
        viewModelScope.launch {
            try {
                val userId = currentUserId.value ?: return@launch
                Log.d("NewsFeedViewModel", "Fetching likes for post ID: $postId")
                val response = apiService.getPostLikes(postId)
                if (response.isSuccessful) {
                    val likes = response.body() ?: emptyList()
                    Log.d("NewsFeedViewModel", "Likes fetched: ${likes.size} for post ID: $postId")
                    val userLike = likes.find { it.userId == userId }
                    val isLiked = userLike != null
                    val likeCount = likes.size
                    _postLikes.value = _postLikes.value + (postId to isLiked)
                    _likeCounts.value = _likeCounts.value + (postId to likeCount)
                    if (userLike != null) {
                        _userLikeIds.value = _userLikeIds.value + (postId to userLike.id)
                        Log.d("NewsFeedViewModel", "User has liked post ID: $postId, like ID: ${userLike.id}")
                    } else {
                        _userLikeIds.value = _userLikeIds.value - postId
                        Log.d("NewsFeedViewModel", "User has not liked post ID: $postId")
                    }
                } else {
                    Log.w("NewsFeedViewModel", "Failed to fetch likes for post ID: $postId, HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error fetching likes for post ID: $postId", e)
            }
        }
    }

    fun toggleLike(postId: Int, ownerId: Int) {
        val isLiked = _postLikes.value[postId] ?: false
        Log.d("NewsFeedViewModel", "Toggling like for post ID: $postId, currently liked: $isLiked")
        if (isLiked) {
            unlikePost(postId)
        } else {
            likePost(postId, ownerId)
        }
    }


    fun likePost(postId: Int, ownerId: Int) {
        viewModelScope.launch {
            val userId = currentUserId.value ?: return@launch
            Log.d("NewsFeedViewModel", "Liking post ID: $postId for user ID: $userId")
            _postLikes.value = _postLikes.value + (postId to true)
            _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) + 1)
            try {
                val request = LikeRequest(userId, postId, ownerId)
                val response = apiService.likePost(request)
                if (response.isSuccessful) {
                    Log.d("NewsFeedViewModel", "Successfully liked post ID: $postId")
                    fetchPostLikes(postId) // Refetch to get the new like ID
                } else {
                    Log.w("NewsFeedViewModel", "Failed to like post ID: $postId, HTTP ${response.code()}")
                    _postLikes.value = _postLikes.value + (postId to false)
                    _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) - 1)
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error liking post ID: $postId", e)
                _postLikes.value = _postLikes.value + (postId to false)
                _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) - 1)
            }
        }
    }

    fun unlikePost(postId: Int) {
        val likeId = _userLikeIds.value[postId] ?: return
        Log.d("NewsFeedViewModel", "Unliking post ID: $postId with like ID: $likeId")
        viewModelScope.launch {
            _postLikes.value = _postLikes.value + (postId to false)
            _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) - 1)
            _userLikeIds.value = _userLikeIds.value - postId
            try {
                val response = apiService.unlikePost(likeId)
                if (response.isSuccessful) {
                    Log.d("NewsFeedViewModel", "Successfully unliked post ID: $postId")
                } else {
                    Log.w("NewsFeedViewModel", "Failed to unlike post ID: $postId, HTTP ${response.code()}")
                    _postLikes.value = _postLikes.value + (postId to true)
                    _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) + 1)
                    _userLikeIds.value = _userLikeIds.value + (postId to likeId)
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error unliking post ID: $postId", e)
                _postLikes.value = _postLikes.value + (postId to true)
                _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) + 1)
                _userLikeIds.value = _userLikeIds.value + (postId to likeId)
            }
        }
    }

    fun deletePost(postId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.deletePost(postId)
                if (response.isSuccessful) {
                    Log.d("NewsFeedViewModel", "Post deleted successfully: $postId")
                    resetNewPostTrigger() // Trigger UI refresh by setting _newPostTrigger to true
                } else {
                    Log.e("NewsFeedViewModel", "Failed to delete post $postId: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "Error deleting post $postId", e)
            }
        }
    }



    fun updatePost(
        postId: Int,
        updatedContent: String,
        updatedTitle: String?,
        updatedCategory: String?,
        updatedPrivacy: String?,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val contentPart = updatedContent.toRequestBody("text/plain".toMediaTypeOrNull())
                val titlePart = updatedTitle?.toRequestBody("text/plain".toMediaTypeOrNull())
                val categoryPart = updatedCategory?.toRequestBody("text/plain".toMediaTypeOrNull())
                val privacyPart = updatedPrivacy?.toRequestBody("text/plain".toMediaTypeOrNull())
                val typePart = "text".toRequestBody("text/plain".toMediaTypeOrNull())
                val postTypePart = "normal".toRequestBody("text/plain".toMediaTypeOrNull())

                val imageParts = if (imageUri != null) {
                    val file = File(context.contentResolver.getFileFromUri(imageUri)?.path ?: "")
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    listOf(MultipartBody.Part.createFormData("image", file.name, requestFile))
                } else {
                    null
                }

                val response = apiService.updatePostWithImage(
                    postId = postId,
                    content = contentPart,
                    title = titlePart,
                    category = categoryPart,
                    privacy = privacyPart,
                    type = typePart,
                    postType = postTypePart,
                    images = imageParts
                )

                if (response.isSuccessful) {
                    _newPostTrigger.value = true
                    refreshPosts()
                    onSuccess()
                } else {
                    onFailure("Failed to update post: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                onFailure(e.message ?: "Unknown error")
            }
        }
    }

    // Add this extension function to get File from Uri
    fun ContentResolver.getFileFromUri(uri: Uri): File? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = query(uri, filePathColumn, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val filePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return filePath?.let { File(it) }
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

    private val _userReposts = MutableStateFlow<List<Repost>>(emptyList())
    val userReposts: StateFlow<List<Repost>> = _userReposts.asStateFlow()

    fun fetchUserReposts(userId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.getRepostsByUserId(userId)
                if (response.isSuccessful) {
                    _userReposts.value = response.body() ?: emptyList()
                } else {
                    Log.e("NewsFeedViewModel", "Failed to fetch reposts: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "Error fetching reposts", e)
            }
        }
    }

    // Map to store original posts by their IDs
    private val _originalPosts = MutableStateFlow<Map<Int, NewsFeedDataClassItem>>(emptyMap())
    val originalPosts: StateFlow<Map<Int, NewsFeedDataClassItem>> = _originalPosts.asStateFlow()

    // Function to fetch the original post
    fun fetchOriginalPost(postId: Int) {
        viewModelScope.launch {
            try {
                val response = apiService.getPostById(postId) // API call to get post by ID
                if (response.isSuccessful) {
                    response.body()?.let { post ->
                        _originalPosts.value = _originalPosts.value + (postId to post)
                    }
                } else {
                    Log.e("NewsFeedViewModel", "Failed to fetch post: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "Error fetching post", e)
            }
        }
    }

}