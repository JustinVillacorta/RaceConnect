package com.example.raceconnect.model

import com.google.gson.annotations.SerializedName

data class Repost(
    @SerializedName("id") val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("post_id") val postId: Int,
    @SerializedName("owner_id") val ownerId: Int,
    @SerializedName("quote") val quote: String?,
    @SerializedName("created_at") val createdAt: String
)

data class CreateRepostRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("post_id") val postId: Int,
    @SerializedName("quote") val quote: String? = null
)

data class CreateRepostResponse(
    @SerializedName("message") val message: String,
    @SerializedName("repost_id") val repostId: Int?
)