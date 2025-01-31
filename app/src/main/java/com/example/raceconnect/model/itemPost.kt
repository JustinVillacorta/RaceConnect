package com.example.raceconnect.model

data class itemPostRequest(
    val user_id: Int,
    val category: String,
    val description: String,
    val favorite_count: Int,
    val image_url: String,
    val price: Double,
    val seller_id: Int,
    val status: String,
    val title: String
)

