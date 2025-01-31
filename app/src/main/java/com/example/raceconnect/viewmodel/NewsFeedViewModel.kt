import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.network.RetrofitInstance
import kotlin.math.log

class NewsFeedViewModel : ViewModel() {
    private val _posts = mutableStateListOf<NewsFeedDataClassItem>()
    val posts: List<NewsFeedDataClassItem> get() = _posts

    init {
        fetchPosts()
    }

    private fun fetchPosts() {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getAllPosts()
                _posts.clear()
                _posts.addAll(response)
            } catch (e: HttpException) {
                println("Error fetching posts: ${e.message}")
            }
        }
    }

    fun addPost(post: NewsFeedDataClassItem) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.createPost(post)
                if (response.isSuccessful && response.body() != null) {
                    val newPost = response.body()!!
                    _posts.add(newPost) // Add to the list
                    Log.d("AddPost", "Post added successfully: $newPost")
                } else {
                    Log.e("AddPost", "Failed to add post: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("AddPost", "Error posting data: ${e.message}", e)
            }
        }
    }

}
