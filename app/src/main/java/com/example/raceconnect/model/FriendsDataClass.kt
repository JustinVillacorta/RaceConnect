package com.example.raceconnect.model
import com.google.gson.annotations.SerializedName

data class Friend(
    val id: String, // Maps to user_id or friend_id (we'll decide which to use)
    val name: String, // Maps to username
    val status: String,
    val profileImageUrl: String? = null // Optional
)



data class FriendRequest(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("friend_id")
    val friendId: String
)

data class UpdateFriendStatus(
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("friend_id")
    val friendId: String,
    val status: String
)

data class RemoveFriendRequest(
    @SerializedName("user_id")
    val user_id: String,
    @SerializedName("friend_id")
    val friend_id: String
)