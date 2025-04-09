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
    @SerializedName("is_read") private val isReadInt: Int,
    @SerializedName("created_at") val createdAt: Date,
    @SerializedName("report_id") val reportId: Int?,
    val status: String?,
    @SerializedName("repost_id") val repostId: Int?,
    @SerializedName("like_id") val likeId: Int?,
    @SerializedName("comment_id") val commentId: Int?,
    @SerializedName("convo_id") val convoId: Int?,
    @SerializedName("trigger_user_id") val triggerUserId: Int?,
    @SerializedName("trigger_username") val triggerUsername: String?,
    @SerializedName("trigger_profile_picture") val triggerProfilePicture: String?,
    @SerializedName("is_admin") private val isAdminInt: Int?
) {
    val isRead: Boolean
        get() = isReadInt == 1

    val isAdmin: Boolean
        get() = isAdminInt == 1 // True if sent by admin
}



data class SimpleResponse(
    val message: String?,
    val error: String?
)