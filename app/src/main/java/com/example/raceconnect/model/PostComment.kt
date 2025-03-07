package com.example.raceconnect.model

import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.annotations.SerializedName
import java.util.Date

data class PostComment(
    val id: Int? = null,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("post_id") val postId: Int,
    @SerializedName("comment") val comment: String,
    @SerializedName("text") val text: String? = null,
    @SerializedName("created_at") val createdAt: Date? = null,
    val username: String? = null,
    val likes: Int = 0,
    val icon: ImageVector? = null
)