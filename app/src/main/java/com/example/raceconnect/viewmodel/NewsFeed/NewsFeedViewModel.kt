package com.example.raceconnect.viewmodel.NewsFeed

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File

class NewsFeedViewModel(application: Application, private val userPreferences: UserPreferences) : AndroidViewModel(application) {
    private val _posts = MutableStateFlow<List<NewsFeedDataClassItem>>(emptyList())
    val posts: StateFlow<List<NewsFeedDataClassItem>> = _posts

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            try {
                _isRefreshing.value = true
                val response = RetrofitInstance.api.getAllPosts()
                _posts.value = response
            } catch (e: HttpException) {
                Log.e("NewsFeedViewModel", "Error fetching posts: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun refreshPosts() {
        fetchPosts()
    }

    fun addPost(content: String, imageUri: Uri?) {
        viewModelScope.launch {
            val userId = userPreferences.user.first()?.id
            if (userId == null) {
                Log.e("NewsFeedViewModel", "❌ User ID is null. Cannot create post.")
                return@launch
            }

            try {
                var imageUrl: String? = null

                // ✅ Step 1: Upload image first (if provided)
                if (imageUri != null) {
                    imageUrl = uploadImageToServer(imageUri, userId, content)
                    if (imageUrl == null) {
                        Log.e("NewsFeedViewModel", "❌ Image upload failed, skipping post creation")
                        return@launch
                    }
                }

                // ✅ Step 2: Create post with the image URL in the first request
                val newPost = NewsFeedDataClassItem(
                    id = 0,
                    user_id = userId,
                    title = "You",
                    content = content,
                    img_url = imageUrl, // ✅ Attach image URL in the same request
                    category = "Formula 1",
                    privacy = "Public",
                    type = if (imageUri != null) "image" else "text",
                    post_type = "normal",
                    like_count = 0,
                    comment_count = 0,
                    repost_count = 0,
                    created_at = "",
                    updated_at = ""
                )

                val response = RetrofitInstance.api.createPost(newPost)

                if (response.isSuccessful && response.body() != null) {
                    val createdPost = response.body()!!
                    Log.d("NewsFeedViewModel", "✅ Post created successfully: ID = ${createdPost.id}")

                    _posts.update { currentPosts -> currentPosts + createdPost }
                    fetchPosts()
                } else {
                    Log.e("NewsFeedViewModel", "❌ Failed to create post: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error adding post", e)
            }
        }
    }

    private suspend fun uploadImageToServer(imageUri: Uri, userId: Int, content: String): String? {
        return try {
            val contentResolver = getApplication<Application>().contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)

            if (inputStream == null) {
                Log.e("NewsFeedViewModel", "❌ Failed to open image file: $imageUri")
                return null
            }

            // ✅ Convert image to temporary file
            val tempFile = withContext(Dispatchers.IO) {
                File.createTempFile("upload_", ".jpg", getApplication<Application>().cacheDir)
            }
            tempFile.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }

            // ✅ Create RequestBody for image
            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), tempFile)
            val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)

            // ✅ Create RequestBody for additional fields (Fix applied here)
            val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId.toString())
            val contentPart = RequestBody.create("text/plain".toMediaTypeOrNull(), content)

            // ✅ Send API request with additional data
            val response = RetrofitInstance.api.uploadPostImage(imagePart, userIdPart, contentPart)

            if (response.isSuccessful && response.body()?.success == true) {
                Log.d("NewsFeedViewModel", "✅ Image uploaded successfully: ${response.body()?.imageUrl}")
                response.body()?.imageUrl
            } else {
                Log.e("NewsFeedViewModel", "❌ Image upload failed: ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e("NewsFeedViewModel", "❌ Error uploading image", e)
            null
        }
    }
}
