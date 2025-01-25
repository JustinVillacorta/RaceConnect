package com.example.raceconnect.Model

data class Post(
    val title: String,
    val description: String?,
    val images: List<Int>, // A list of drawable resource IDs
    val likeCount: Int,
    val commentCount: Int,
    val shareCount: Int
)
