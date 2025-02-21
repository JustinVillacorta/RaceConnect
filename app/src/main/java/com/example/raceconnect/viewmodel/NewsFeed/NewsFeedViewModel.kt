package com.example.raceconnect.viewmodel.NewsFeed

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
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

                // ✅ Convert image to MultipartBody if available
                val imagePart = imageUri?.let { uri ->
                    val contentResolver = getApplication<Application>().contentResolver
                    val inputStream = contentResolver.openInputStream(uri) ?: return@let null

                    val tempFile = File.createTempFile("upload_", ".jpg", getApplication<Application>().cacheDir)
                    tempFile.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }

                    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), tempFile)
                    MultipartBody.Part.createFormData("image", tempFile.name, requestFile)
                }

                // ✅ Call API to create post with image (if available)
                val response = RetrofitInstance.api.createPostWithImage(
                    userIdPart, contentPart, titlePart, categoryPart, privacyPart, typePart, postTypePart, imagePart
                )

                if (response.isSuccessful) {
                    Log.d("NewsFeedViewModel", "✅ Post created successfully")
                    refreshPosts()
                } else {
                    Log.e("NewsFeedViewModel", "❌ Failed to create post: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "❌ Error adding post", e)
            }
        }
    }

}
