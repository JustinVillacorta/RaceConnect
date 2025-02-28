package com.example.raceconnect.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.annotations.SerializedName

data class NewsFeedDataClassItem(
    val id: Int = 0,
    val user_id: Int,
    val title: String?,
    val content: String,
    val img_url: String?,
    val like_count: Int = 0,
    val comment_count: Int = 0,
    val repost_count: Int = 0,
    val category: String = "Formula 1",
    val privacy: String = "Public",
    val type: String = "text",
    val post_type: String = "normal",
    val created_at: String = "",
    val updated_at: String = "",
    val isLiked: Boolean = false
)

data class PostResponse(
    val message: String,
    val post_id: String,  // Matches API response type
    val image_urls: List<String>
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



// likes

data class PostLike(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("post_id") val postId: Int,
    @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("created_at") val createdAt: String
)