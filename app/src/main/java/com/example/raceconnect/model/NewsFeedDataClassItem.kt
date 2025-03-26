package com.example.raceconnect.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.annotations.SerializedName

data class NewsFeedDataClassItem(
    val id: Int = 0,
    val user_id: Int,
    val username: String? = null,
    val title: String?,
    val content: String,
    @SerializedName("img_url") val imgUrl: String? = null,
    val like_count: Int = 0,
    val comment_count: Int = 0,
    val repost_count: Int = 0,
    val category: String = "Formula 1",
    val privacy: String = "Public",
    val type: String = "text",
    @SerializedName("post_type") val postType: String = "normal",
    val status: String? = null, // Added
    val created_at: String = "",
    val updated_at: String = "",
    val report: String? = null, // Added
    val archived_at: String? = null, // Added
    val profile_picture: String? = null, // Added
    val images: List<String>? = null,
    val isLiked: Boolean = false,
    val isRepost: Boolean? = false,
    val original_post_id: Int? = null,
    val quote: String? = null,
    val isAnnouncement: Boolean = false // New field to flag announcements
)


data class ProfileRepostsDataClass(
    val id: Int = 0,
    val user_id: Int,
    val username: String? = null,
    val title: String?,
    val content: String,
    @SerializedName("img_url") val imgUrl: String? = null,
    val like_count: Int = 0,
    val comment_count: Int = 0,
    val repost_count: Int = 0,
    val category: String = "Formula 1",
    val privacy: String = "Public",
    val type: String = "text",
    @SerializedName("post_type") val postType: String = "normal",
    val status: String? = null, // Added
    val created_at: String = "",
    val updated_at: String = "",
    val report: String? = null, // Added
    val archived_at: String? = null, // Added
    val profile_picture: String? = null, // Added
    val images: List<PostImage>? = null,
    val isLiked: Boolean = false,
    val isRepost: Boolean? = false,
    val original_post_id: Int? = null,
    val quote: String? = null,
    val isAnnouncement: Boolean = false // New field to flag announcements
)


data class PostByIdResponse(
    @SerializedName("original_post_id") val originalPostId: Int? = null,
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val username: String,
    val title: String,
    val content: String,
    @SerializedName("like_count") val likeCount: Int,
    @SerializedName("comment_count") val commentCount: Int,
    @SerializedName("repost_count") val repostCount: Int,
    val category: String,
    val privacy: String,
    val type: String,
    @SerializedName("post_type") val postType: String,
    val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("updated_at") val updatedAt: String,
    val report: String,
    @SerializedName("archived_at") val archivedAt: String?,
    @SerializedName("reported_at") val reportedAt: String?,
    @SerializedName("profile_picture") val profilePicture: String,
    val images: List<PostImage>
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


data class LikeRequest(
    val user_id: Int,
    val post_id: Int,
    val owner_id: Int
)

data class PostImage(
    val id: Int,
    val image_url: String
)

data class UpdatePostResponse(
    val message: String
)

data class UpdatePostRequest(
    @SerializedName("content")
    val content: String,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("privacy")
    val privacy: String? = null
)
