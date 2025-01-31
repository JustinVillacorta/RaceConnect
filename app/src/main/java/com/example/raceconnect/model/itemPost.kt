package com.example.raceconnect.model

data class itemPostRequest(
    val category: String,
    val description: String,
    val favorite_count: Int,
    val image_url: String,
    val price: Double,
    val seller_id: Int,
    val status: String,
    val title: String
)

data class itemPostResponse(
    val message: String,
)