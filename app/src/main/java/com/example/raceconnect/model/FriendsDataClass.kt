package com.example.raceconnect.model
import com.google.gson.annotations.SerializedName

data class Friend(
    @SerializedName("id") val id: String = "",
    @SerializedName("username") val name: String = "",
    @SerializedName("profile_picture") val profileImageUrl: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("status") val status: String? = "NonFriends",
    @SerializedName("receiver_id") val receiverId: String? = null
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