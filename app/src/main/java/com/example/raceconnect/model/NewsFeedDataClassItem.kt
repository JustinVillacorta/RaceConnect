package com.example.raceconnect.model

data class NewsFeedDataClassItem(
    val comment_count: Int,
    val content: String,
    val created_at: String,
    val id: Int,
    val img_url: String,
    val like_count: Int,
    val repost_count: Int,
    val title: String,
    val type: String,
    val updated_at: String,
    val user_id: Int
)