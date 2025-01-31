package com.example.raceconnect.model

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
