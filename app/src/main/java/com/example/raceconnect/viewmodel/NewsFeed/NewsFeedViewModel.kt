package com.example.raceconnect.viewmodel.NewsFeed

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.model.PostResponse
import com.example.raceconnect.network.RetrofitInstance
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class NewsFeedViewModel(application: Application, private val userPreferences: UserPreferences) :
    AndroidViewModel(application) {


    private val _postLikes = MutableStateFlow<Map<Int, Boolean>>(emptyMap()) // Post ID -> Liked Status
    private val _likeCounts = MutableStateFlow<Map<Int, Int>>(emptyMap()) // Post ID -> Like Count
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

    fun resetNewPostTrigger() {
        _newPostTrigger.value = false
    }


    private val _newPostTrigger = MutableStateFlow(false) // ✅ Tracks new posts
    val newPostTrigger: StateFlow<Boolean> = _newPostTrigger.asStateFlow()

    fun addPost(content: String, imageUri: Uri?) {
        viewModelScope.launch {
            val userId = userPreferences.user.first()?.id ?: return@launch

            try {
                val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId.toString())
                val contentPart = RequestBody.create("text/plain".toMediaTypeOrNull(), content)
                val titlePart = RequestBody.create("text/plain".toMediaTypeOrNull(), "You")
                val categoryPart = RequestBody.create("text/plain".toMediaTypeOrNull(), "Formula 1")
                val privacyPart = RequestBody.create("text/plain".toMediaTypeOrNull(), "Public")
                val typePart = RequestBody.create("text/plain".toMediaTypeOrNull(), if (imageUri != null) "image" else "text")
                val postTypePart = RequestBody.create("text/plain".toMediaTypeOrNull(), "normal")

                val imagePart = imageUri?.let { uri ->
                    val contentResolver = getApplication<Application>().contentResolver
                    val inputStream = contentResolver.openInputStream(uri) ?: return@let null

                    val tempFile = File.createTempFile("upload_", ".jpg", getApplication<Application>().cacheDir)
                    tempFile.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }

                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), tempFile)
                    MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
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


    private val _postImages = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val postImages: StateFlow<Map<Int, List<String>>> = _postImages

    fun getPostImages(postId: Int) {
        viewModelScope.launch {
            try {
                Log.d("NewsFeedViewModel", "Fetching images for post ID: $postId")
                val response = RetrofitInstance.api.GetPostImg(postId)

                if (response.isSuccessful) {
                    val postResponses = response.body() ?: emptyList()

                    // Extract only image URLs
                    val imageUrls = postResponses.map { it.image_url }

                    Log.d("NewsFeedViewModel", "✅ Extracted Image URLs for post $postId: $imageUrls")

                    // Update the map to keep existing images for other posts
                    _postImages.value = _postImages.value.toMutableMap().apply {
                        this[postId] = imageUrls
                    }
                } else {
                    Log.e("NewsFeedViewModel", "❌ Failed to fetch images: ${response.errorBody()?.string()}")
                    _postImages.value = _postImages.value.toMutableMap().apply {
                        this[postId] = emptyList()
                    }
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error fetching post images", e)
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

                    if (_postLikes.value.containsKey(postId)) { // ✅ Prevent overriding newly added posts
                        _postLikes.value = _postLikes.value + (postId to isLiked)
                        _likeCounts.value = _likeCounts.value + (postId to likeCount)
                    }
                } else {
                    Log.e("NewsFeedViewModel", "❌ Failed to fetch likes: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error fetching likes", e)
            }
        }
    }


    fun toggleLike(postId: Int, ownerId: Int) {
        viewModelScope.launch {
            val userId = userPreferences.user.first()?.id ?: return@launch

            try {
                val requestBody = mapOf(
                    "user_id" to userId,
                    "post_id" to postId,
                    "owner_id" to ownerId
                )

                // ✅ Optimistic UI update before sending API request
                _postLikes.value = _postLikes.value + (postId to true)
                _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) + 1)

                val response = RetrofitInstance.api.likePost(requestBody)

                if (!response.isSuccessful) {
                    Log.e("NewsFeedViewModel", "❌ Failed to like post: ${response.errorBody()?.string()}")
                    // Revert the UI update if API call fails
                    _postLikes.value = _postLikes.value + (postId to false)
                    _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) - 1)
                } else {
                    Log.d("NewsFeedViewModel", "✅ Like added successfully")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error liking post", e)
                // Revert UI update on error
                _postLikes.value = _postLikes.value + (postId to false)
                _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) - 1)
            }
        }
    }


    fun unlikePost(postId: Int) {
        viewModelScope.launch {
            try {
                // ✅ Optimistic UI update before sending API request
                _postLikes.value = _postLikes.value + (postId to false)
                _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) - 1)

                val response = RetrofitInstance.api.unlikePost(postId)

                if (!response.isSuccessful) {
                    Log.e("NewsFeedViewModel", "❌ Failed to unlike post: ${response.errorBody()?.string()}")
                    // Revert the UI update if API call fails
                    _postLikes.value = _postLikes.value + (postId to true)
                    _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) - 1)
                } else {
                    Log.d("NewsFeedViewModel", "✅ Post unliked successfully")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error unliking post", e)
                // Revert UI update on error
                _postLikes.value = _postLikes.value + (postId to true)
                _likeCounts.value = _likeCounts.value + (postId to (_likeCounts.value[postId] ?: 0) - 1)
            }
        }
    }




}
