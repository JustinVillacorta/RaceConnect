import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.HttpException
import com.example.raceconnect.model.NewsFeedDataClassItem
import com.example.raceconnect.network.RetrofitInstance

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
                if (response.isSuccessful) {
                    _posts.add(response.body()!!) // Add the new post
                }
            } catch (e: Exception) {
                println("Error posting data: ${e.message}")
            }
        }
    }
}
