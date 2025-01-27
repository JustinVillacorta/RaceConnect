package com.example.raceconnect.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.raceconnect.model.Post
import com.example.raceconnect.R

class NewsFeedViewModel : ViewModel() {
    // State to store the list of posts
    private val _posts = mutableStateListOf(
        Post(
            title = "Juniffer Lawrence",
            description = "My new car is here! :))",
            images = listOf(R.drawable.baseline_account_circle_24, R.drawable.baseline_account_circle_24),
            likeCount = 100,
            commentCount = 27,
            shareCount = 18
        ),
        Post(
            title = "Dana Wheat",
            description = "Green Lambo Urus <333",
            images = listOf(R.drawable.img),
            likeCount = 107,
            commentCount = 35,
            shareCount = 12
        )
    )

    // Expose posts as immutable list
    val posts: List<Post> get() = _posts

    // Function to add a post
    fun addPost(post: Post) {
        _posts.add(post)
    }

    // Function to increment likes for a post
    fun likePost(index: Int) {
        _posts[index] = _posts[index].copy(likeCount = _posts[index].likeCount + 1)
    }

    // Add other functions like comment or share handling if needed
}