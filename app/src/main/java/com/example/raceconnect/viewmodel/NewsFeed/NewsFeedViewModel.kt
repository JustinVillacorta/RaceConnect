package com.example.raceconnect.viewmodel.NewsFeed

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.*
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException
import java.io.File

class NewsFeedViewModel(application: Application, private val userPreferences: UserPreferences) : AndroidViewModel(application) {

    // ✅ Paging source with caching
    private val _pager = MutableStateFlow(createPager())
    val posts = _pager.flatMapLatest { it.flow.cachedIn(viewModelScope) }

    // ✅ Refresh state (true when refreshing)
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // ✅ Create new pager for reloading
    private fun createPager(): Pager<Int, NewsFeedDataClassItem> {
        return Pager(
            config = PagingConfig(pageSize = 10, prefetchDistance = 2),
            pagingSourceFactory = { NewsFeedPagingSource() }
        )
    }

    // ✅ Force refresh function
    fun refreshPosts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                _pager.value = createPager() // ✅ Forces reloading posts
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error refreshing posts", e)
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun addPost(content: String, imageUri: Uri?) {
        viewModelScope.launch {
            val userId = userPreferences.user.first()?.id
            if (userId == null) {
                Log.e("NewsFeedViewModel", "❌ User ID is null. Cannot create post.")
                return@launch
            }

            try {
                // ✅ Step 1: Create the post first
                val newPost = NewsFeedDataClassItem(
                    id = 0,
                    user_id = userId,
                    title = "You",
                    content = content,
                    img_url = null,
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

                val postResponse = RetrofitInstance.api.createPost(newPost)

                if (!postResponse.isSuccessful || postResponse.body() == null) {
                    Log.e("NewsFeedViewModel", "❌ Failed to create post: ${postResponse.errorBody()?.string()}")
                    return@launch // Stop execution if post creation fails
                }

                val postId = postResponse.body()!!.id  // ✅ Get the post ID
                Log.d("NewsFeedViewModel", "✅ Post created successfully: ID = $postId")

                // ✅ Step 2: Upload image only if post creation is successful
                if (imageUri != null) {
                    val imageUrl = uploadImageToServer(imageUri, userId, content, postId)
                    if (imageUrl != null) {
                        Log.d("NewsFeedViewModel", "✅ Image uploaded successfully")
                    } else {
                        Log.e("NewsFeedViewModel", "❌ Image upload failed")
                    }
                }

                // ✅ Step 3: Refresh posts after successful post (with or without image)
                refreshPosts()

            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error adding post", e)
            }
        }
    }




    private suspend fun uploadImageToServer(imageUri: Uri, userId: Int, content: String, postId: Int): String? {
        return try {
            if (postId <= 0) {
                Log.e("NewsFeedViewModel", "❌ Invalid postId. Skipping image upload.")
                return null
            }

            val contentResolver = getApplication<Application>().contentResolver
            val inputStream = contentResolver.openInputStream(imageUri) ?: return null

            val tempFile = File.createTempFile("upload_", ".jpg", getApplication<Application>().cacheDir)
            tempFile.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }

            val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), tempFile)
            val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)

            // ✅ Include post_id, user_id, and content in the request
            val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userId.toString())
            val contentPart = RequestBody.create("text/plain".toMediaTypeOrNull(), content)
            val postIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), postId.toString())

            val response = RetrofitInstance.api.uploadPostImage(imagePart, userIdPart, contentPart, postIdPart)

            if (response.isSuccessful && response.body() != null) {
                Log.d("NewsFeedViewModel", "✅ Image uploaded successfully: ${response.body()!!.imageUrl}")
                response.body()!!.imageUrl
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
