import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateListOf
import com.example.raceconnect.datastore.UserPreferences
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.network.RetrofitInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class NewsFeedViewModel(application: Application) : AndroidViewModel(application) {
    private val userPreferences = UserPreferences(application)

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

                val validPosts = response.map { post ->
                    post.copy(
                        title = post.title ?: "Untitled Post",
                        content = post.content ?: "No content available.",
                        img_url = post.img_url ?: ""
                    )
                }
                _posts.value = validPosts
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

    fun addPost(content: String) {
        viewModelScope.launch {
            try {
                val userId = userPreferences.user.first()?.id
                if (userId == null) {
                    Log.e("NewsFeedViewModel", "User ID is null. Cannot create post.")
                    return@launch
                }

                val newPost = NewsFeedDataClassItem(
                    id = 0,
                    user_id = userId,
                    title = "You",
                    content = content,
                    img_url = "",
                    like_count = 0,
                    comment_count = 0,
                    repost_count = 0,
                    type = "text",
                    created_at = "",
                    updated_at = ""
                )

                val response = RetrofitInstance.api.createPost(newPost)
                if (response.isSuccessful && response.body() != null) {
                    val addedPost = response.body()!!

                    // Update the list with the newly added post
                    _posts.update { currentPosts -> currentPosts + addedPost }
                    Log.d("NewsFeedViewModel", "Post added successfully: $addedPost")

                    // Fetch the latest posts from the backend to ensure consistency
                    fetchPosts()
                } else {
                    Log.e("NewsFeedViewModel", "Failed to add post: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("NewsFeedViewModel", "Error adding post", e)
            }
        }
    }
}
