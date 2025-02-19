package com.example.raceconnect.model

import androidx.compose.ui.graphics.vector.ImageVector

data class NewsFeedDataClassItem(
    val id: Int = 0,  // Auto-increment in DB
    val user_id: Int,  // Foreign key (Must exist in Users table)
    val title: String?,  // Optional title
    val content: String,  // Required
    val img_url: String?,  // Optional image URL
    val like_count: Int = 0,  // Default 0
    val comment_count: Int = 0,  // Default 0
    val repost_count: Int = 0,  // Default 0
    val category: String = "Formula 1",  // Must match ENUM values
    val privacy: String = "Public",  // Must match ENUM values
    val type: String = "text",  // Must match ENUM values
    val post_type: String = "normal",  // Can be "announcement" or "normal"
    val created_at: String = "",  // Timestamp auto-filled in DB
    val updated_at: String = ""  // Timestamp auto-updates in DB
)




data class ImageUploadResponse(
    val success: Boolean,
    val imageUrl: String
)


// change if needed
data class Comment(
    val username: String,
    val text: String,
    val timestamp: String,
    val likes: Int,
    val icon: ImageVector
)
