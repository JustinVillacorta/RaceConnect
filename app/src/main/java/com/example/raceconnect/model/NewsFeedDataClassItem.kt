package com.example.raceconnect.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.annotations.SerializedName

data class NewsFeedDataClassItem(
    val id: Int = 0,
    val user_id: Int,
    val username: String? = null,
    val title: String?,
    val content: String,
    @SerializedName("img_url") val imgUrl: String? = null, // Kept for backward compatibility if API uses it
    val like_count: Int = 0,
    val comment_count: Int = 0,
    val repost_count: Int = 0,
    val category: String = "Formula 1",
    val privacy: String = "Public",
    val type: String = "text",
    @SerializedName("post_type") val postType: String = "normal",
    val created_at: String = "",
    val updated_at: String = "",
    val isLiked: Boolean = false,
    val images: List<String>? = null, // Added to match API response
    val isRepost: Boolean? = false,  // Default to false if null
    val original_post_id: Int? = null
)

data class PostResponse(
    val message: String,
    val post_id: String,
    val image_url: String
)

data class Comment(
    val username: String,
    val text: String,
    val timestamp: String,
    val likes: Int,
    val icon: ImageVector
)

data class PostLike(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("post_id") val postId: Int,
    @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("created_at") val createdAt: String
)

data class ReportRequest(
    @SerializedName("post_id") val post_id: Int?,
    @SerializedName("marketplace_item_id") val marketplace_item_id: Int?,
    @SerializedName("reporter_id") val reporter_id: Int,
    @SerializedName("reason") val reason: String
)

data class ReportResponse(
    val message: String,
    val report_id: Int?
)