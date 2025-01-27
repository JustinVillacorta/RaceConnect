package com.example.raceconnect.model

data class Post(
    val title: String, // User's name or identifier
    val description: String?, // Text content of the post
    val images: List<Int> = emptyList(), // List of drawable resource IDs for images
    val videos: List<String> = emptyList(), // List of video URLs or paths
    val likeCount: Int = 0, // Initial like count
    val commentCount: Int = 0, // Initial comment count
    val shareCount: Int = 0, // Initial share count
    val postType: PostType = PostType.TEXT // Type of post (text, image, video, etc.)
)

enum class PostType {
    TEXT,
    IMAGE,
    VIDEO
}
