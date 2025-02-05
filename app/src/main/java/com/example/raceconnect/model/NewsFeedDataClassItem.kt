package com.example.raceconnect.model

import androidx.compose.ui.graphics.vector.ImageVector

data class NewsFeedDataClassItem(
    val id: Int = 0,
    val user_id: Int = 0,
    val title: String? = null,
    val content: String? = null,
    val img_url: String? = null,
    val like_count: Int = 0,
    val comment_count: Int = 0,
    val repost_count: Int = 0,
    val type: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)


// change if needed
data class Comment(
    val username: String,
    val text: String,
    val timestamp: String,
    val likes: Int,
    val icon: ImageVector
)
