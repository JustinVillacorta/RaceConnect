package com.example.raceconnect.Model

data class Post(
    val title: String,
    val images: List<Int>,
    val likeCount: Int = 0,
    val commentCount: Int = 0
)
