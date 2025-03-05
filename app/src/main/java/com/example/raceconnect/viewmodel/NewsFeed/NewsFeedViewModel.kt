package com.example.raceconnect.viewmodel.NewsFeed

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class NewsFeedViewModel(private val userPreferences: UserPreferences) : ViewModel() { // ✅ Remove Application

    private val _postLikes = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    private val _likeCounts = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val postLikes: StateFlow<Map<Int, Boolean>> = _postLikes.asStateFlow()
    val likeCounts: StateFlow<Map<Int, Int>> = _likeCounts.asStateFlow()

    private val _pager = MutableStateFlow(createPager())
    val posts = _pager.flatMapLatest { it.flow.cachedIn(viewModelScope) }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private fun createPager(): Pager<Int, NewsFeedDataClassItem> {
        return Pager(
            config = PagingConfig(pageSize = 10, prefetchDistance = 2),
            pagingSourceFactory = { NewsFeedPagingSource() }
        )
    }

    fun refreshPosts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _pager.value = createPager()
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error refreshing posts", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    private val _newPostTrigger = MutableStateFlow(false)
    val newPostTrigger: StateFlow<Boolean> = _newPostTrigger.asStateFlow()

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
                val categoryPart = RequestBody.create("text/plain".toMediaTypeOrNull(), category) // Use provided category
                val privacyPart = RequestBody.create("text/plain".toMediaTypeOrNull(), privacy) // Use provided privacy
                val typePart = RequestBody.create("text/plain".toMediaTypeOrNull(), if (imageUri != null) "image" else "text")
                val postTypePart = RequestBody.create("text/plain".toMediaTypeOrNull(), "normal")

                // ✅ Convert URI to File Before Upload
                val imagePart = imageUri?.let { uri ->
                    val tempFile = getFileFromUri(context, uri)
                    tempFile?.let {
                        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), it)
                        MultipartBody.Part.createFormData("image", it.name, requestFile)
                    }
                }

                val response = RetrofitInstance.api.createPostWithImage(
                    userIdPart, contentPart, titlePart, categoryPart, privacyPart, typePart, postTypePart, imagePart
                )

                if (response.isSuccessful) {
                    _newPostTrigger.value = true // ✅ Notify UI to refresh
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


    private val _postImages = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val postImages: StateFlow<Map<Int, List<String>>> = _postImages

    fun getPostImages(postId: Int) {
        viewModelScope.launch {
            try {
                Log.d("NewsFeedViewModel", "Fetching images for post ID: $postId")
                val response = RetrofitInstance.api.GetPostImg(postId)

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
                _postImages.value = _postImages.value.toMutableMap().apply {
                    this[postId] = emptyList()
                }
            }
        }
    }

    fun fetchPostLikes(postId: Int) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.user.first()?.id ?: return@launch
                val response = RetrofitInstance.api.getPostLikes(postId)
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

                val response = RetrofitInstance.api.likePost(requestBody)

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
                val response = RetrofitInstance.api.unlikePost(postId)

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

    fun reportPost(postId: Int) {
        viewModelScope.launch {
            try {
                // Replace with your actual reporting logic (e.g., API call)
                // For example:
                // repository.reportPost(postId)
                Log.d("NewsFeedViewModel", "Post $postId reported")
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "Error reporting post: $e")
            }
        }
    }



}
