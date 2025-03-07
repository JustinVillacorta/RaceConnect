package com.example.raceconnect.model

import com.google.gson.annotations.SerializedName
import java.util.*

data class Notification(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("post_id") val postId: Int?,
    @SerializedName("marketplace_item_id") val marketplaceItemId: Int?,
    val type: String,
    val content: String,
    @SerializedName("is_read") private val isReadInt: Int, // Change to Int to match backend
    @SerializedName("created_at") val createdAt: Date,
    @SerializedName("report_id") val reportId: Int?,
    val status: String?,
    @SerializedName("repost_id") val repostId: Int?,
    @SerializedName("like_id") val likeId: Int?,
    @SerializedName("comment_id") val commentId: Int?
) {
    // Add a computed property to convert Int to Boolean
    val isRead: Boolean
        get() = isReadInt == 1
}

data class NotificationRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("post_id") val postId: Int? = null,
    @SerializedName("marketplace_item_id") val marketplaceItemId: Int? = null,
    val type: String,  // Must be 'post', 'marketplace', 'system', or 'report'
    val content: String
)

data class CreateNotificationResponse(
    val message: String?,
    val error: String?
)

data class SimpleResponse(
    val message: String?,
    val error: String?
)