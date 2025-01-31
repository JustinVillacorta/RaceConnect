package com.example.raceconnect.model

data class MarketplaceDataClassItem(
    val category: String,
    val created_at: String,
    val description: String,
    val favorite_count: Int,
    val id: Int,
    val image_url: String,
    val price: String,
    val seller_id: Int,
    val status: String,
    val title: String,
    val updated_at: String
)
data class itemPostResponse(
    val message: String
    )