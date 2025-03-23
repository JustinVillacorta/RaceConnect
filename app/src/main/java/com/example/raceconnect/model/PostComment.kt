package com.example.raceconnect.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.annotations.SerializedName
import java.util.Date

data class PostComment(
    val id: Int? = null,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("post_id") val postId: Int,
    @SerializedName("comment") val comment: String = "", // Default to empty string
    @SerializedName("created_at") val createdAt: Date? = null,
    val username: String? = null,
    val likes: Int = 0,
    val isLiked: Boolean = false,
    var replies: List<Reply> = emptyList()
)

data class Reply(
    val id: Int? = null,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("parent_comment_id") val parentCommentId: Int,
    @SerializedName("reply_text") val text: String = "", // Default to empty string
    @SerializedName("created_at") val createdAt: Date? = null,
    val username: String? = null,
    val likes: Int = 0,
    val isLiked: Boolean = false
)

data class ReplyRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("parent_comment_id") val parentCommentId: Int,
    @SerializedName("reply_text") val replyText: String
)

data class AddReplyResponse(
    val message: String,
    @SerializedName("reply_id") val replyId: Int
)

